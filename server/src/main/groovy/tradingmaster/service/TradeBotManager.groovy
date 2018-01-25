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
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.exchange.ExchangeService
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.exchange.paper.PaperExchange
import tradingmaster.model.*
import tradingmaster.util.NumberHelper

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
    OrderExecutorService orderExecutorService

    @Autowired
    MaketWatcherService marketWatcheService

    @Autowired
    PositionUpdateHandler positionUpdateHandler


    List<TradeBot> getActiveBots() {
        return new ArrayList(this.TRADE_BOT_MAP.values().findAll { it.active })
    }

    TradeBot findBotById(Integer botId) {
        return TRADE_BOT_MAP.get(botId)
    }

    Position findPositionById(Integer botId, Integer posId) {
        return this.TRADE_BOT_MAP.get(botId).positions.find { it.id == posId }
    }

    void startBots() {

        tradeBotRepository.findByActive(true).each { TradeBot b ->

            log.info("********************************************")
            log.info("* Starting trading bot $b")
            log.info("********************************************")

            // load the config
            ScriptStrategy strategy = strategyStore.loadStrategyById(b.configId, null)
            b.config = parseBotConfig( strategy.getScript() )

            b.positions = positionRepository.findByBotId(b.id)

            b.positions.findAll { !it.closed && !it.error }.each { Position p ->
                marketWatcheService.createMarketWatcher( new CryptoMarket(b.exchange, p.market) )
            }

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

        if(b.startBalance == null) {
            log.info("Setting start balance for bot ${b.id} to $balanceOnExchange")
            b.startBalance = balanceOnExchange
        }

        log.info("Setting current balance for bot ${b.id} to $balanceOnExchange")
        b.currentBalance = balanceOnExchange
    }

    IExchangeAdapter getExchangeAdapter(TradeBot b) {
        if(b.isBacktest()) {

            PaperExchange exchange = new PaperExchange()
            exchange.config = b.config
            return exchange

        } else {
           return exchangeService.getExchangyByName( b.config.exchange )
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

            if(b.positions.findAll { !it.closed }.size() < b.config.maxPositions) {

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

    void openPosition(TradeBot bot, Signal s) {

        // TODO: calculate based on settings
        BigDecimal balanceToSpend = calcBalanceForNextTrade(bot)

        if(balanceToSpend > 0) {

            Position pos = new Position()

            pos.created = new Date()
            pos.botId = bot.getId()
            pos.buySignalId = s.getId()

            pos.market = "${bot.config.baseCurrency}-${s.asset}"
            pos.status = "pending"
            pos.signalRate = s.price

            // Too: call exc to place the order...
            String currency  = bot.config.baseCurrency
            String asset = s.asset

            PriceLimit priceLimit = null
            if(bot.config.buyPriceLimitPercent && s.getPrice()) {
                priceLimit = new PriceLimit(s.getPrice(), bot.config.buyPriceLimitPercent)
            }

            positionRepository.save(pos)
            bot.positions << pos

            ExchangeResponse<IOrder> newOrderRes = orderExecutorService.placeLimitOrder(bot, getExchangeAdapter(bot), BuySell.BUY, balanceToSpend, (PriceLimit) priceLimit, currency, asset)

            if(newOrderRes.success) {

                IOrder newOrder = newOrderRes.getResult()

                updateBuyPosition(pos, newOrder)
                log.debug "New position created! $pos"

                // start the watcher service to observe the market
                marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, bot.baseCurrency, s.asset))

            } else {
                def msg = "Error on Buy: ${newOrderRes.getMessage()}"
                log.error(msg)
                pos.setError(true)
                pos.setErrorMsg(msg)
                pos.setClosed(true)
            }

            positionRepository.save(pos)


        } else {
            // balance to small

        }
    }

    BigDecimal extractFee(BigDecimal amount, BigDecimal fee) {
        amount *= 1e8
        amount *= fee
        amount = Math.floor(amount)
        amount /= 1e8
        return amount
    }

    void syncPosition(Position pos, TradeBot bot) {
        if(pos.extSellOrderId) {
           ExchangeResponse<IOrder> sellOrder = getExchangeAdapter(bot).getOrder(pos.extSellOrderId)
            if(sellOrder.success) {

                updateSellPosition(pos, sellOrder.getResult())
            }
        }

        if(pos.extbuyOrderId) {
            ExchangeResponse<IOrder> buyOrder = getExchangeAdapter(bot).getOrder(pos.extbuyOrderId)
            if(buyOrder.success) {
                updateBuyPosition(pos, buyOrder.getResult())
            }
        }

        positionRepository.save(pos)
    }

    private void updateSellPosition(Position pos, IOrder newOrder) {
        pos.setExtSellOrderId(newOrder.getId())
        pos.setSellFee(newOrder.getCommissionPaid())
        pos.setSellDate(newOrder.getCloseDate())
        pos.setSellRate(newOrder.getPricePerUnit())
        pos.setAmount( newOrder.getQuantity())

        // calc final profit
        def currentPrice = newOrder.getPricePerUnit()
        BigDecimal resultInPercent = positionUpdateHandler.calculatePositionResult(pos.getBuyRate(), currentPrice)
        pos.result = resultInPercent
        pos.setStatus("closed")
        pos.setClosed(true)
        pos.sellInPogress = false
    }

    private void updateBuyPosition(Position pos, IOrder newOrder) {
        // validate order !!
        if(!newOrder.getId()) {
            log.error("No order id is provided!")
        }

        if(newOrder.getCommissionPaid() == null) {
            log.error("No getCommissionPaid id is provided!")
        }

        if(newOrder.getCloseDate() == null) {
            log.error("No getCloseDate id is provided!")
        }

        if(newOrder.getPricePerUnit() == null) {
            log.error("No getPricePerUnit id is provided!")
        }

        if(newOrder.getQuantity() == null) {
            log.error("No getQuantity id is provided!")
        }

        // update position
        pos.setExtbuyOrderId(newOrder.getId())
        pos.setBuyFee(newOrder.getCommissionPaid())
        pos.setBuyDate(newOrder.getCloseDate())
        pos.setBuyRate(newOrder.getPricePerUnit())
        pos.setAmount( newOrder.getQuantity())

        pos.setStatus("open")
    }

    void closePosition(Position pos, Candle c, TradeBot bot) {
        closePosition(pos, c.close, bot)
    }

    void closePosition(Position pos, BigDecimal sellPrice, TradeBot bot) {

        PriceLimit priceLimit = null
        if(bot.config.sellPriceLimitPercent && sellPrice) {
            priceLimit = new PriceLimit(sellPrice, (BigDecimal) bot.config.sellPriceLimitPercent)
        }

        pos.sellInPogress = true
        positionRepository.save(pos)

        ExchangeResponse<IOrder> newOrderRes = orderExecutorService.placeLimitOrder(bot, getExchangeAdapter(bot), BuySell.SELL, pos.amount, (PriceLimit) priceLimit, pos.market)

        if(newOrderRes.success) {

            IOrder newOrder = newOrderRes.getResult()
            updateSellPosition(pos, newOrder)

            log.debug "position ${pos.id} closed! ${NumberHelper.twoDigits(pos.result)}%"

            // TODO: stop the watcher service to observe the market
           // marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, bot.baseCurrency, s.asset))

        } else {
            log.error("Error on Sell: ${newOrderRes.getMessage()}")

            pos.setError(true)
            pos.setErrorMsg(newOrderRes.getMessage())
            pos.setClosed(false)
            pos.sellInPogress = false
        }

        positionRepository.save(pos)
    }

    BigDecimal calcBalanceForNextTrade(TradeBot bot) {

        syncBanlance(bot)

        def maxPositions = bot.config.maxPositions
        def openPositions = bot.positions.findAll { !it.closed }
        def positionsLeft = maxPositions - openPositions.size()

        def balanceForNextTrade = bot.currentBalance / positionsLeft

        // Todo: check balance on exchange

        return balanceForNextTrade
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
