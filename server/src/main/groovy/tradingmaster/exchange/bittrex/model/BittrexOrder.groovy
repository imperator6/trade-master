package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.IOrder

@ToString(includeNames=true)
class BittrexOrder implements IOrder {

    @JsonProperty("OrderUuid")
    String id

    @JsonProperty("AccountId")
    String accountId

    @JsonProperty("Exchange")
    String market

    @JsonProperty("OrderType")
    String orderType

    @JsonProperty("Quantity")
    BigDecimal quantity

    @JsonProperty("QuantityRemaining")
    BigDecimal quantityRemaining

    @JsonProperty("Limit")
    BigDecimal limit

    @JsonProperty("Reserved")
    BigDecimal reserved

    @JsonProperty("ReserveRemaining")
    BigDecimal reserveRemaining

    @JsonProperty("CommissionReserved")
    BigDecimal commissionReserved

    @JsonProperty("CommissionReserveRemaining")
    BigDecimal commissionReserveRemaining

    @JsonProperty("CommissionPaid")
    BigDecimal commissionPaid

    @JsonProperty("Price")
    BigDecimal price

    @JsonProperty("PricePerUnit")
    BigDecimal pricePerUnit

    @JsonProperty("Opened")
    Date openDate

    @JsonProperty("Closed")
    Date closeDate

    @JsonProperty("IsOpen")
    Boolean open

    @JsonProperty("Sentinel")
    String sentinel

    @JsonProperty("CancelInitiated")
    Boolean cancelInitiated

    @JsonProperty("ImmediateOrCancel")
    Boolean immediateOrCancel

    @JsonProperty("IsConditional")
    Boolean isConditional


    @JsonProperty("Condition")
    String condition

    @JsonProperty("ConditionTarget")
    String conditionTarget

    @Override
    Date getTimeStamp() {
        return openDate
    }

    Boolean isOpen() {
        return open
    }
}
