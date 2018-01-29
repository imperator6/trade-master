package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.model.ITicker

class BinanceTicker implements ITicker {

    @JsonProperty("symbol")
    String symbol

    String getMarket() {
        if(symbol) {
            return symbol.substring(3) + "-" + symbol.substring(0,3)
        }
        null
    }

    @JsonProperty("bidPrice")
    BigDecimal bid

    @JsonProperty("askPrice")
    BigDecimal ask

    @JsonProperty("bidQty")
    BigDecimal bidQty

    @JsonProperty("askQty")
    BigDecimal askQty

}
