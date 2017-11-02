package tradingmaster.model

interface  ITrade {

    String getExtId()

    BigDecimal getQuantity()

    BigDecimal getPrice()

    Date getDate()

}
