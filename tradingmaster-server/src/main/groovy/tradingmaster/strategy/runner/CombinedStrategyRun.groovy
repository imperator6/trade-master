package tradingmaster.strategy.runner

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.Candle
import tradingmaster.service.PositionUpdateHandler
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
    PositionUpdateHandler positionUpdateHandler

    List<Strategy> strategies = []

    TradeBot bot

    Integer candleCount = 0

    @Override
    void init(TradeBot bot) {
        this.bot = bot
    }


    @Override
    List<Signal> nextCandle(Candle c) {

        candleCount++

        boolean execute = false

        if(positionUpdateHandler.isValidCandleSize(this.bot, c) && !this.strategies.isEmpty()) {

            if(bot.backtest && c.botId == this.bot.id) {
                execute = true
            }

            if (!bot.backtest && c.botId == null) {
                execute = false
            }

            // check if assed is allowed
            if(!tradeBotManager.isValidAssest(this.bot, c.getMarket().getAsset())) {
                execute = false
            }

            if(bot.config.warmup && (candleCount <= bot.config.warmup)) {
                execute = false
            }
        }

        if(!execute) return Collections.EMPTY_LIST

        List signals = []

        Map<String, StrategyResult> longResults = [:]
        Map<String, StrategyResult> shortResults = [:]

        strategies.each {
            StrategyResult r = it.execute( c )

            if(r == StrategyResult.LONG) {
                longResults.put(it.getName(), r )
            }

            if(r == StrategyResult.SHORT) {
                shortResults.put(it.getName(), r )
            }
        }

        boolean goLong = !longResults.isEmpty()

        boolean goShort = !shortResults.isEmpty()

        String market = "${bot.baseCurrency}-${c.getMarket().getAsset()}".toString()

        Position firstOpenPosition = tradeBotManager.findFirstOpenPosition( bot.id, market )


        if(goLong && !goShort) {

            def triggerName = longResults.keySet().join(",")

            // We have a signal
            Signal s = new Signal()
            s.buySell = "buy"
            s.asset = c.getMarket().getAsset()
            s.price = c.close
            s.signalDate = c.end
            s.exchange = this.bot.exchange
            s.triggerName = triggerName
            s.botId = this.bot.id

            if(firstOpenPosition) {
                log.debug("Can't open a new position for Signal s ${s}, as position is already open exsits!")
            } else {
                signals.add(s)
                log.debug("Strategy go LONG (buy)!")
            }
        }

        if(goShort && !goLong) {

            def triggerName = shortResults.keySet().join(",")

            // We have a signal
            Signal s = new Signal()
            s.buySell = "sell"
            s.asset = c.getMarket().getAsset()
            s.price = c.close
            s.signalDate = c.end
            s.exchange = this.bot.exchange
            s.triggerName = triggerName
            s.botId = this.bot.id

            if(firstOpenPosition) {
                s.positionId = firstOpenPosition.id
                signals.add(s)
                log.debug("Strategy go SHORT (sell)!")
            } else {
                log.debug("Can't close any position for Signal s ${s}, as no open position exsits!")
            }
        }



//        if(goShort && prevActionResult != StrategyResult.SHORT) {
//
//            actionBindings.sell()
//            prevActionResult = StrategyResult.SHORT
//        }

        // TODO: check open position for profit to close
        // find positions for given asset! check candle

        return signals

    }

    void close() {
        this.bot = null
        this.positionUpdateHandler = null
        this.tradeBotManager = null
        this.strategies = null
    }
}
