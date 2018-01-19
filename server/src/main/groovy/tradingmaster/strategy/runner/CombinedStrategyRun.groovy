package tradingmaster.strategy.runner

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import tradingmaster.model.Candle
import tradingmaster.model.TradeBot
import tradingmaster.service.TradeBotManager
import tradingmaster.strategy.Strategy
import tradingmaster.strategy.StrategyResult

@Service
@Scope("prototype")
@Commons
class CombinedStrategyRun  implements IStrategyRunner {

    @Autowired
    TradeBotManager tradeBotManager

    List<Strategy> strategies = []

    TradeBot bot

    //StrategyResult prevActionResult = StrategyResult.NONE

    void init(TradeBot bot) {
        this.bot = bot
    }

    void nextCandle(Candle c) {

        Map<String, StrategyResult> results = [:]

        strategies.each {
            StrategyResult r = it.execute( c )
            results.put(it.getName(), r )
        }

        boolean goLong = results.values().findAll { it == StrategyResult.LONG }.size() == results.size()

        //boolean goShort = results.values().findAll { it == StrategyResult.SHORT }.size() == results.size()

        if(goLong) {

            tradeBotManager.openPosition("TA Strategy", c, bot)

            //prevActionResult = StrategyResult.LONG
        }

//        if(goShort && prevActionResult != StrategyResult.SHORT) {
//
//            actionBindings.sell()
//            prevActionResult = StrategyResult.SHORT
//        }

        // TODO: check open position for profit to close
        // find positions for given asset! check candle

    }
}
