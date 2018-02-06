package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString
class TakeProfit {

    Boolean enabled = false

    BigDecimal value = 20
}
