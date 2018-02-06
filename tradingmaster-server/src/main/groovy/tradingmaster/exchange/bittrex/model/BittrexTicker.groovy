package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.model.ITicker

class BittrexTicker implements ITicker {

    String market

    @JsonProperty("Bid")
    BigDecimal bid

    @JsonProperty("Ask")
    BigDecimal ask

    @JsonProperty("Last")
    BigDecimal last

}
