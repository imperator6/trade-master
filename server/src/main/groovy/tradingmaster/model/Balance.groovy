package tradingmaster.model

import groovy.transform.ToString

@ToString
class Balance {

    String currency

    BigDecimal value = 0
    BigDecimal available = 0
    BigDecimal pending =  0

    String cryptoAddress = ""
}
