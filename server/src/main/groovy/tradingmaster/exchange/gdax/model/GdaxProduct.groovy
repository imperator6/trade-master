package tradingmaster.exchange.gdax.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString

@ToString
class GdaxProduct {

    String id

    @JsonProperty("base_currency")
    String currency

    @JsonProperty("quote_currency")
    String asset

    @JsonProperty("base_min_size")
    Double minSize

    @JsonProperty("base_max_size")
    Double maxSize

    @JsonProperty("quote_increment")
    Double increment


}
