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
import tradingmaster.service.StrategyRunnerService
import tradingmaster.service.TradeBotManager
import tradingmaster.service.cache.CacheService
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

    @Autowired
    StrategyRunnerService strategyRunnerService

    List<Strategy> strategies = []

    TradeBot bot

    Integer candleCount = 0

    @Override
    void init(TradeBot bot) {
        this.bot = bot
    }


    @Override
    synchronized List<Signal> nextCandle(Candle c) {


        boolean execute = false

        if(positionUpdateHandler.isValidCandleSize(this.bot, c) && !this.strategies.isEmpty()) {

            if(bot.config.backtest.enabled && c.botId == this.bot.id) {
                execute = true // backtest only
            }

            if (!bot.config.backtest.enabled && c.botId == null) {
                // execute for live trading!
                execute = true
            }

            // check if assed is allowed
            if(!tradeBotManager.isValidAssest(this.bot, c.getMarket().getAsset())) {
                log.debug("Skipping checking startegies for bot ${bot.id}. Asset ${c.getMarket().getAsset()} is not allowed!")
                execute = false
            }

        }

        if(!execute) return Collections.EMPTY_LIST

        log.debug("Strategy Execution ${c.market.getCurrency()}-${c.market.getAsset()} (${c.end}) for bot ${bot.id}. Candlesize ${c.period}" )

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

        candleCount++

        if(bot.config.warmup && (candleCount <= bot.config.warmup)) {
            log.debug("Skipping StrategyResult results. Warmup is not complete ${bot.config.warmup} < ${candleCount} ")
            return []
        }

        boolean goLong = !longResults.isEmpty()

        boolean goShort = !shortResults.isEmpty()

      //  goShort = false; // TODO... only via stop loss make it configurable !!! Does not work on downtrend !!!

        String market = "${bot.config.baseCurrency}-${c.getMarket().getAsset()}".toString()

        Position firstOpenPosition = tradeBotManager.findFirstOpenPosition( bot.id, market )

        List<Position> allOpenPositions = tradeBotManager.findAllOpenPositionByMarket(bot.id, market)

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

            if(allOpenPositions.size() >= bot.config.maxOpenPositions) {
                log.debug("Can't open a new position for Signal s ${s}, as max open position ${bot.config.maxOpenPositions} has been reached!")
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

                def posResult = positionUpdateHandler.calculatePositionResult(firstOpenPosition.buyRate, c.close, 0.0)

                // dust trade protection
                if(posResult.abs() < bot.config.dustTradeProtection) {
                    log.info("Dust trade protection. Won't sell pos ${firstOpenPosition.id} result ${firstOpenPosition.result}% is within ${bot.config.dustTradeProtection}%")
                } else {
                    signals.add(s)
                    log.debug("Strategy go SHORT (sell)!")
                }

            } else {
                log.debug("Can't close any position for Signal s ${s}, as no open position exsits for market ${c.getMarket()}!")
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

    void resetStrategies(Candle c) {

        this.candleCount = 0
        this.strategies = this.strategyRunnerService.createStrategies(bot.config)

    }

    void close() {
        this.bot = null
        this.positionUpdateHandler = null
        this.tradeBotManager = null
        this.strategies = null
        this.strategyRunnerService = null
    }
}
