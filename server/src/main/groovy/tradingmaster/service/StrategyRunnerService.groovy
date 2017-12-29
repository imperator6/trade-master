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
import tradingmaster.model.*

import javax.annotation.PostConstruct
import javax.script.*
import java.time.LocalDateTime

@Service
@Commons
class StrategyRunnerService implements  MessageHandler {

    Map<String, StrategyRun> strategyMap = [:]

    Map<String, StrategyRun> backtestMap = [:]

    @Autowired
    TaskExecutor backtestTaskExecutor

    @Autowired
    PublishSubscribeChannel candelChannel1Minute

    @Autowired
    IStrategyStore strategyStore

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

        def matchingStrategies = strategies.values().findAll { StrategyRun s ->
            s.getMarket().equals( c.getMarket() )
        }

        log.info("Found ${matchingStrategies.size()} matching strategies!")

        matchingStrategies.each { StrategyRun s ->
           s.nextCandle(c)
        }
    }


    String startStrategy(StrategyRunConfig config) {

        log.info("Starting a new Strategy!")

        // ToDo... put a real portfolio here
        IPortfolio portfolio = ctx.getBean(PaperPortfolio.class)
        //portfolio.init([:])


        StrategyRun run = crerateStrategyRun(config, portfolio, false)

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

        StrategyRun run = crerateStrategyRun(config, portfolio, true)
        backtestMap.put(config.id, run)

        Runnable task = {

            List<Candle> candles = candleStore.find( "1min", config.exchange, config.market, start, end)

            candles = CandleAggregator.aggregate(config.candleSize, candles)

            // TODO: split in chuncks?
            candles.each { c ->
                run.nextCandle(c)
            }

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

            result.complete = run.backtestComplete

            result.changes = new ArrayList(run.portfolioChanges)

            result.portfolio = run.portfolio

            result.signals = new ArrayList(run.signals)
        }

        return result
    }

    StrategyRun crerateStrategyRun(StrategyRunConfig config, IPortfolio portfolio, boolean backtest) {

        Strategy strategy = strategyStore.loadStrategyById(config.getStrategyId(), null)

        StrategyRun run = new StrategyRun(new ActionBindings(), backtest)
        run.market = new CryptoMarket( config.exchange, config.market)
        run.strategy = strategy
        run.strategyParmas = config.strategyParams
        run.portfolio = portfolio

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn")
        engine.eval(strategy.getScript())

        run.engine = engine

        return run

    }


    class ActionBindings {

        Candle candle
        StrategyRun run

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





    class StrategyRun {

        Strategy strategy
        Map strategyParmas
        IMarket market
        IPortfolio portfolio

        ActionBindings actionBindings

        List<TradingSignal> signals = []

        List<PortfolioChange> portfolioChanges = []

        ScriptEngine engine

        boolean backtest = false
        boolean backtestComplete = false

        boolean firstCandle = true

        Candle prevCandle = null

        StrategyRun(ActionBindings ab, boolean backtest) {
            this.actionBindings = ab
            this.actionBindings.run = this
            this.backtest = backtest
        }

        void nextCandle(Candle c) {

            // update candle in bindings
            this.actionBindings.candle = c

            if(firstCandle) {
                actionBindings.onFirstCandle(c)
                firstCandle = false
            }

            Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE)
            b.put("market", market)
            //b.put( "candle", c )

            Invocable invocable = (Invocable) engine

            Object result  = invocable.invokeFunction("nextCandle", c, strategyParmas , this.actionBindings )

            this.prevCandle = c
        }

        void close() {
            this.backtestComplete = true
            actionBindings.onLastCandle(this.prevCandle)
        }
    }


}
