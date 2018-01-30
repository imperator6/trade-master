package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty

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
        if(symbol) {
            return symbol.substring(3) + "-" + symbol.substring(0,3)
        }
        null
    }
}
