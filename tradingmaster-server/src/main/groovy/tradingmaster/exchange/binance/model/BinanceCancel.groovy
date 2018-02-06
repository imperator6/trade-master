package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.exchange.binance.BinanceHelper

class BinanceCancel {

    @JsonProperty("orderId")
    String id

    @JsonProperty("clientOrderId")
    String clientOrderId

    @JsonProperty("origClientOrderId")
    String origClientOrderId

    @JsonProperty("symbol")
    String symbol

    String getMarket() {
        return BinanceHelper.convertSymbolToMarket(symbol)
    }
}
