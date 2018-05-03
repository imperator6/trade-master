package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service
import tradingmaster.core.CandleAggregator
import tradingmaster.db.PositionRepository
import tradingmaster.db.SignalRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.entity.json.Config
import tradingmaster.exchange.paper.PaperExchange
import tradingmaster.model.*
import tradingmaster.strategy.*
import tradingmaster.strategy.runner.CombinedStrategyRun
import tradingmaster.strategy.runner.IStrategyRunner
import tradingmaster.util.DateHelper

import javax.annotation.PostConstruct
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Service
@Commons
class StrategyRunnerService implements  MessageHandler {

    Map<String, ReentrantLock> lockMap = new ConcurrentHashMap()

    Map<String, IStrategyRunner> backtestRunnerMap = [:]

    @Autowired
    TaskExecutor backtestTaskExecutor

    @Autowired
    PublishSubscribeChannel mixedCandelSizesChannel

    @Autowired
    PublishSubscribeChannel backtestChannel

    @Autowired
    ICandleStore candleStore

    @Autowired
    ApplicationContext ctx

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    SignalRepository signalRepository

    @Autowired
    PositionRepository positionRepository

    @PostConstruct
    init() {
        backtestChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        BacktestMessage msg = message.getPayload()

        BacktestRun lock = lockMap.get(msg.getBacktestId())

        if(lock == null) {
            log.error("Can't find BacktestRun for id ${msg.getBacktestId()}")
            return
        }

        synchronized (lock) {
            if("complete".equalsIgnoreCase(msg.action)) {
                lock.unlock()
            } else if ("setSignalCount".equalsIgnoreCase(msg.action)) {
                lock.setTotalSignalCount(msg.signalCount)
            } else if ("signalComplete".equalsIgnoreCase(msg.action)) {

                if(lock.getTotalSignalCount() == null) {
                    throw new RuntimeException("totalSignalCount is ZERO! Can't increase complete count!")
                }

                if(msg.getPositionId() != null) {
                    Position pos = tradeBotManager.findPositionById(lock.getBotId(), msg.getPositionId())
                    if(pos != null)
                        positionRepository.save(pos)
                }

                lock.increaseSignalCompleteCount()

                if(lock.allSignalseComplete()) {
                    lock.unlock()
                }
            }
        }

    }


    String startStrategy(Integer strategyId) {

        log.info("Starting a new ScriptStrategy!")

        crerateStrategyRun(strategyId, false)

        //runnerMap.put(config.id, run)
    }

    String startBacktest(LocalDateTime start, LocalDateTime end, StrategyRunConfig config) {


        TradeBot bot = tradeBotManager.findBotById(config.getBotId())

        if(bot.backtest) {

            log.info("Starting a new Backtest for strategyId/config ${config.strategyId}.")

        } else {
            log.error("Can't backtest bot with id ${config.getBotId()}. As flag backtest is false")
        }

        bot.config.backtest.market = config.market
        bot.config.backtest.startDate = DateHelper.toDate(start)
        bot.config.backtest.endDate = DateHelper.toDate(end)

        tradeBotManager.save(bot)

        // update candle size
        bot.config.candleSize = config.getCandleSize() + "m"
        //bot.config.exchange = 'PaperExchange'

        PaperExchange exchange = bot.getPaperExchange()
        exchange.config = bot.config
        exchange.setBalance((String) bot.config.baseCurrency, (BigDecimal) bot.config.backtest.startBalance)

        bot.setStartBalance( (BigDecimal) bot.config.backtest.startBalance )
        bot.setTotalBaseCurrencyValue( (BigDecimal) bot.config.backtest.startBalance )
        bot.setResult( 0.0 )
        bot.setTotalBalanceDollar( null )
        bot.setFxDollar( null )

        // delete all exsisting positions
        tradeBotManager.removeAllPositions(bot.getId())
        signalRepository.deleteByBotId(bot.getId())

        bot.setStrategyRunner( crerateStrategyRun(bot) )

       // IStrategyRunner run = crerateStrategyRun(config.strategyId, true)
       // backtestRunnerMap.put(config.id, run)

        Runnable task = {

            List<TimeRange> ranges = []

            Candle firstCandle = null
            Candle lastCandle = null

            // split into smaller date ranges
            Instant from = start.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES)
            Instant to = end.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES)

            Instant current = from
            while(current < to) {

                Instant nextTo = current.plus( 14, ChronoUnit.DAYS)

                if(nextTo > to) {
                    nextTo = to
                }

                ranges << new TimeRange(current, nextTo)
                current = nextTo.plus(1, ChronoUnit.MINUTES)
            }

            BacktestRun lock = new BacktestRun(bot.getId())
            lockMap.put(config.getId(), lock)

