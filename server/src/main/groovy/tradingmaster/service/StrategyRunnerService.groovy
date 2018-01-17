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

import javax.annotation.PostConstruct
import javax.script.*
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
    PaperPortfolioService paperPortfolioService

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
        IPortfolio portfolio = ctx.getBean(PaperPortfolio.class)
        //portfolio.init([:])


        ScriptStrategyRun run = crerateStrategyRun(config, portfolio, false)

        strategyMap.put(config.id, run)
    }

    String startBacktest(LocalDateTime start, LocalDateTime end, StrategyRunConfig config) {

        log.info("Starting a new Backtest with id ${config.id}. ${config.market} ${config.exchange}")

        CryptoMarket market =  new CryptoMarket(config.exchange, config.market)

        // TODO: pass PaperPortfolio settings ...
        Map portfolioParams = [:]
        portfolioParams.assetName = market.getAsset()
        portfolioParams.currencyName = market.getCurrency()

        PaperPortfolio portfolio = ctx.getBean(PaperPortfolio.class)
        paperPortfolioService.init(portfolioParams, portfolio)

        DefaultStrategyRun run = crerateStrategyRun(config, portfolio, true)
        backtestMap.put(config.id, run)

        Runnable task = {

            List<TimeRange> ranges = []

            // split into smaller date ranges
            Instant from = start.toInstant(ZoneOffset.UTC)
            Instant to = end.toInstant(ZoneOffset.UTC)

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

                log.info("Processing ${candles.size()} candles from ${range.from} to ${range.to}")


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

    StrategyRun crerateStrategyRun(StrategyRunConfig config, IPortfolio portfolio, boolean backtest) {

        ScriptStrategy strategy = strategyStore.loadStrategyById(config.getStrategyId(), null)

        StrategyRun run = null

        if(strategy.script.indexOf("function(candle, params, actions)") > -1) {

            run = new ScriptStrategyRun(portfolio, new ActionBindings(), backtest)
            run.market = new CryptoMarket( config.exchange, config.market)

            run.strategy = strategy
            run.strategyParmas = config.strategyParams

            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn")
            engine.eval(strategy.getScript())

            run.engine = engine

        } else {
            // config run

            def c = new JsonSlurper().parseText(strategy.getScript())

            run = new CombinedStrategyRun(portfolio, new ActionBindings(), backtest)
            run.market = new CryptoMarket( config.exchange, config.market)

            if(c.dema) {
                DemaSettings demaSettings = new DemaSettings(c.dema) // as DemaSettings

                if(demaSettings.enabled)
                    run.strategies << new Dema(demaSettings)
            }

            if(c.macd) {
                MacdSettings settings = new MacdSettings(c.macd)

                if(settings.enabled)
                    run.strategies << new Macd(settings)
            }



            // Todo: set candle size from config!
            //config.setCandleSize()




        }


        return run

    }


    class ActionBindings {

        Candle candle
        DefaultStrategyRun run

        ActionBindings() {
        }

        void buy() {
            this.buy(null, null)
        }

        void sell() {
            this.sell(null, null)
        }

        void buy(Number price, Number amount) {
            if(price == null)
                price = candle.close

            TradingSignal signal =  new TradingSignal()
            signal.date = candle.end
            signal.type = BuySell.BUY.getName()
            signal.value = price

            run.signals << signal

            if(run.backtest) {
                run.portfolioChanges << paperPortfolioService.buy(price, amount, candle, run.portfolio)
            } else {
                // no backtest real portfolio action
                log.warn("Not implemented!")
            }

        }

        void sell(Number price, Number amount) {
            if(price == null)
                price = candle.close

            TradingSignal signal =  new TradingSignal()
            signal.date = candle.end
            signal.type = BuySell.SELL.getName()
            signal.value = price

            run.signals << signal

            if(run.backtest) {
                run.portfolioChanges << paperPortfolioService.sell(price, amount, candle, run.portfolio)
            } else {
              // no backtest real portfolio action
                log.warn("Not implemented!")
            }
        }

        void onFirstCandle(Candle c) {
            if(run.backtest) {
               paperPortfolioService.onFirstCandle(c, run.portfolio)
            }
        }

        void onLastCandle(Candle c) {
            if(run.backtest) {
                paperPortfolioService.onLastCandle(c, run.portfolio)
            }
        }

    }

    abstract class DefaultStrategyRun {

        boolean backtest = false
        boolean backtestComplete = false

        IMarket market

        IPortfolio portfolio

        List<TradingSignal> signals = []

        List<PortfolioChange> portfolioChanges = []

        ActionBindings actionBindings

        boolean firstCandle = true

        Candle prevCandle = null

        void nextCandle(Candle c) {

            // update candle in bindings
            this.actionBindings.candle = c

            if(firstCandle) {
                actionBindings.onFirstCandle(c)
                firstCandle = false
            }

            run(c)

            this.prevCandle = c
        }

        abstract void run(Candle c)

        void close() {
            this.backtestComplete = true
            actionBindings.onLastCandle(this.prevCandle)
        }

        BacktestResult getResult() {

            BacktestResult res = new BacktestResult()

            res.complete = this.backtestComplete

            res.changes = new ArrayList(this.portfolioChanges)

            res.portfolio = this.portfolio

            res.signals = new ArrayList(this.signals)

            return res
        }
    }


    class CombinedStrategyRun extends DefaultStrategyRun implements StrategyRun {

        List<Strategy> strategies = []

        StrategyResult prevActionResult = StrategyResult.NONE

        CombinedStrategyRun(IPortfolio portfolio, ActionBindings ab, boolean backtest) {
            this.actionBindings = ab
            this.actionBindings.run = this
            this.portfolio = portfolio
            this.backtest = backtest
        }

        void run(Candle c) {

            Map<String, StrategyResult> results = [:]

            strategies.each {
                results.put(it.getName(), it.execute( c ))
            }

            boolean goLong = results.values().findAll { it == StrategyResult.LONG }.size() == results.size()

            boolean goShort = results.values().findAll { it == StrategyResult.SHORT }.size() == results.size()

            if(goLong && prevActionResult != StrategyResult.LONG) {

                actionBindings.buy()
                prevActionResult = StrategyResult.LONG
            }

            if(goShort && prevActionResult != StrategyResult.SHORT) {

                actionBindings.sell()
                prevActionResult = StrategyResult.SHORT
            }
        }
    }


    class ScriptStrategyRun extends DefaultStrategyRun implements StrategyRun {

        ScriptStrategy strategy
        Map strategyParmas

        ScriptEngine engine

        ScriptStrategyRun(IPortfolio portfolio, ActionBindings ab, boolean backtest) {
            this.actionBindings = ab
            this.actionBindings.run = this
            this.portfolio = portfolio
            this.backtest = backtest
        }

        void run(Candle c) {

            Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE)
            b.put("market", market)
            //b.put( "candle", c )

            Invocable invocable = (Invocable) engine

            Object result  = invocable.invokeFunction("nextCandle", c, strategyParmas , this.actionBindings )

        }
    }

}
