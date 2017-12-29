package tradingmaster.model

import groovy.transform.ToString

@ToString
class Strategy implements IStrategy {

    BigDecimal id = null
    String name = ""
    String language = ""
    String script = ""
    BigDecimal version = 1

    Strategy() {
    }

    Strategy(String script, String language) {
        this.language = language
        this.script = script
    }
}
