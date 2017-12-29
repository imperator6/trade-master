package tradingmaster.exchange.gdax.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.ITrade

@ToString
class GdaxTrade implements ITrade {

    @JsonProperty("trade_id")
    String extId

    @JsonProperty("size")
    BigDecimal quantity

    @JsonProperty("price")
    BigDecimal price

    @JsonProperty("time")
    Date date
}
