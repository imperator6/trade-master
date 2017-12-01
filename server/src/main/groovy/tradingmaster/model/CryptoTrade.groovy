package tradingmaster.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString

@ToString
class CryptoTrade implements ITrade{

    @JsonProperty("Id")
    String extId

    @JsonProperty("Quantity")
    BigDecimal quantity

    @JsonProperty("Price")
    BigDecimal price

    @JsonProperty("TimeStamp")
    Date date
}
