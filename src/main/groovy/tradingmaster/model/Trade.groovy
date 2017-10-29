package tradingmaster.model

interface  ITrade {

    String getExtId()
    Double getQuantity()
    BigDecimal getPrice()
    Date getDate()

}
