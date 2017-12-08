package tradingmaster.model

import groovy.transform.ToString

@ToString
class Strategy implements IStrategy {

    BigDecimal id = ""
    String name = ""
    String language = ""
    String script = ""
    BigDecimal version = ""

    Strategy() {
    }

    Strategy(String script, String language) {
        this.language = language
        this.script = script
    }
}
