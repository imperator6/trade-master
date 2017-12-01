package tradingmaster.model

import groovy.transform.ToString


@ToString
class CryptoMarket extends DefaultMarket {

    String currency
    String asset

    CryptoMarket(String exchange, String market) {
        this( exchange, market.split("-")[0], market.split("-")[1])
    }

    CryptoMarket(String exchange, String currency, String asset) {
        super( exchange, "${currency.toUpperCase()}-${asset.toUpperCase()}".toString())
        this.asset = asset.toUpperCase()
        this.currency = currency.toUpperCase()
    }


}
