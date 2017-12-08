package tradingmaster.model

import groovy.transform.ToString

@ToString
class Strategy implements IStrategy {

    String id = ""
    String name = ""
    String language = ""
    String script = ""
    String version = ""

    Strategy() {
    }

    Strategy(String script, String language) {
        this.language = language
        this.script = script
    }
}
