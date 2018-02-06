package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.ITrade

@ToString
class BinanceTrade implements ITrade {

    @JsonProperty("a")
    String extId

    @JsonProperty("q")
    BigDecimal quantity

    @JsonProperty("p")
    BigDecimal price

    @JsonProperty("T")
    Date date
}
