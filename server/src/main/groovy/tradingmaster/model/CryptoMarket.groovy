package tradingmaster.model

import groovy.transform.ToString


@ToString
class CryptoMarket extends DefaultMarket {

    String currency
    String asset

    CryptoMarket(String exchange, String currency, String asset) {
        super( exchange, "${currency}-${asset}".toString())
        this.asset = asset
        this.currency = currency
    }


}
