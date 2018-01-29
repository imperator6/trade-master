package tradingmaster.model

interface ITicker {

    String getMarket()

    BigDecimal getBid()

    BigDecimal getAsk()

    //BigDecimal getLast()

}