package tradingmaster.strategy;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import tradingmaster.model.Candle;
import tradingmaster.strategy.indicator.Ema;

import java.math.BigDecimal;

@CompileStatic
public class Dema implements Strategy {

    private Ema shortEma;
    private Ema longEma;
    private double up = 0.025;
    private double down = -0.025;

    public Dema(DemaSettings settings) {
        this.shortEma = new Ema(settings.getShortPeriod());
        this.longEma = new Ema(settings.getLongPeriod());
        this.up = settings.getUp().doubleValue();
        this.down = settings.getDown().doubleValue();
    }

    public String getName() {
        return "dema";
    }

    public StrategyResult execute(Candle c) {

        BigDecimal s = shortEma.update(c.getClose());
        BigDecimal l = longEma.update(c.getClose());

        double diff = 100.0 * (s.doubleValue() - l.doubleValue()) / ((s.doubleValue() + l.doubleValue()) / 2.0);

        if (diff < down) {

            return StrategyResult.SHORT;
        } else if (diff > up) {


            return StrategyResult.LONG;
        } else {

            return StrategyResult.NONE;
        }

    }



}
