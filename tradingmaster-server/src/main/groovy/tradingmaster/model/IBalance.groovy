package tradingmaster.model

interface IBalance {

    String getCurrency()

    BigDecimal getValue()

    BigDecimal getAvailable()

    BigDecimal getPending()

    String getCryptoAddress()

}
