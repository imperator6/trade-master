package tradingmaster.model

import groovy.transform.ToString
import groovy.transform.TupleConstructor


@ToString
class StrategyScript implements IStrategyScript {

    String language = ""
    String script = ""

     StrategyScript(String script, String language) {
        this.language = language
        this.script = script
    }
}
