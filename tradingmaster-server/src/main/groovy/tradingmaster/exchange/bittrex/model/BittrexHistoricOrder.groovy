package tradingmaster.exchange.bittrex.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString
import tradingmaster.model.IOrder

@ToString
class BittrexHistoricOrder implements IOrder {


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

    @Override
    Date getTimeStamp() {
        return date
    }

    @Override
    Boolean isOpen() {
        return false
    }

    @Override
    Date getCloseDate() {
        return date
    }

    @Override
    BigDecimal getCommissionPaid() {
        return commission
    }

    @Override
    String getBuySell() {
        if(this.orderType != null && this.orderType.toLowerCase().indexOf("sell") > -1) {
            return "sell"
        }
        return "buy"
    }
}
