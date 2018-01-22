package tradingmaster.model

interface IOrder {

    String getId()

    Boolean isOpen()

    Date getCloseDate()

    BigDecimal getCommissionPaid()

    BigDecimal getPrice()

    BigDecimal getQuantity()

}
