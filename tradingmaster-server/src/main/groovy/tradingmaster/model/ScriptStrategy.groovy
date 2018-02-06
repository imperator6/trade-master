package tradingmaster.model

import groovy.transform.ToString

@ToString
class ScriptStrategy implements IScriptStrategy {

    Integer id = null
    String name = ""
    String language = ""
    String script = ""
    BigDecimal version = 1

    ScriptStrategy() {
    }

    ScriptStrategy(String script, String language) {
        this.language = language
        this.script = script
    }
}
