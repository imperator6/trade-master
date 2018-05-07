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
import tradingmaster.db.StrategyResultRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.entity.json.Config
import tradingmaster.exchange.paper.PaperExchange
import tradingmaster.model.*
import tradingmaster.strategy.*
import tradingmaster.strategy.runner.CombinedStrategyRun
import tradingmaster.strategy.runner.IStrategyRunner
import tradingmaster.strategy.runner.StrategyByMarketCache
import tradingmaster.util.DateHelper
import tradingmaster.util.NumberHelper

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
    PositionUpdateHandler positionUpdateHandler

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

    @Autowired
    StrategyResultRepository strategyResultRepository

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
            if("signalComplete".equalsIgnoreCase(msg.action)) {
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

        if(bot.config.backtest.enabled) {

            log.info("Starting a new Backtest for strategyId/config ${config.strategyId}.")

        } else {
            log.error("Can't backtest bot with id ${config.getBotId()}. As flag backtest is false")
        }

        CryptoMarket cm = new CryptoMarket(bot.config.exchange, config.market)
        bot.config.baseCurrency = cm.getCurrency()
        bot.baseCurrency = bot.config.baseCurrency
        bot.config.backtest.market = config.market
        bot.config.backtest.startDate = DateHelper.toDate(start)
        bot.config.backtest.endDate = DateHelper.toDate(end)

        // update candle size
        bot.config.candleSize = config.getCandleSize()
        //bot.config.exchange = 'PaperExchange'

        // calc start balance
        def startBalance = bot.config.amountPerOrder * bot.config.maxOpenPositions * bot.config.assetFilter.allowed.size()
        bot.config.backtest.startBalance = startBalance

        PaperExchange exchange = bot.getPaperExchange()
        exchange.config = bot.config
        exchange.setBalance((String) bot.config.baseCurrency, (BigDecimal) bot.config.backtest.startBalance)

        bot.setStartBalance( (BigDecimal) bot.config.backtest.startBalance )
        bot.setTotalBaseCurrencyValue( (BigDecimal) bot.config.backtest.startBalance )
        bot.setResult( 0.0 )
        bot.setTotalBalanceDollar( null )
        bot.setFxDollar( null )

        tradeBotManager.save(bot)

        // delete all exsisting positions
        tradeBotManager.removeAllPositions(bot.getId())
        signalRepository.deleteByBotId(bot.getId())
        strategyResultRepository.deleteByBotId(bot.getId())


        IStrategyRunner strategyRunner = ctx.getBean(StrategyByMarketCache.class)
        strategyRunner.init(bot)

        bot.setStrategyRunner( strategyRunner )

       // IStrategyRunner run = crerateStrategyRun(config.strategyId, true)
       // backtestRunnerMap.put(config.id, run)

        Runnable task = {

            List<TimeRange> ranges = []

            Map firstCandleCache = [:]
            Map lastCandleCache = [:]

           // Candle firstCandle = null
           // Candle lastCandle = null

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


            List<String> markets = bot.config.assetFilter.allowed.collect { String asset ->
                String market = "${bot.baseCurrency}-${asset}" // config.market
                return market
            }

            ranges.each { range ->

                List<Candle> candles = getCandlesForAllMarkets( range, bot.config.exchange, markets, bot.config.candleSize )

                log.info("Processing ${candles.size()} candles of size ${config.candleSize} from ${range.from} to ${range.to}")

                candles.each { c ->

                    lock.lock()

                    c.botId = bot.getId()
                    c.backtestId = config.getId()

                    exchange.candle = c

                    exchange.setTicker( c.close, c.market.getCurrency(),  c.market.getAsset())

                    mixedCandelSizesChannel.send(MessageBuilder.withPayload( c ).build() )

                    Candle first = firstCandleCache.get(c.getMarket().getPair())

                    if(first == null) {
                        firstCandleCache.put(c.getMarket().getPair(), c)
                    }

                    lastCandleCache.put(c.getMarket().getPair(), c)
                    lock.waitForUnlock() // wait till candle execution is complete
                }
            }

            // update USDT values!
            List usdtCandles = candleStore.find( "1min", config.exchange, "USDT-BTC", end.minusDays(1), end)
            // usdtCandles = CandleAggregator.aggregate(config.candleSize, usdtCandles)

            if(usdtCandles) {
                Candle lastUsdt = usdtCandles.last()
                lastUsdt.period = bot.config.candleSize // fake the candle size..
                mixedCandelSizesChannel.send(MessageBuilder.withPayload( lastUsdt ).build() )
            }

            lastCandleCache.each { String market, Candle lastCandle ->

                // not sure why this is needed?
                positionUpdateHandler.processBotUpdate(bot ,lastCandle)
            }


            // save update for open positions..
            tradeBotManager.findAllOpenPosition(bot.getId()).each {
                positionRepository.save(it)
            }

            def totalHoldResult = 0.0
            def totalStart = 0.0

            log.info("------------------ Hold Result -------------------------")


            // calc hold result
            lastCandleCache.each { String market, Candle lastCandle  ->

                Candle firstCandle = firstCandleCache.get(market)

                def startAmount = bot.config.amountPerOrder / firstCandle.close
                def finalCurrency = startAmount * lastCandle.close

                totalStart += bot.config.amountPerOrder
                totalHoldResult += finalCurrency
                log.info("---    ${market}: ${NumberHelper.formatNumber(finalCurrency)} ${bot.config.baseCurrency}")
            }

            def totalHoldPrc = NumberHelper.formatNumber( NumberHelper.xPercentFromBase( totalStart, totalHoldResult) )

            log.info("--------------------------------------------------------")
            log.info("---    TOTAL: ${NumberHelper.formatNumber(totalHoldResult)} ${bot.config.baseCurrency} (${totalHoldPrc}%)")
            log.info("--------------------------------------------------------")

            tradeBotManager.syncBanlance(bot)

            BacktestMessage msg = new BacktestMessage()
            msg.backtestId = config.getId()
            msg.action = "backtestComplete"

            // no actions --> notify strategy runner to fire next candle
            backtestChannel.send( MessageBuilder.withPayload(msg).build()  )

            log.info("Backtest task: all candles done!")

        } as Runnable

        backtestTaskExecutor.execute(task)

        return config.getId()
        // ... loading
    }

    List<Candle> getCandlesForAllMarkets(TimeRange range, String exchange, List<String> markets, candleSize) {

        List<Candle> result = []

        markets.each { market ->
            List<Candle> candles = candleStore.find( "1min", exchange, market, LocalDateTime.ofInstant(range.from, ZoneOffset.UTC), LocalDateTime.ofInstant(range.to, ZoneOffset.UTC))
            result.addAll(CandleAggregator.aggregate(candleSize, candles)  )
        }

        return result.sort { a,b -> a.end <=> b.end }
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
