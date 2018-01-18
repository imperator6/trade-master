package tradingmaster.strategy.indicator;

import java.math.BigDecimal;

public class Ema implements Indicator<BigDecimal> {

    private Integer age = 0;
    private Integer weight;
    private BigDecimal result = null;

    public Ema(Integer weight) {
        this.weight = weight;
    }

    public BigDecimal update(BigDecimal price) {

        // The first time we can't calculate based on previous
        // ema, because we haven't calculated any yet.
        if (result == null) {
            this.result = price;
        }

        age++;

        return calculate(price);
    }

    private BigDecimal calculate(BigDecimal price) {

        // weight factor
        double k = 2.0 / (this.weight + 1.0);

        // yesterday
        BigDecimal y = this.result;

        // calculation
        this.result = new BigDecimal(price.doubleValue() * k + y.doubleValue() * (1.0 - k));

        return this.result;
    }

    public BigDecimal getResult() {
        return result;
    }
}
