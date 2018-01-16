package tradingmaster.strategy

import tradingmaster.model.Candle
import tradingmaster.strategy.indicator.Ema


class Dema implements Strategy {

    Ema shortEma
    Ema longEma

    def up = 0.025
    def down = -0.025

    Dema(DemaSettings settings) {
        this.shortEma = new Ema(settings.shortPeriod)
        this.longEma = new Ema(settings.longPeriod)
        this.up = settings.up
        this.down = settings.down
    }

    String getName() {
        return "dema"
    }

    StrategyResult execute(Candle c) {

        def s = shortEma.update(c)
        def l = longEma.update(c)

        def diff = 100 * (s - l) / ((s + l) / 2)

        if(diff < down) {

            return StrategyResult.SHORT
        } else if(diff > up) {


            return StrategyResult.LONG
        } else  {

            return StrategyResult.NONE
        }
    }

}