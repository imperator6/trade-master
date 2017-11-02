package tradingmaster.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includeFields=true)
class DefaultMarket implements IMarket {

    String name
    String exchange // exchange or broker

    DefaultMarket(exchange, name) {
        this.exchange = exchange
        this.name = name
    }
}
