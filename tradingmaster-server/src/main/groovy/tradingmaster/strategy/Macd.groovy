package tradingmaster.strategy

import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import tradingmaster.db.entity.StrategyResult
import tradingmaster.model.Candle
import tradingmaster.strategy.indicator.MacdIndicator

@CompileStatic
@Commons
class Macd implements Strategy {

    MacdSettings settings
    MacdIndicator indicator

    Integer duration = 0
    String direction = 'up'
    boolean persisted = false
    boolean adviced = false


    Macd(MacdSettings s) {
        this.settings = s
        this.indicator =  new MacdIndicator(s.shortPeriod, s.longPeriod, s.signalPeriod)
    }

    String getName() {
        return "macd"
    }

    StrategyResult execute(Candle c) {

        BigDecimal macddiff = this.indicator.update(c.close)

        StrategyResult res = new StrategyResult()
        res.setPriceDate(c.getEnd())
        res.setMarket(c.getMarket().getPair())
        res.setPrice(c.getClose())
        res.setValue1(macddiff)
        res.setName(getName())

        if(macddiff > this.settings.up) {

            // new trend detected
            if(this.direction != 'up') {
                // reset the state for the new trend
                log.debug("New up trend detection")
                duration = 0
                persisted = false
                direction = 'up'
                adviced =false
            }

            this.duration++

            log.debug("In uptrend since $duration candle(s)")

            if(this.duration >= this.settings.persistence)
                this.persisted = true;

            if(this.persisted && !this.adviced) {
                this.adviced = true
                res.setAdvice("long")
                return res
            }

        } else if(macddiff < this.settings.down) {

            // new trend detected
            if(this.direction != 'down') {
                // reset the state for the new trend
                log.debug("New down trend detection")
                duration = 0
                persisted = false
                direction = 'down'
                adviced = false
            }

            this.duration++

            log.debug("In downtrend since $duration candle(s)")

            if(this.duration >= this.settings.persistence)
                this.persisted = true

            if(this.persisted && !this.adviced) {
                this.adviced = true
                res.setAdvice("short")
                return res
            }

        }

        //log.debug('In no trend')

        return res // neutral

    }

}
