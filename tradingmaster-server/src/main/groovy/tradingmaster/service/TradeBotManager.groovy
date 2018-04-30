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

    @Autowired
    StrategyRunnerService strategyRunnerService


    List<TradeBot> getActiveBots() {
        return new ArrayList(this.TRADE_BOT_MAP.values().findAll { it.active })
    }

    TradeBot findBotById(Integer botId) {
        return TRADE_BOT_MAP.get(botId)
    }

    TradeBot findBotByExchange(String exchange) {
        return TRADE_BOT_MAP.values().find { it.exchange.equalsIgnoreCase(exchange )}
    }

    Position findPositionById(Integer botId, Integer posId) {
        return this.TRADE_BOT_MAP.get(botId).getPositions().find { it.id == posId }
    }

    Position findFirstOpenPosition(Integer botId, String market) {

        List<Position> allByMarket = findAllOpenPositionByMarket(botId, market)

        if(!allByMarket.isEmpty()) {
            allByMarket = allByMarket.sort { a,b -> a.buyDate <=> b.buyDate }
            return allByMarket.first()
        }

        // not found
        return null
    }

    List<Position> findAllOpenPositionByMarket(Integer botId, String market) {
        def res = this.TRADE_BOT_MAP.get(botId).getPositions().findAll { it.market == market && !it.closed && it.buyDate != null}
        if(res == null) {
            res = Collections.EMPTY_LIST
        }
        return res
    }

    List<Position> findAllOpenPosition(Integer botId) {
        return this.TRADE_BOT_MAP.get(botId).getPositions().findAll{ !it.closed }
    }

    synchronized void removeAllPositions(Integer botId) {
       TradeBot bot = this.TRADE_BOT_MAP.get(botId)
        bot.positionMap.clear()
        positionRepository.deleteByBotId( bot.getId() )
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

            Set distinctPositions = new HashSet()

            b.getPositions().findAll { !it.closed || (it.settings && it.settings.traceClosedPosition) }.each { Position p ->
                distinctPositions.add(new CryptoMarket(b.exchange, p.market))
            }

            distinctPositions.each {
                marketWatcheService.createMarketWatcher(  it )
            }

            if(b.baseCurrency.toUpperCase().indexOf("USD") >= 0) {
               // USDT or USD...
                marketWatcheService.createMarketWatcher( new CryptoMarket(b.exchange,  "USDT-BTC"))
                //TODO check if USDT or USD exchange
            } else {
                // for Dollar conversion start a market watcher for USDT
                marketWatcheService.createMarketWatcher( new CryptoMarket(b.exchange,  "USDT-${b.baseCurrency}"))
            }

            b.setStrategyRunner( strategyRunnerService.crerateStrategyRun(b) )


            TRADE_BOT_MAP.put( b.getId(), b )

            syncBanlance(b)
        }

    }

    void refreshBotConfig(IScriptStrategy strategy) {

        TRADE_BOT_MAP.values().findAll{ it.configId == strategy.getId() }.each {
            it.config = parseBotConfig( strategy.getScript() )

            if(it.strategyRunner) {
                it.strategyRunner.close()
            }

            it.setStrategyRunner( strategyRunnerService.crerateStrategyRun(it) )
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

            synchronized (b) {
                if(b.paperExchange == null) {
                    b.paperExchange = new PaperExchange()
                    b.paperExchange.setBalance(b.config.baseCurrency, b.currentBalance)
                }
            }

            return b.getPaperExchange()

        } else {
           return exchangeService.getExchangyByName( b.exchange )
        }
    }

//    IExchangeAdapter getPaperExchangeAdapter(TradeBot b, Position p, Candle c) {
//
//        if(b.isBacktest()) {
//            PaperExchange exchange = new PaperExchange()
//            exchange.config = b.config
//            exchange.candle = c
//
//            log.info(p.market)
//
//            CryptoMarket market = new CryptoMarket(b.exchange, p.market)
//            String currency  = market.getCurrency()
//            String asset = market.getAsset()
//            exchange.setTicker( c.close, currency, asset)
//            return exchange
//        } else {
//            throw new RuntimeException("Not supported!")
//        }
//    }


    TradeBot createNewBot(Integer configId, boolean backtest) {

        if(backtest) {
            TradeBot bot = new TradeBot()
            bot.configId = 7 // TODO... load config by name or given parametr
            bot.backtest = backtest
            bot.active = false

            ScriptStrategy strategy = strategyStore.loadStrategyById(bot.configId , null)
            Map params = parseBotConfig(strategy.getScript())

            bot.config = params
            bot.baseCurrency = params.baseCurrency

            bot.startBalance = params.startBalance

            bot.exchange = "PaperExchange"

            return bot

        } else {

            TradeBot p = new TradeBot()
            p.configId = configId
            p.backtest = backtest

            ScriptStrategy strategy = strategyStore.loadStrategyById(p.configId , null)
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

    }


    boolean isValidSignalForBot(TradeBot b, Signal s) {
        boolean valid = false

        if(b.backtest && b.getPositions().size() > 500) {
            return false
        }

        if(s.getExchange().equalsIgnoreCase(b.exchange)) {

            // check if asset is supported
            //getExchangeAdapter(b).

            if("buy".equalsIgnoreCase(s.buySell) && findAllOpenPosition(b.id).size() > b.config.maxOpenPositions) {
                log.info("Max open positions (${b.config.maxOpenPositions}) has reached! Signal $s.id is not valid for TradeBot $b.id. ")
            } else {
                valid = isValidAssest(b, s.asset)
            }

        } else {
            log.info("Signal $s.id is not valid for TradeBot $b.id. Exchange does not match! $s.exchange != $b.exchange")
        }

        return valid
    }

    boolean isValidAssest(TradeBot b, String asset) {

        boolean valid = false

        if(b.config.assetFilter.enabled) {

            Map filter = b.config.assetFilter

            // check allowed
            if(filter.allowed) {
                List<String> allowedAssets = filter.allowed

                if(allowedAssets.find{ it.equalsIgnoreCase(asset) }) {
                    log.debug("Asset $asset is allowed for Bot ${b.getId()}")
                    valid = true
                } else {
                    log.debug("Asset $asset is NOT allowed for Bot ${b.getId()}")
                    valid = false
                }

            } else if (filter.forbidden) {

                List<String> forbiddenAssets = filter.forbidden

                if(forbiddenAssets.find{ it.equalsIgnoreCase(asset) }) {
                    log.debug("Asset $asset is forbidden for Bot ${b.getId()}")
                } else {
                    valid = true
                }
            } else {
                valid = true
            }
        } else {
            valid = true
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
