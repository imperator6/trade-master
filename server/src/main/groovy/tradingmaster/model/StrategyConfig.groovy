package tradingmaster.model

import groovy.transform.ToString
import groovy.transform.TupleConstructor

import javax.script.Invocable
import javax.script.ScriptEngine

@ToString
@TupleConstructor
class StrategyConfig {

    IMarket market

    IPortfolio portfolio

    IStrategyScript script

    ScriptEngine scriptHandler

}
