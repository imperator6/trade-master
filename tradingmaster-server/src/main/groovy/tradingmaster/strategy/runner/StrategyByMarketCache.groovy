package tradingmaster.strategy.runner

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.Candle
import tradingmaster.service.PositionUpdateHandler
import tradingmaster.service.StrategyRunnerService
import tradingmaster.service.TradeBotManager
import tradingmaster.service.cache.CacheService

@Service
@Scope("prototype")
@Commons
class StrategyByMarketCache extends CacheService<IStrategyRunner> implements IStrategyRunner {

    @Autowired
    TradeBotManager tradeBotManager


    @Autowired
    StrategyRunnerService strategyRunnerService

    @Autowired
    PositionUpdateHandler positionUpdateHandler


    TradeBot bot

    @Override
    void init(TradeBot bot) {
        this.bot = bot
    }


    @Override
    synchronized List<Signal> nextCandle(Candle c) {

        boolean execute = false

        if(positionUpdateHandler.isValidCandleSize(this.bot, c)) {

            if(bot.backtest && c.botId == this.bot.id) {
                execute = true // backtest only
            }

            if (!bot.backtest && c.botId == null) {
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

        IStrategyRunner runnerForMarket = get(c.getMarket())

        if(runnerForMarket == null) {
            log.info("Creating new Strategy instance for market ${c.getMarket()} for bot ${bot.id}")
            runnerForMarket = strategyRunnerService.crerateStrategyRun(this.bot)
            set(c.market, runnerForMarket)
        }


        return runnerForMarket.nextCandle( c )
    }

    void resetStrategies(Candle c) {

        IStrategyRunner runnerForMarket = get(c.getMarket())

        if(runnerForMarket) {
            runnerForMarket.resetStrategies(c)
        }
    }

    void close() {
        super.cacheMap.values().each { IStrategyRunner r ->
            r.close()
        }

        this.bot = null
        this.tradeBotManager = null
        this.strategyRunnerService = null

    }
}
