package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service
import tradingmaster.core.CandleAggregator
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.*
import tradingmaster.strategy.*
import tradingmaster.strategy.runner.CombinedStrategyRun
import tradingmaster.strategy.runner.IStrategyRunner
import tradingmaster.strategy.runner.ScriptStrategyRun

import javax.annotation.PostConstruct
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
@Commons
class StrategyRunnerService implements  MessageHandler {

    Map<String, IStrategyRunner> runnerMap = [:]

    Map<String, IStrategyRunner> backtestRunnerMap = [:]

    @Autowired
    TaskExecutor backtestTaskExecutor

    @Autowired
    PublishSubscribeChannel candelChannel1Minute


    @Autowired
    ICandleStore candleStore

    @Autowired
    ApplicationContext ctx

    @Autowired
    TradeBotManager tradeBotManager

    @PostConstruct
    init() {
        //candelChannel1Minute.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        // Not active check init()
       Candle c = message.getPayload()

        Map strategies = new HashMap(this.runnerMap)

        def matchingStrategies = strategies.values().findAll { ScriptStrategyRun s ->
            s.getMarket().equals( c.getMarket() )
        }

        matchingStrategies.each { ScriptStrategyRun s ->
           s.nextCandle(c)
        }
    }


    String startStrategy(Integer strategyId) {

        log.info("Starting a new ScriptStrategy!")

        crerateStrategyRun(strategyId, false)

        //runnerMap.put(config.id, run)
    }

    String startBacktest(LocalDateTime start, LocalDateTime end, StrategyRunConfig config) {

        log.info("Starting a new Backtest for strategyId/config ${config.strategyId}.")

        IStrategyRunner run = crerateStrategyRun(config.strategyId, true)
        backtestRunnerMap.put(config.id, run)

        Runnable task = {

            List<TimeRange> ranges = []

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

            ranges.each { range ->

                List<Candle> candles = candleStore.find( "1min", config.exchange, config.market, LocalDateTime.ofInstant(range.from, ZoneOffset.UTC), LocalDateTime.ofInstant(range.to, ZoneOffset.UTC))
                candles = CandleAggregator.aggregate(config.candleSize, candles)

                log.info("Processing ${candles.size()} candles of size ${config.candleSize} from ${range.from} to ${range.to}")


                candles.each { c ->
                    run.nextCandle(c)
                }
            }

            log.info("Backtest task: all candles done!")

            run.close()

            // Todo... persit results?
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
