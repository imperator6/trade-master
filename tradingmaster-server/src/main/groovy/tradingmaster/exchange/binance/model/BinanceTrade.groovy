package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.ITrade

@ToString
class BinanceTrade implements ITrade {

    @JsonProperty("id")
    String extId

    @JsonProperty("qty")
    BigDecimal quantity

    @JsonProperty("price")
    BigDecimal price

    @JsonProperty("time")
    Date date
}
