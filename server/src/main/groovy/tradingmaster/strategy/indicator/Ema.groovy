package tradingmaster.strategy.indicator

import tradingmaster.model.Candle

class Ema implements Indicator<BigDecimal> {

    Integer age = 0
    Integer weight

    BigDecimal result = null

    Ema(Integer weight) {
        this.weight = weight
    }

    BigDecimal update(BigDecimal price) {

        // The first time we can't calculate based on previous
        // ema, because we haven't calculated any yet.
        if(result == null) {
            this.result = price
        }

        age++

        return calculate(price)
    }

    //    calculation (based on tick/day):
    //  Ema = Price(t) * k + Ema(y) * (1 – k)
    //  t = today, y = yesterday, N = number of days in Ema, k = 2 / (N+1)
    private BigDecimal calculate(BigDecimal price) {

        // weight factor
        def k = 2 / (this.weight + 1)

        // yesterday
        def y = this.result

        // calculation
        this.result = price * k + y * (1 - k)

        return this.result
    }










}
