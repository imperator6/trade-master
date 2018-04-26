package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.OrderRepository
import tradingmaster.db.PositionRepository
import tradingmaster.db.entity.Order
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.entity.json.PositionSettings
import tradingmaster.db.entity.json.StopLoss
import tradingmaster.db.entity.json.TakeProfit
import tradingmaster.db.entity.json.TrailingStopLoss
import tradingmaster.db.mariadb.MariaCandleStore
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.model.*
import tradingmaster.util.NumberHelper

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

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

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    OrderRepository orderRepository

    @Autowired
    OrderService orderService

    @Autowired
    MariaCandleStore candleStore


    Position findPositionById(Integer botId, Integer posId) {
        return tradeBotManager.TRADE_BOT_MAP.get(botId).getPositions().find { it.id == posId }
    }

    List<Position> findOpenPositions(TradeBot bot) {
        return new ArrayList<Position>(bot.getPositions().findAll{ !it.closed && it.buyDate != null })
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

        settings.traceClosedPosition = false
        pos.settings = settings

        if(settings.buyWhen.spend &&  settings.buyWhen.spend > 0) {
            settings.buyWhen.quantity = 0
        }

        //applyBotSettings(bot, pos.settings)

        save(pos)
        bot.addPosition(pos)

        log.debug "New buyWehn position created! $pos"

        // start the watcher service to observe the market
        if(!bot.backtest)
            marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))

        return pos
    }

    Position openPosition(TradeBot bot, Signal s) {

        if(!isTradingActive(bot)) {
            return null
        }

        Position pos = new Position()

        pos.created = new Date()
        pos.botId = bot.getId()
        pos.market = bot.config.baseCurrency + '-' + s.asset
        pos.status = "pending"
        pos.buySignalId = s.getId()
        pos.signalRate = s.price

        log.info(pos.market)

        applyBotSettings(bot, pos.settings)

        BigDecimal balanceToSpend = tradeBotManager.calcBalanceForNextTrade(bot)

        if(balanceToSpend > 0) {

            save(pos)
            bot.addPosition(pos)

            openPosition(bot, pos, balanceToSpend, s.getPrice(), s.signalDate)
        } else {
            log.warn("Can't open position ${s.asset} balance is to low. ${balanceToSpend}")
        }

        return pos
    }

    void applyBotSettings(TradeBot bot, PositionSettings settings) {

        Map config = bot.config

        settings.stopLoss = config.stopLoss as StopLoss
        settings.trailingStopLoss = config.trailingStopLoss as TrailingStopLoss
        settings.takeProfit = config.takeProfit as TakeProfit
    }


    Position openPosition(TradeBot bot, Position pos, BigDecimal balanceToSpend, BigDecimal signalPrice, Date signalDate) {

        if(!isTradingActive(bot)) {
            def msg = "Trading is not active bot bot ${bot}"
            log.error(msg)
            pos.buyInPogress = false
            pos.setError(true)
            pos.setErrorMsg(msg)
            pos.setClosed(true)
            return pos
        }

        def startTime = LocalDateTime.now()

        IExchangeAdapter exchangeAdapter = tradeBotManager.getExchangeAdapter(bot)

        PriceLimit priceLimit = null
        if(bot.config.buyPriceLimitPercent && signalPrice) {
            priceLimit = new PriceLimit(signalPrice,(BigDecimal) bot.config.buyPriceLimitPercent)
        }

        CryptoMarket market = new CryptoMarket(bot.exchange, pos.getMarket())
        String currency  = market.getCurrency()
        String asset = market.getAsset()

        ExchangeResponse<IOrder> newOrderRes = orderExecutorService.placeLimitOrder(bot, exchangeAdapter, BuySell.BUY, balanceToSpend, (PriceLimit) priceLimit, currency, asset, pos)

        if(newOrderRes.success) {

            IOrder newOrder = newOrderRes.getResult()

            updateBuyPosition(pos, newOrder)
            log.debug "New position created! $pos"

            // start the watcher service to observe the market
            if(!bot.backtest)
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

        save(pos)
        log.info("Took ${ChronoUnit.MILLIS.between(startTime,  LocalDateTime.now())} ms to open the signal!")

        return pos
    }

    boolean isTradingActive(TradeBot bot) {
        if(bot && bot.config) {

            if(bot.backtest) return true

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

        if(bot.backtest) {
            log.warn("Can't load position for a backtest Bot!")
            return
        }

        IExchangeAdapter exchangeAdapter = tradeBotManager.getExchangeAdapter(bot)

        List<IBalance> balances = exchangeAdapter.getBalances()

        log.info("Importing position from exchange: ${exchangeAdapter.getExchangeName()}" )

        //List<IOrder> orderHistory =  exchangeAdapter.get
        balances.findAll { it.getCurrency() != bot.getBaseCurrency() } .each { IBalance balance ->

            def market = "${bot.config.baseCurrency}-${balance.getCurrency()}"

            // check if balance already exists
            def alreadyOpenQuantity = 0
            def openPositions = findOpenPositions(bot).findAll { it.market == market }
            if(openPositions) {
                alreadyOpenQuantity = openPositions.sum { it.amount  }
            }

            def quantity = balance.getValue() - alreadyOpenQuantity

            if(quantity > 0.0001) {

                List<IOrder> orders = orderRepository.findByExchangeAndMarketAndBuySellOrderByDateDesc(exchangeAdapter.getExchangeName(), market, "buy")

                //def quantityLeft = balance.getAvailable()

                Order matchingOrder = orders.find { it.quantity == quantity }

                if(matchingOrder) {

                    def order = matchingOrder

                    Position pos = new Position()

                    pos.created = new Date()
                    pos.botId = bot.getId()
                    pos.buySignalId = -1
                    pos.market = market
                    pos.status = "open"
                    pos.signalRate = 0
                    pos.setAmount( order.getQuantity() )
                    pos.settings.holdPosition = true

                    pos.setExtbuyOrderId(order.getExtOrderId())
                    pos.setBuyFee(order.getCommission())
                    pos.setBuyDate(order.getDate())
                    pos.setBuyRate(order.getPricePerUnit())

                    setStartFx(bot, pos)

                    save(pos)
                    bot.addPosition(pos)

                    marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))

                } else {

                    log.warn("No historic order found for market ${market} on exchange ${exchangeAdapter.getExchangeName()} and quantity: ${quantity}")

                    Position pos = new Position()

                    pos.created = new Date()
                    pos.botId = bot.getId()
                    pos.buySignalId = -1
                    pos.market = market
                    pos.status = "open"
                    pos.signalRate = 0
                    pos.setAmount( quantity )
                    pos.settings.holdPosition = true

                    CryptoMarket cm = new CryptoMarket(exchangeAdapter.getExchangeName(), market)

                    Order lastOrder = orderService.findLastBuyOrderForAsset(cm)
                    if(lastOrder) {
                        pos.setBuyDate(lastOrder.getDate())
                        setStartFx(bot, pos)

                    } else {
                        pos.setBuyDate(new Date())
                    }

                    pos.setBuyFee(0)

                    if(orders) {
                        // use the buy rate from the last order
                        pos.setBuyRate( orders.first().getPricePerUnit() )

                        save(pos)
                        bot.addPosition(pos)
                        marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))
                    } else {
                        // use the ticker
                        ExchangeResponse<ITicker> tickerExchangeResponse = exchangeAdapter.getTicker(pos.getMarket())

                        if(tickerExchangeResponse.success) {
                            ITicker ticker = tickerExchangeResponse.getResult()


                            pos.setBuyRate(ticker.getAsk())

                            save(pos)
                            bot.addPosition(pos)
                            marketWatcheService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))

                        } else {
                            log.error("Can create postion for market ${pos.getMarket()}. ${tickerExchangeResponse.getMessage()}")
                        }
                    }
                }
            }
         }
    }

    void setStartFx(TradeBot bot, Position pos) {

        if(bot.config.baseCurrency == 'BTC' && pos.getBuyDate() != null) {

            Instant startI = pos.getBuyDate().toInstant().truncatedTo( ChronoUnit.MINUTES )
            Instant endI = startI.plus(2, ChronoUnit.MINUTES)

            List<Candle> candles = candleStore.find("1min", "Binance", "USDT-BTC",LocalDateTime.ofInstant(startI, ZoneOffset.UTC), LocalDateTime.ofInstant(endI, ZoneOffset.UTC))

            if(candles) {
                pos.setBuyFx( candles.first().close )
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

        save(pos)
    }


    void save(Position pos) {
        synchronized (pos) {
            TradeBot bot = tradeBotManager.findBotById(pos.getBotId())

            if(!bot.backtest || pos.id == null) {
                positionRepository.save(pos)
            }
        }
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

    void clonePosition( Position pos) {

        log.info("Cloning position ${pos.id} ${pos.market}")

        TradeBot bot = tradeBotManager.findBotById( pos.getBotId() )

        Position newPos = pos.clone()
        newPos.setId(null)
        save( newPos )
        bot.addPosition( newPos )


    }

    Position closePosition(Position pos, BigDecimal sellPrice, TradeBot bot, Date closeDate) {

        if(!isTradingActive(bot)) {
            def msg = "Trading is not active bot bot ${bot}"
            log.error(msg)
            pos.sellInPogress = false
            pos.setError(true)
            pos.setErrorMsg(msg)
            return pos
        }

        def startTime = LocalDateTime.now()

        PriceLimit priceLimit = null
        if(bot.config.sellPriceLimitPercent && sellPrice) {
            priceLimit = new PriceLimit(sellPrice, (BigDecimal) bot.config.sellPriceLimitPercent)
        }

        pos.sellInPogress = true
        save(pos)

        IExchangeAdapter exchangeAdapter = tradeBotManager.getExchangeAdapter(bot)

//        if(bot.backtest) {
//            // A new instance will be returned in case of a paper exchange
//            // fake a candle
//            Candle c = new Candle()
//            c.close = sellPrice
//            c.end = closeDate
//            c.market = new CryptoMarket(bot.exchange, pos.market)
//
//            exchangeAdapter = tradeBotManager.getPaperExchangeAdapter( bot, pos, c)
//        } else {
//
//        }


        ExchangeResponse<IOrder> newOrderRes = orderExecutorService.placeLimitOrder(bot, exchangeAdapter, BuySell.SELL, pos.amount, (PriceLimit) priceLimit, pos.market, pos)

        if(newOrderRes.success) {

            IOrder newOrder = newOrderRes.getResult()
            updateSellPosition(pos, newOrder)

            log.debug "position ${pos.id} closed! ${NumberHelper.twoDigits(pos.result)}%"
            save(pos) // save before stopping the market manager

            try {
                marketWatcheService.stopMarketWatcher(bot.getExchange(), pos.getMarket())
            } catch(Exception e) {
                log.error("Error while trying to stop the market watcher!")
            }

            tradeBotManager.syncBanlance(bot)

            if(pos.settings.rebuy && pos.settings.rebuy.enabled) {
                // open a new position
                PositionSettings settings = pos.settings.clone()

                // we have closed a postion we want to rebuy if the price has fallen...
                settings.buyWhen.enabled = true

                def earnings = pos.amount * pos.sellRate // - fee?

                settings.buyWhen.minPrice = pos.sellRate * (1+((pos.settings.rebuy.value-1)/100))
                settings.buyWhen.maxPrice = pos.sellRate * (1+((pos.settings.rebuy.value)/100))
                settings.buyWhen.quantity = 0
                settings.buyWhen.spend = earnings

                newPosition(bot, pos.market, settings)
            }

        } else {
            log.error("Error on Sell: ${newOrderRes.getMessage()}")

            pos.setError(true)
            pos.setErrorMsg(newOrderRes.getMessage())
            pos.setClosed(false)
            pos.sellInPogress = false

            save(pos)
        }

        log.info("Took ${ChronoUnit.MILLIS.between(startTime, LocalDateTime.now())} ms to open the signal!")

        return pos
    }
}
