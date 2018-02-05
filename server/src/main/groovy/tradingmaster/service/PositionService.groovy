package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.PositionRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.entity.json.PositionSettings
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.model.*
import tradingmaster.util.NumberHelper

@Service
@Commons
class PositionService {


    @Autowired
    PositionRepository positionRepository

    @Autowired
    OrderExecutorService orderExecutorService

    @Autowired
    MarketWatcherService marketWatcheService

    @Autowired
    PositionUpdateHandler positionUpdateHandler

    @Autowired TradeBotManager tradeBotManager


    Position findPositionById(Integer botId, Integer posId) {
        return tradeBotManager.TRADE_BOT_MAP.get(botId).getPositions().find { it.id == posId }
    }

    void deletePosition(Position pos) {

        log.info("Deleting position with id ${pos.id}")

        TradeBot bot = tradeBotManager.findBotById(pos.getBotId())

        bot.removePosition(pos)
        positionRepository.delete(pos.getId())

        marketWatcheService.stopMarketWatcher(bot.getExchange() ,pos.getMarket())
    }

    Position newPosition(TradeBot bot, String market, PositionSettings settings) {

        Position pos = new Position()
        pos.created = new Date()
        pos.botId = bot.getId()
        pos.status = "wait for market"
        pos.market = market

        pos.settings = settings

        positionRepository.save(pos)
        bot.addPosition(pos)

        log.debug "New buyWehn position created! $pos"

        // start the watcher service to observe the market
        marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))

        return pos
    }

    void openPosition(TradeBot bot, Signal s) {

        if(!isTradingActive(bot)) {
            return
        }

        Position pos = new Position()

        pos.created = new Date()
        pos.botId = bot.getId()
        pos.market = "${bot.config.baseCurrency}-${s.asset}"
        pos.status = "pending"
        pos.buySignalId = s.getId()
        pos.signalRate = s.price

        BigDecimal balanceToSpend = tradeBotManager.calcBalanceForNextTrade(bot)

        if(balanceToSpend > 0) {

            positionRepository.save(pos)
            bot.addPosition(pos)

            openPosition(bot, pos, balanceToSpend, s.getPrice())
        } else {
            log.warn("Can't open position ${s.asset} balance is to low. ${balanceToSpend}")
        }
    }


    void openPosition(TradeBot bot, Position pos, BigDecimal balanceToSpend, BigDecimal signalPrice) {

        if(!isTradingActive(bot)) {
            return
        }

        PriceLimit priceLimit = null
        if(bot.config.buyPriceLimitPercent && signalPrice) {
            priceLimit = new PriceLimit(signalPrice,(BigDecimal) bot.config.buyPriceLimitPercent)
        }

        CryptoMarket market = new CryptoMarket(bot.exchange, pos.getMarket())
        String currency  = market.getCurrency()
        String asset = market.getAsset()

        ExchangeResponse<IOrder> newOrderRes = orderExecutorService.placeLimitOrder(bot, tradeBotManager.getExchangeAdapter(bot), BuySell.BUY, balanceToSpend, (PriceLimit) priceLimit, currency, asset)

        if(newOrderRes.success) {

            IOrder newOrder = newOrderRes.getResult()

            updateBuyPosition(pos, newOrder)
            log.debug "New position created! $pos"

            // start the watcher service to observe the market
            marketWatcheService.createMarketWatcher(market)

            tradeBotManager.syncBanlance(bot)

        } else {
            def msg = "Error on Buy: ${newOrderRes.getMessage()}"
            log.error(msg)
            pos.buyInPogress = false
            pos.setError(true)
            pos.setErrorMsg(msg)
            pos.setClosed(true)
        }

        positionRepository.save(pos)
    }

    boolean isTradingActive(TradeBot bot) {
        if(bot && bot.config) {

            if( bot.config.liveTrading != null) {
                if(!bot.config.liveTrading) {
                    log.warn("*** Live Trading is DISABELD for bot ${bot.id}")
                }

                return bot.config.liveTrading
            } else {
                log.warn("Property 'liveTrading' is not set in config! Check bot with id ${bot.id} ${bot.exchange}")
            }
        }

        return true
    }

    void loadPositionsFromExchange(TradeBot bot) {

        IExchangeAdapter exchangeAdapter = tradeBotManager.getExchangeAdapter(bot)

        List<IBalance> balances = exchangeAdapter.getBalances()

        //List<IOrder> orderHistory =  exchangeAdapter.get
        balances.findAll { it.getCurrency() != bot.getBaseCurrency() } .each { IBalance balance ->

            if(balance.value > 0.0001) {
                Position pos = new Position()

                pos.created = new Date()
                pos.botId = bot.getId()
                pos.buySignalId = -1
                pos.market = "${bot.config.baseCurrency}-${balance.getCurrency()}"
                pos.status = "open"
                pos.signalRate = 0
                pos.setAmount( balance.getAvailable() )
                pos.settings.holdPosition = true

                //pos.setExtbuyOrderId(newOrder.getId())
                // TODO: Try to find a historic order!
                pos.setBuyFee(0)
                pos.setBuyDate(new Date())

                ExchangeResponse<ITicker> tickerExchangeResponse = exchangeAdapter.getTicker(pos.getMarket())

                if(tickerExchangeResponse.success) {
                    ITicker ticker = tickerExchangeResponse.getResult()

                    pos.setBuyRate(ticker.getAsk())

                    positionRepository.save(pos)
                    bot.addPosition(pos)

                    marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))

                } else {
                    log.error("Can create postion for market ${pos.getMarket()}. ${tickerExchangeResponse.getMessage()}")
                }
            }
         }

    }

    void syncPosition(Position pos, TradeBot bot) {
        if(pos.extSellOrderId) {
           ExchangeResponse<IOrder> sellOrder = tradeBotManager.getExchangeAdapter(bot).getOrder(pos.getMarket(), pos.extSellOrderId)
            if(sellOrder.success) {

                updateSellPosition(pos, sellOrder.getResult())
            }
        }

        if(pos.extbuyOrderId) {
            ExchangeResponse<IOrder> buyOrder = tradeBotManager.getExchangeAdapter(bot).getOrder(pos.getMarket(), pos.extbuyOrderId)
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

        if(!isTradingActive(bot)) {
            return
        }

        PriceLimit priceLimit = null
        if(bot.config.sellPriceLimitPercent && sellPrice) {
            priceLimit = new PriceLimit(sellPrice, (BigDecimal) bot.config.sellPriceLimitPercent)
        }

        pos.sellInPogress = true
        positionRepository.save(pos)

        ExchangeResponse<IOrder> newOrderRes = orderExecutorService.placeLimitOrder(bot, tradeBotManager.getExchangeAdapter(bot), BuySell.SELL, pos.amount, (PriceLimit) priceLimit, pos.market)

        if(newOrderRes.success) {

            IOrder newOrder = newOrderRes.getResult()
            updateSellPosition(pos, newOrder)

            log.debug "position ${pos.id} closed! ${NumberHelper.twoDigits(pos.result)}%"
            positionRepository.save(pos) // save before stopping the market manager

            try {
                marketWatcheService.stopMarketWatcher(bot.getExchange(), pos.getMarket())
            } catch(Exception e) {
                log.error("Error while trying to stop the market watcher!")
            }

            tradeBotManager.syncBanlance(bot)

        } else {
            log.error("Error on Sell: ${newOrderRes.getMessage()}")

            pos.setError(true)
            pos.setErrorMsg(newOrderRes.getMessage())
            pos.setClosed(false)
            pos.sellInPogress = false

            positionRepository.save(pos)
        }
    }
}
