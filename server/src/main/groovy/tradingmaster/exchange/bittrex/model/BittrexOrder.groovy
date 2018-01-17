package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString

@ToString
class BittrexOrder  {

    @JsonProperty("OrderUuid")
    String id

    @JsonProperty("Exchange")
    String market

    @JsonProperty("TimeStamp")
    Date date

    @JsonProperty("OrderType")
    String orderType

    @JsonProperty("Limit")
    BigDecimal limit

    @JsonProperty("QuantityRemaining")
    BigDecimal quantityRemaining

    @JsonProperty("Quantity")
    BigDecimal quantity

    @JsonProperty("Commission")
    BigDecimal commission

    @JsonProperty("Price")
    BigDecimal price

    @JsonProperty("PricePerUnit")
    BigDecimal pricePerUnit

    @JsonProperty("IsConditional")
    Boolean conditional

    @JsonProperty("Condition")
    String condition

    @JsonProperty("ConditionTarget")
    String conditionTarget

    @JsonProperty("ImmediateOrCancel")
    Boolean immediateOrCancel

}