            ranges.each { range ->

                List<Candle> candles = candleStore.find( "1min", config.exchange, config.market, LocalDateTime.ofInstant(range.from, ZoneOffset.UTC), LocalDateTime.ofInstant(range.to, ZoneOffset.UTC))
                candles = CandleAggregator.aggregate(config.candleSize, candles)

                log.info("Processing ${candles.size()} candles of size ${config.candleSize} from ${range.from} to ${range.to}")

                candles.each { c ->

                    lock.lock()


                    c.botId = bot.getId()
                    c.backtestId = config.getId()

                    exchange.candle = c

                    CryptoMarket market = c.market
                    String currency  = market.getCurrency()
                    String asset = market.getAsset()
                    exchange.setTicker( c.close, currency, asset)

                    mixedCandelSizesChannel.send(MessageBuilder.withPayload( c ).build() )

                    if(firstCandle == null) {
                        firstCandle = c
                    }

                    lastCandle = c
                    lock.waitForUnlock() // wait till candle execution is complete
                }
            }

            // update USDT values!
            List usdtCandles = candleStore.find( "1min", config.exchange, "USDT-BTC", DateHelper.toLocalDateTime(lastCandle.start) ,DateHelper.toLocalDateTime(lastCandle.end))
           // usdtCandles = CandleAggregator.aggregate(config.candleSize, usdtCandles)

            if(usdtCandles) {
                Candle lastUsdt = usdtCandles.last()
                lastUsdt.period = bot.config.candleSize // fake the candle size..
                mixedCandelSizesChannel.send(MessageBuilder.withPayload( lastUsdt ).build() )
            }


            // save update for open positions..
            tradeBotManager.findAllOpenPosition(bot.getId()).each {
                positionRepository.save(it)
            }

            // calc hold result
            def startAmount = bot.config.backtest.startBalance / firstCandle.close
            def finalCurrency = startAmount * lastCandle.close

            log.info("Hold result: ${finalCurrency}")

            tradeBotManager.syncBanlance(bot)

            log.info("Backtest task: all candles done!")

        } as Runnable

        backtestTaskExecutor.execute(task)

        return config.getId()
        // ... loading
    }

    BacktestResult getBacktestResults(String id) {

        BacktestResult result = new BacktestResult()

        IStrategyRunner run = backtestRunnerMap.get(id)

        if(run) {
            result = run.getResult()
        }

        return result
    }

    IStrategyRunner crerateStrategyRun(TradeBot tradeBot) {

        IStrategyRunner run = ctx.getBean(CombinedStrategyRun.class)
        run.init(tradeBot)

        //Map paramMap = tradeBot.config

        run.strategies.addAll( createStrategies( tradeBot.config ))

        /*
        // configure strategies
        if(paramMap.dema) {
            DemaSettings demaSettings = new DemaSettings(paramMap.dema) // as DemaSettings

            if(demaSettings.enabled) {
                log.info("Adding Strategy DEMA settings: $demaSettings")
                run.strategies << new Dema(demaSettings)
            }
        }

        if(paramMap.macd) {
            MacdSettings settings = new MacdSettings(paramMap.macd)

            if(settings.enabled) {
                log.info("Adding Strategy MACD settings: $settings")
                run.strategies << new Macd(settings)
            }
        }

        if(paramMap.rsi) {
            RsiSettings settings = new RsiSettings(paramMap.rsi)

            if(settings.enabled) {
                log.info("Adding Strategy RSI settings: $settings")
                run.strategies << new Rsi(settings)
            }
        }
        */

        return run
    }

    List createStrategies(Config paramMap) {

        def strategies = []

        // configure strategies
        if(paramMap.dema) {
            DemaSettings demaSettings = paramMap.dema // as DemaSettings

            if(demaSettings.enabled) {
                log.info("Adding Strategy DEMA settings: $demaSettings")
                strategies << new Dema(demaSettings)
            }
        }

        if(paramMap.macd) {
            MacdSettings settings = paramMap.macd

            if(settings.enabled) {
                log.info("Adding Strategy MACD settings: $settings")
                strategies << new Macd(settings)
            }
        }

        if(paramMap.rsi) {
            RsiSettings settings = paramMap.rsi

            if(settings.enabled) {
                log.info("Adding Strategy RSI settings: $settings")
                strategies << new Rsi(settings)
            }
        }

        return strategies
    }

    IStrategyRunner crerateStrategyRun(Integer configId, boolean backtest) {

        TradeBot tradeBot = tradeBotManager.createNewBot(configId, backtest)

        IStrategyRunner run = ctx.getBean(CombinedStrategyRun.class)
        run.init(tradeBot)

        Map paramMap = tradeBot.config

        // configure strategies
        if(paramMap.dema) {
            DemaSettings demaSettings = new DemaSettings(paramMap.dema) // as DemaSettings

            if(demaSettings.enabled) {
                log.info("Adding Strategy DEMA settings: $demaSettings")
                run.strategies << new Dema(demaSettings)
            }

        }

        if(paramMap.macd) {
            MacdSettings settings = new MacdSettings(paramMap.macd)

            if(settings.enabled) {
                log.info("Adding Strategy MACD settings: $settings")
                run.strategies << new Macd(settings)
            }
        }

        if(paramMap.rsi) {
            RsiSettings settings = new RsiSettings(paramMap.rsi)

            if(settings.enabled) {
                log.info("Adding Strategy RSI settings: $settings")
                run.strategies << new Rsi(settings)
            }
        }


        return run
    }












}
