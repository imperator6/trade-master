package tradingmaster.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Commons


@ToString
@EqualsAndHashCode
@Commons
class CryptoMarket extends DefaultMarket implements Serializable {

    String currency
    String asset

    CryptoMarket() {
    }

    CryptoMarket(String exchange, String market) {
        this( exchange, market.split("-")[0], market.split("-")[1])
    }

    CryptoMarket(String exchange, String currency, String asset) {
        super( exchange, "${currency.toUpperCase()}-${asset.toUpperCase()}".toString())
        this.asset = asset.toUpperCase()
        this.currency = currency.toUpperCase()
    }

    String getPair() {
        return "${currency}-${asset}".toString()
    }


}
