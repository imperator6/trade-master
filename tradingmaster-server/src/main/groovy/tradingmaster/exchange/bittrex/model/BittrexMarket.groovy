package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty

class BittrexMarket {

    @JsonProperty("BaseCurrency")
    String currency

    @JsonProperty("MarketCurrency")
    String asset

    @JsonProperty("MinTradeSize")
    Double minSize

    @JsonProperty("IsActive")
    Boolean isActive

    @JsonProperty("MarketName")
    String marketName

    @JsonProperty("BaseCurrencyLong")
    String currencyName

    @JsonProperty("MarketCurrencyLong")
    String assetName
}
