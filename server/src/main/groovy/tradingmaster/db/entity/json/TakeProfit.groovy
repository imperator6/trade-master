package tradingmaster.db.entity.json

import groovy.transform.ToString

@ToString
class TakeProfit {

    Boolean enabled = false

    BigDecimal value = 20
}
