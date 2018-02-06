package tradingmaster.strategy.indicator

import groovy.transform.CompileStatic

@CompileStatic
class MacdIndicator implements Indicator<BigDecimal> {

    Ema shortEma
    Ema longEma
    Ema signalEma

    MacdIndicator(Integer shortEma, Integer longEma, Integer signalEma) {
        this.shortEma = new Ema(shortEma)
        this.longEma = new Ema(longEma)
        this.signalEma = new Ema(signalEma)
    }

    BigDecimal update(BigDecimal n) {

        this.shortEma.update(n)
        this.longEma.update(n)

        def diff = calculateEMAdiff()
        this.signalEma.update(diff)
        def result = diff - this.signalEma.getResult()

        return result
    }


    BigDecimal calculateEMAdiff() {
        def shortEMA = this.shortEma.getResult()
        def longEMA = this.longEma.getResult()
        def diff = shortEMA - longEMA
        return diff
    }
}
