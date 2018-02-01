package tradingmaster.db.entity.json

import groovy.transform.ToString

@ToString
class TrailingStopLoss {

    Boolean enabled = false

    BigDecimal value  = 2 // sell if it lost 2% after the 12% has reached

    BigDecimal startAt  = 12 // start trailing at 12% profit
}
