package tradingmaster.db.entity.json

import groovy.transform.ToString

@ToString
class StopLoss {

    Boolean enabled = false

    BigDecimal value = -10
}
