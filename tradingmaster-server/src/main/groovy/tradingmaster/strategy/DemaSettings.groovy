package tradingmaster.strategy

import groovy.transform.CompileStatic

@CompileStatic
class DemaSettings {

    Boolean enabled = true
    Integer shortPeriod = 10
    Integer longPeriod = 21
    BigDecimal up = 0.025
    BigDecimal down= -0.025
}
