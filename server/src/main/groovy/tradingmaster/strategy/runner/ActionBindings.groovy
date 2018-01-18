package tradingmaster.strategy.runner

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import tradingmaster.model.BuySell
import tradingmaster.model.Candle
import tradingmaster.model.TradingSignal
import tradingmaster.service.PaperPortfolioService

@Component
@Scope(value='prototype')
@Commons
class ActionBindings {

    @Autowired
    PaperPortfolioService paperPortfolioService

    Candle candle
    DefaultStrategyRun run

    ActionBindings() {
    }

    void buy() {
        this.buy(null, null)
    }

    void sell() {
        this.sell(null, null)
    }

    void buy(Number price, Number amount) {
        if(price == null)
            price = candle.close

        TradingSignal signal =  new TradingSignal()
        signal.date = candle.end
        signal.type = BuySell.BUY.getName()
        signal.value = price

        run.signals << signal

        if(run.backtest) {
            run.portfolioChanges << paperPortfolioService.buy(price, amount, candle, run.portfolio)
        } else {
            // no backtest real portfolio action
            log.warn("Not implemented!")
        }

    }

    void sell(Number price, Number amount) {
        if(price == null)
            price = candle.close

        TradingSignal signal =  new TradingSignal()
        signal.date = candle.end
        signal.type = BuySell.SELL.getName()
        signal.value = price

        run.signals << signal

        if(run.backtest) {
            run.portfolioChanges << paperPortfolioService.sell(price, amount, candle, run.portfolio)
        } else {
            // no backtest real portfolio action
            log.warn("Not implemented!")
        }
    }

    void onFirstCandle(Candle c) {
        if(run.backtest) {
            paperPortfolioService.onFirstCandle(c, run.portfolio)
        }
    }

    void onLastCandle(Candle c) {
        if(run.backtest) {
            paperPortfolioService.onLastCandle(c, run.portfolio)
        }
    }

}
