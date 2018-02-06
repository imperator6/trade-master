package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.ITrade

@ToString
class BittrexTrade implements ITrade {

    @JsonProperty("Id")
    String extId

    @JsonProperty("Quantity")
    BigDecimal quantity

    @JsonProperty("Price")
    BigDecimal price

    @JsonProperty("TimeStamp")
    Date date
}
