package tradingmaster.exchange.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import tradingmaster.model.IOrder

class BinanceOrder implements IOrder {

    // side, timeInForce, status

    @JsonProperty("orderId")
    String id

    @JsonProperty("clientOrderId")
    String clientOrderId

    @JsonProperty("symbol")
    String symbol

    String getMarket() {
        if(symbol) {
            return symbol.substring(3) + "-" + symbol.substring(0,3)
        }
        null
    }



    @JsonProperty("price")
    BigDecimal price

    @JsonProperty("origQty")
    BigDecimal quantity

    @JsonProperty("executedQty")
    BigDecimal executedQty

    @JsonProperty("status")
    String status

    @JsonProperty("side")
    String side

    @JsonProperty("timeInForce")
    String timeInForce

    @JsonProperty("QuantityRemaining")
    BigDecimal getQuantityRemaining() {
        if(quantity && executedQty) {
            return quantity - executedQty
        }
        return 0
    }

    @JsonProperty("type")
    String orderType

    @JsonProperty("time")
    Long time

    Date getTimeStamp() {
        return new Date(time)
    }

    @JsonProperty("isWorking")
    boolean isWorking

    Boolean isOpen() {
        return  getQuantityRemaining() > 0.0
    }

    BigDecimal getCommissionPaid() {
        return 0
    }

    BigDecimal getPricePerUnit() {
        return price
    }

    Date getCloseDate() {
        return new Date(time)
    }


/*
    *
    *
    * String getId()

    String getMarket()

    Date getTimeStamp()

    Boolean isOpen()

    Date getCloseDate()

    BigDecimal getCommissionPaid()

    BigDecimal getPrice()

    BigDecimal getPricePerUnit()

    BigDecimal getQuantity()

    BigDecimal getQuantityRemaining()
    *
    *
    * */

}
