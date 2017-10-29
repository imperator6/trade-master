package tradingmaster.exchange.bittrex

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.ITrade

@ToString
class BittrexTrade implements ITrade{


    @JsonProperty("Id")
    String extId

    @JsonProperty("Quantity")
    Double quantity

    @JsonProperty("Price")
    BigDecimal price

    @JsonProperty("TimeStamp")
    Date date
}
