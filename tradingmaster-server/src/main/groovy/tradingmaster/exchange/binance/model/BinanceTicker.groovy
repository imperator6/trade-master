package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.exchange.binance.BinanceHelper
import tradingmaster.model.ITicker

class BinanceTicker implements ITicker {

    @JsonProperty("symbol")
    String symbol

    String getMarket() {
        return BinanceHelper.convertSymbolToMarket(symbol)
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
