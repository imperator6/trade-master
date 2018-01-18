package tradingmaster.strategy.runner

import groovy.util.logging.Commons
import tradingmaster.model.Candle
import tradingmaster.model.IPortfolio
import tradingmaster.model.StrategyRun
import tradingmaster.strategy.Strategy
import tradingmaster.strategy.StrategyResult

@Commons
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
            StrategyResult r = it.execute( c )
            results.put(it.getName(), r )
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
