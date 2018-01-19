package tradingmaster.service

import groovy.json.JsonSlurper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service
import sun.font.ScriptRun
import tradingmaster.core.CandleAggregator
import tradingmaster.db.mariadb.MariaStrategyStore
import tradingmaster.model.*
import tradingmaster.strategy.*
import tradingmaster.strategy.runner.CombinedStrategyRun
import tradingmaster.strategy.runner.DefaultStrategyRun
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

    Map<String, ScriptRun> strategyMap = [:]

    Map<String, ScriptRun> backtestMap = [:]

    @Autowired
    TaskExecutor backtestTaskExecutor

    @Autowired
    PublishSubscribeChannel candelChannel1Minute

    @Autowired
    MariaStrategyStore strategyStore

    @Autowired
    ICandleStore candleStore

    @Autowired
    ApplicationContext ctx

    @Autowired
    TradeBotManager tradeBotManager

    @PostConstruct
    init() {
        candelChannel1Minute.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Candle c = message.getPayload()

        Map strategies = new HashMap(this.strategyMap)

        def matchingStrategies = strategies.values().findAll { ScriptStrategyRun s ->
            s.getMarket().equals( c.getMarket() )
        }

        log.info("Found ${matchingStrategies.size()} matching strategies!")

        matchingStrategies.each { ScriptStrategyRun s ->
           s.nextCandle(c)
        }
    }


    String startStrategy(StrategyRunConfig config) {

        log.info("Starting a new ScriptStrategy!")

        // ToDo... put a real portfolio here
        IPortfolio portfolio = ctx.getBean(TradeBot.class)
        //portfolio.init([:])


        ScriptStrategyRun run = crerateStrategyRun(config, portfolio, false)

        strategyMap.put(config.id, run)
    }

    String startBacktest(LocalDateTime start, LocalDateTime end, StrategyRunConfig config) {

        log.info("Starting a new Backtest with id ${config.id}. ${config.market} ${config.exchange}")

        CryptoMarket market =  new CryptoMarket(config.exchange, config.market)

        // TODO: pass TradeBot settings ...
        Map portfolioParams = [:]
        portfolioParams.assetName = market.getAsset()
        portfolioParams.currencyName = market.getCurrency()


        DefaultStrategyRun run = crerateStrategyRun(config, portfolioParams, true)
        backtestMap.put(config.id, run)

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

        StrategyRun run = backtestMap.get(id)

        if(run) {
            result = run.getResult()
        }

        return result
    }

    StrategyRun crerateStrategyRun(StrategyRunConfig config, Map paramMap, boolean backtest) {

        ScriptStrategy strategy = strategyStore.loadStrategyById(config.getStrategyId(), null)

        // TODO.. load real portfolio if no a backtest
        TradeBot tradeBot = ctx.getBean(TradeBot.class)

        IStrategyRunner run = null

        if(strategy.script.indexOf("function(candle, params, actions)") > -1) {

            // not supported anymore
            throw new RuntimeException("Scripts are not supported at the moment!")

//            paperPortfolioService.init(paramMap, tradeBot, backtest)
//            run = new ScriptStrategyRun(tradeBot, ctx.getBean(ActionBindings.class), backtest)
//            run.market = new CryptoMarket( config.exchange, config.market)
//
//            run.strategy = strategy
//            run.strategyParmas = config.strategyParams
//
//            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn")
//            engine.eval(strategy.getScript())
//
//            run.engine = engine

        } else {
            // config run

            paramMap = new JsonSlurper().parseText(strategy.getScript())

            config.strategyParams = paramMap
            config.exchange = paramMap.exchange
            //config.market = "${paramMap.currencyName}-${paramMap.assetName}"
            config.candleSize = CandleInterval.parse(paramMap.candleSize).getMinuteValue()
            config.warmup = paramMap.warmup

            tradeBotManager.init(paramMap, tradeBot, backtest)

            run = ctx.getBean(CombinedStrategyRun.class)
            run.init(tradeBot)

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


            // Todo: set candle size from config!
            //config.setCandleSize()


        }




        return run

    }












}
