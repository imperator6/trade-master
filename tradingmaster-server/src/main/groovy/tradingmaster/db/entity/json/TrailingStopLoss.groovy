package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString
class TrailingStopLoss {

    Boolean enabled = false

    BigDecimal value  = 2 // sell if it lost 2% after the 12% has reached

    BigDecimal startAt  = 12 // start trailing at 12% profit

    @Deprecated
    Integer keepAtLeastForHours = 0 // replaced with activeAfterHours

    Integer activeAfterHours = 0

    Integer checkInterval = 1

}
