package tradingmaster.strategy;

import groovy.transform.CompileStatic;
import tradingmaster.db.entity.StrategyResult;
import tradingmaster.model.Candle;
import tradingmaster.strategy.indicator.RsiIndicator;

import java.math.BigDecimal;

@CompileStatic
public class Rsi implements Strategy {

    RsiSettings settings;
    RsiIndicator indicator;

    Integer duration = 0;
    String direction = "none";
    boolean persisted = false;
    boolean adviced = false;

    public Rsi(RsiSettings settings) {
        this.settings = settings;
        this.indicator = new RsiIndicator(settings.interval);
    }

    public String getName() {
        return "rsi";
    }

    public StrategyResult execute(Candle c) {

        double rsiVal = this.indicator.update(c.getClose()).doubleValue();

        StrategyResult res = new StrategyResult();
        res.setPriceDate(c.getEnd());
        res.setMarket(c.getMarket().getPair());
        res.setPrice(c.getClose());
        res.setValue1(new BigDecimal(rsiVal));
        res.setName(getName());

        if(rsiVal > settings.high) {

            // new trend detected
            if(this.direction != "high") {

                duration = 0;
                persisted = false;
                direction = "high";
                adviced = false;
            }

            duration++;

            //log.debug('In high since', this.trend.duration, 'candle(s)');

            if(duration >= settings.persistence)
                this.persisted = true;

            if(this.persisted /*&& !this.adviced*/) {
                this.adviced = true;
                res.setAdvice("short");
                return res;
            }

        } else if(rsiVal < settings.low) {

            // new trend detected
            // new trend detected
            if(this.direction != "low") {

                duration = 0;
                persisted = false;
                direction = "low";
                adviced = false;
            }


            duration++;

            //log.debug('In low since', this.trend.duration, 'candle(s)');

            if(duration >= settings.persistence)
                this.persisted = true;

            if(this.persisted /*&& !this.adviced*/) {
                this.adviced = true;
                res.setAdvice("long");
                return res;
            }

        }

        return res; // neutral

    }



}
