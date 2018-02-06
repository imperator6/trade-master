package tradingmaster.model

interface IOrder {

    String getId()

    String getMarket()

    Date getTimeStamp()

    Boolean isOpen()

    Date getCloseDate()

    BigDecimal getCommissionPaid()

    BigDecimal getPrice()

    BigDecimal getPricePerUnit()

    BigDecimal getQuantity()

    BigDecimal getQuantityRemaining()


}
