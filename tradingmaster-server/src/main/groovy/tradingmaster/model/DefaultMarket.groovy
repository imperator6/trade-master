package tradingmaster.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includeFields=true)
class DefaultMarket implements IMarket {

    String name
    String exchange // tradingmaster.exchange or broker

    DefaultMarket() {
    }

    DefaultMarket(exchange, name) {
        this.exchange = exchange.capitalize()
        this.name = name
    }
}
