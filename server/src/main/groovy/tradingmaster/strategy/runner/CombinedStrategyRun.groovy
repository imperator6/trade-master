package tradingmaster.strategy.runner

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.Candle
import tradingmaster.service.TradeBotManager
import tradingmaster.strategy.Strategy
import tradingmaster.strategy.StrategyResult

@Service
@Scope("prototype")
@Commons
class CombinedStrategyRun implements IStrategyRunner {

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PublishSubscribeChannel signalChannel

    List<Strategy> strategies = []

    TradeBot bot

    //StrategyResult prevActionResult = StrategyResult.NONE
    @Override
    void init(TradeBot bot) {
        this.bot = bot
    }

    @Override
    void nextCandle(Candle c) {

        Map<String, StrategyResult> results = [:]

        strategies.each {
            StrategyResult r = it.execute( c )
            results.put(it.getName(), r )
        }

        boolean goLong = results.values().findAll { it == StrategyResult.LONG }.size() == results.size()

        //boolean goShort = results.values().findAll { it == StrategyResult.SHORT }.size() == results.size()

        if(goLong) {

            // We have a signal
            Signal s = new Signal()
            s.strategyResult = StrategyResult.LONG
            s.asset = c.getMarket().getAsset()
            s.price = c.close
            s.timestamp = new Date()
            s.triggerName = "TA Strategy"

            //tradeBotManager.openPosition(s, bot)

            signalChannel.send( MessageBuilder.withPayload(s).build() )

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

    void close() {

    }
}
