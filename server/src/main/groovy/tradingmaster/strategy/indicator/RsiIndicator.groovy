package tradingmaster.strategy.indicator

import groovy.transform.CompileStatic

@CompileStatic
class RsiIndicator implements Indicator<BigDecimal> {

    BigDecimal lastClose = 0
    Integer weight = 14

    Ema avgU
    Ema avgD

    BigDecimal u = 0
    BigDecimal d = 0
    BigDecimal rs = 0
    BigDecimal rsi = 0
    BigDecimal age = 0

    RsiIndicator(Integer interval) {
        this.weight = interval

        def weightEma = 2 * this.weight - 1
        avgU = new Ema(weightEma)
        avgD = new Ema(weightEma)

    }

    BigDecimal update(BigDecimal n) {

        BigDecimal currentClose = n

        if(currentClose > this.lastClose) {
            this.u = currentClose - this.lastClose
            this.d = 0
        } else {
            this.u = 0
            this.d = this.lastClose - currentClose
        }

        BigDecimal avgUResult = avgU.update(this.u)
        BigDecimal avgDResult = avgD.update(this.d)

        if(avgDResult == 0) avgDResult = new BigDecimal(Double.MIN_VALUE)

        this.rs = avgUResult / avgDResult
        this.rsi = 100 - (100 / (1 + this.rs))

        this.age++
        this.lastClose = currentClose

        return this.rsi
    }
}
