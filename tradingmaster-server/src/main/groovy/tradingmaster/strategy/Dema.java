package tradingmaster.strategy;

import groovy.transform.CompileStatic;
import tradingmaster.db.entity.StrategyResult;
import tradingmaster.model.Candle;
import tradingmaster.strategy.indicator.Ema;

import java.math.BigDecimal;

@CompileStatic
public class Dema implements Strategy {

    private Ema shortEma;
    private Ema longEma;
    private double up = 0.025;
    private double down = -0.025;
    private boolean reverse = false;
    private boolean signalOnChange = false;

    String direction = "neutral";


    public Dema(DemaSettings settings) {
        this.shortEma = new Ema(settings.getShortPeriod());
        this.longEma = new Ema(settings.getLongPeriod());
        this.up = settings.getUp().doubleValue();
        this.down = settings.getDown().doubleValue();
        this.reverse = settings.getReverse();
        this.signalOnChange = settings.getSignalOnChange();
    }

    public String getName() {
        return "dema";
    }

    public StrategyResult execute(Candle c) {

        BigDecimal s = shortEma.update(c.getClose());
        BigDecimal l = longEma.update(c.getClose());

        double diff = 100.0 * (s.doubleValue() - l.doubleValue()) / ((s.doubleValue() + l.doubleValue()) / 2.0);

        StrategyResult res = new StrategyResult();
        res.setPriceDate(c.getEnd());
        res.setMarket(c.getMarket().getPair());
        res.setPrice(c.getClose());
        res.setValue1(s);
        res.setValue2(l);
        res.setValue3(new BigDecimal(diff));
        res.setName(getName());

        if (diff < down /*&& direction != "down"*/) {

            if(signalOnChange) {
                if(direction != "down") {
                    this.direction = "down";
                    if(reverse)
                        res.setAdvice("long");
                    else
                        res.setAdvice("short");

                    return res;
                }
            } else {
                if(reverse)
                    res.setAdvice("long");
                else
                    res.setAdvice("short");
                return res;
            }


        } else if (diff > up /* &&  direction != "up" */) {

            if(signalOnChange) {
                if(direction != "up") {
                    this.direction = "up";

                    if(reverse)
                        res.setAdvice("short");
                    else
                        res.setAdvice("long");

                    return res;
                }

            } else {
                if(reverse)
                    res.setAdvice("short");
                else
                    res.setAdvice("long");

                return res;
            }
        }

        return res; // neutral is default

    }



}
