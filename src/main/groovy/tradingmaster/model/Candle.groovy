package tradingmaster.model

import groovy.transform.ToString

import java.time.Instant

@ToString
class Candle {

    IMarket market

    Instant start
    Instant end

    BigDecimal open = 0.0
    BigDecimal high = 0.0
    BigDecimal low = 0.0
    BigDecimal close = 0.0

    BigDecimal volume = 0.0
    BigDecimal volumnWeightedPrice = 0.0

    Integer tradeCount = 0



}
