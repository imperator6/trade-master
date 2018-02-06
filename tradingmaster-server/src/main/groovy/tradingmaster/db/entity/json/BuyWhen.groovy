package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString
class BuyWhen {

    Boolean enabled = false

    BigDecimal quantity = 0

    BigDecimal spend = 0

    BigDecimal minPrice = 0

    BigDecimal maxPrice = 0

    Long timeoutHours = 36  // valid for 36 hours


}
