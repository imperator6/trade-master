package tradingmaster.model

import groovy.transform.ToString
import groovy.transform.TupleConstructor

import javax.script.ScriptEngine

@ToString
@TupleConstructor
class StrategyConfig {

    IMarket market

    IPortfolio portfolio

    IScriptStrategy script

    ScriptEngine scriptHandler

}
