package tradingmaster.service

import groovy.json.JsonSlurper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.PositionRepository
import tradingmaster.db.TradeBotRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.mariadb.MariaStrategyStore
import tradingmaster.exchange.ExchangeService
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.exchange.paper.PaperExchange
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IScriptStrategy
import tradingmaster.model.ScriptStrategy

import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap

@Service
@Commons
class TradeBotManager {

    static Map<Integer, TradeBot> TRADE_BOT_MAP = new ConcurrentHashMap()

    @Autowired
    ExchangeService exchangeService

    @Autowired
    MariaStrategyStore strategyStore

    @Autowired
    TradeBotRepository tradeBotRepository

    @Autowired
    PositionRepository positionRepository

    @Autowired
    MarketWatcherService marketWatcheService


    List<TradeBot> getActiveBots() {
        return new ArrayList(this.TRADE_BOT_MAP.values().findAll { it.active })
    }

    TradeBot findBotById(Integer botId) {
        return TRADE_BOT_MAP.get(botId)
    }

    Position findPositionById(Integer botId, Integer posId) {
        return this.TRADE_BOT_MAP.get(botId).getPositions().find { it.id == posId }
    }

    void startBots() {

        tradeBotRepository.findByActive(true).each { TradeBot b ->

            log.info("********************************************")
            log.info("* Starting trading bot $b")
            log.info("********************************************")

            // load the config
            ScriptStrategy strategy = strategyStore.loadStrategyById(b.configId, null)
            b.config = parseBotConfig( strategy.getScript() )
            b.config.exchange = b.exchange // sync exchange

            positionRepository.findByBotId(b.id).each {
                b.addPosition(it)
            }

            b.getPositions().findAll { !it.closed && !it.error }.each { Position p ->
                marketWatcheService.createMarketWatcher( new CryptoMarket(b.exchange, p.market) )
            }

            // for Dollar conversion start a market watcher for USDT
            marketWatcheService.createMarketWatcher( new CryptoMarket(b.exchange,  "USDT-${b.baseCurrency}"))

            TRADE_BOT_MAP.put( b.getId(), b )

            syncBanlance(b)
        }

    }

    void refreshBotConfig(IScriptStrategy strategy) {

        TRADE_BOT_MAP.values().findAll{ it.configId == strategy.getId() }.each {
            it.config = parseBotConfig( strategy.getScript() )
            log.info("Bot config on bot ${it.id} has been updated!")
        }

    }

    private Map parseBotConfig(String configScript) {
        Map config = new JsonSlurper().parseText(configScript)
        return config
    }

    void syncBanlance(TradeBot b) {

        log.debug("Syncing balance for bot ${b.id}")

        def balanceOnExchange = getExchangeAdapter(b).getBalance(b.config.baseCurrency).getValue()

        if(b.startBalance == null || (b.startBalance == 0 && balanceOnExchange > 0)) {
            log.info("Setting start balance for bot ${b.id} to $balanceOnExchange")
            b.startBalance = balanceOnExchange
        }

        log.info("Setting current balance for bot ${b.id} to $balanceOnExchange")
        b.currentBalance = balanceOnExchange

        tradeBotRepository.save(b)
    }

    IExchangeAdapter getExchangeAdapter(TradeBot b) {
        if(b.isBacktest()) {

            PaperExchange exchange = new PaperExchange()
            exchange.config = b.config
            return exchange

        } else {
           return exchangeService.getExchangyByName( b.exchange )
        }
    }


    TradeBot createNewBot(Integer configId, boolean backtest) {

        TradeBot p = new TradeBot()
        p.configId = configId
        p.backtest = backtest

        ScriptStrategy strategy = strategyStore.loadStrategyById(configId, null)
        Map params = parseBotConfig(strategy.getScript())

        p.config = params
        p.baseCurrency = params.baseCurrency
        p.exchange = params.exchange

        syncBanlance(p)

        tradeBotRepository.save(p)

        log.info("A new trade bot has been initilized: ${p}")

        TRADE_BOT_MAP.put( p.getId(), p )

        return p
    }


    boolean isValidSignalForBot(TradeBot b, Signal s) {
        boolean valid = false

        if(s.getExchange().equalsIgnoreCase(b.exchange)) {

            // check if asset is supported
            //getExchangeAdapter(b).

            if(b.getPositions().findAll { !it.closed }.size() < b.config.maxOpenPositions) {

                // check forbiddenAssets
                if( b.config.forbiddenAssets) {

                    List<String> forbiddenAssets = b.config.forbiddenAssets

                    if(forbiddenAssets.find{ it.equalsIgnoreCase(s.asset) }) {
                        log.info("Asset $s.asset is forbidden for Bot ${b.getId()}")
                    } else {
                        valid = true
                    }
                } else {
                    valid = true
                }
            } else {
                log.info("Max open positions  (${b.config.maxPosition}) has reached! Signal $s.id is not valid for TradeBot $b.id. ")
            }


        } else {
            log.info("Signal $s.id is not valid for TradeBot $b.id. Exchange does not match! $s.exchange != $b.exchange")
        }

        return valid
    }


    BigDecimal calcBalanceForNextTrade(TradeBot bot) {

        syncBanlance(bot)

        if(bot.config.amountPerOrder) {

            if(bot.currentBalance >= bot.config.amountPerOrder) {
                return bot.config.amountPerOrder
            } else {
             // use whatever is left
                return bot.currentBalance

            }
        } else {
            log.fatal("Property amountPerOrder is missing! Can't calc next order amount!")
            return 0.0
        }
    }


    static  String format(BigDecimal value) {
        DecimalFormat myFormatter = null
        if(value > 1) {
            myFormatter = new DecimalFormat( "###.###")
        } else {
            myFormatter = new DecimalFormat( "#.#######")
        }

        return myFormatter.format(value)
    }

}
