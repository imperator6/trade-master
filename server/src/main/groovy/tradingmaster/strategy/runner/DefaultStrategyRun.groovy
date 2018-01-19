package tradingmaster.strategy.runner

import tradingmaster.model.BacktestResult
import tradingmaster.model.Candle
import tradingmaster.model.IMarket
import tradingmaster.model.IPortfolio
import tradingmaster.model.PortfolioChange
import tradingmaster.model.TradingSignal

abstract class DefaultStrategyRun implements IStrategyRunner {

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