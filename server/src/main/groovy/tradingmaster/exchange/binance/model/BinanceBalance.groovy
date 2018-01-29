package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.model.IBalance

class BinanceBalance implements IBalance {

    @JsonProperty("asset")
    String currency

    @JsonProperty("free")
    BigDecimal available = 0

    @JsonProperty("locked")
    BigDecimal pending =  0

    @JsonProperty("CryptoAddress")
    String cryptoAddress = ""

    BigDecimal getValue() {
        return available + pending
    }

}
