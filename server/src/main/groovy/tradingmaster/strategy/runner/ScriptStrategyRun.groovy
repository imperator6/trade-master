package tradingmaster.strategy.runner

import tradingmaster.model.Candle
import tradingmaster.model.IPortfolio
import tradingmaster.model.ScriptStrategy
import tradingmaster.model.StrategyRun
import tradingmaster.model.TradeBot

import javax.script.Bindings
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngine

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

    @Override
    void init(TradeBot bot) {

    }
}
