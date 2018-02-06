package tradingmaster.model

import groovy.transform.ToString
import tradingmaster.util.NumberHelper

@ToString(includeNames=true)
class PriceLimit {

    BigDecimal signalPrice = 0

    BigDecimal priceLimitPercent = 0

    BigDecimal priceLimit = 0

    PriceLimit(BigDecimal signalPrice, BigDecimal priceLimitPercent) {
        this.signalPrice = signalPrice
        this.priceLimitPercent = priceLimitPercent
        calculate()
    }


    void calculate() {
        priceLimit = NumberHelper.addXPercentTo(signalPrice, priceLimitPercent)
    }


}
