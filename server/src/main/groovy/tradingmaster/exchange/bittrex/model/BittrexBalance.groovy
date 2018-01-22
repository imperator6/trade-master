package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.model.IBalance

class BittrexBalance implements IBalance {

    @JsonProperty("Currency")
    String currency

    @JsonProperty("Balance")
    BigDecimal value = 0

    @JsonProperty("Available")
    BigDecimal available = 0

    @JsonProperty("Pending")
    BigDecimal pending =  0

    @JsonProperty("CryptoAddress")
    String cryptoAddress = ""

}
