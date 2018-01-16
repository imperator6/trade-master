package tradingmaster.strategy

class MacdSettings {

    Boolean enabled = true

    Integer shortPeriod = 10
    Integer longPeriod = 21
    Integer signalPeriod = 21

    BigDecimal up = 0.025
    BigDecimal down= -0.025

    Integer persistence = 1
}
