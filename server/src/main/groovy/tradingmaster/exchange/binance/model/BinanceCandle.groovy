package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty

class BinanceCandle {

    @JsonProperty(index=0)
    Long openTime

    @JsonProperty(index=1)
    Double open

    @JsonProperty(index=2)
    Double high

    @JsonProperty(index=3)
    Double low

    @JsonProperty(index=4)
    Double close

    @JsonProperty(index=5)
    Double volume

    @JsonProperty(index=6)
    Long closeTime

    @JsonProperty(index=7)
    Double quoteAsset

    @JsonProperty(index=8)
    Integer tradeCount


}
