package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty

class BittrexBuySell {

    @JsonProperty("uuid")
    String orderId

}
