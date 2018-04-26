package tradingmaster.model

class Order implements IOrder {

    String id

    String market

    Date timeStamp

    Boolean open

    Date closeDate

    BigDecimal commissionPaid

    BigDecimal price

    BigDecimal pricePerUnit

    BigDecimal quantity

    BigDecimal quantityRemaining

    String buySell

    Boolean isOpen() {
        return open
    }
}
