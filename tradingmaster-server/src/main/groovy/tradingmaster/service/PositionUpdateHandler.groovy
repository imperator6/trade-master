package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tradingmaster.db.PositionRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.entity.json.StopLoss
import tradingmaster.db.entity.json.TakeProfit
import tradingmaster.db.entity.json.TrailingStopLoss
import tradingmaster.model.BacktestMessage
import tradingmaster.model.Candle
import tradingmaster.util.NumberHelper

import javax.annotation.PostConstruct
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
@Commons
class PositionUpdateHandler implements  MessageHandler {


    @Autowired
    PublishSubscribeChannel mixedCandelSizesChannel

    @Autowired
    PublishSubscribeChannel fxDollarChannel

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PositionRepository positionRepository

    @Autowired
    TaskExecutor positionTaskExecutor

    @Autowired
    PositionService positionService

    @Autowired
    AlertService alertService

    @Autowired
    PublishSubscribeChannel positionUpdateChannel

    @Autowired
    PublishSubscribeChannel backtestChannel


    @Autowired
    PublishSubscribeChannel signalChannel

    @PostConstruct
    init() {
        mixedCandelSizesChannel.subscribe(this)
    }

    @Scheduled(initialDelay=120000l, fixedRate=60000l)
    void checkPositionIsUpToDate() {

        log.debug("Checking if positions are up to date...")

        tradeBotManager.getActiveBots().each { TradeBot bot ->

            String candelSizeM = bot.config.candleSize // 1m, 15 30m....
            def candleSize = candelSizeM.replace("m", "") as Integer
            def timeout = candleSize * 3

            bot.getPositions().findAll { !it.closed && it.lastUpdate != null }.each { Position p ->

                ZonedDateTime positionCreateDate = p.lastUpdate.toInstant().atZone(ZoneOffset.UTC)
                ZonedDateTime now = new Date().toInstant().atZone(ZoneOffset.UTC)
                def minutes = ChronoUnit.MINUTES.between(positionCreateDate, now)

                if(minutes > timeout) {
                    log.warn("!! Position $p.id $p.market has not been updated since $minutes minutes! LastUpdate ${p.lastUpdate} !!")
                } else {
                    log.debug("Position $p.id $p.market is up to date. Last update < $timeout minutes!")
                }
            }
        }
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Candle c = message.getPayload()

        executeAsync(c)
    }

    void executeAsync(Candle c) {
        log.debug("Processing position for market: ${c.getMarket().getName()} candlesize: ${c.getPeriod()} ${c.market.exchange}")

        List botList = tradeBotManager.getActiveBots()

        if(c.botId != null) {
            botList = []
            botList.add( tradeBotManager.findBotById(c.botId))
        }

        def task = {

            List<Signal> signals = []

            botList.each { TradeBot bot ->

                if(bot.getStrategyRunner()) {

                    List strategySignals =  bot.getStrategyRunner().nextCandle(c)

                    strategySignals.each { Signal s ->
                        signals.add(s)
                    }
                }

                processBotUpdate(bot, c)

                List signalsFromUpdateCheck = processPositions(bot, c)
                signals.addAll( signalsFromUpdateCheck )
            }

            // TODO... remove double signlas....
            signals = signals.unique { a, b -> a.positionId <=> b.positionId }

            // send all signals
            signals.each {
                it.candle = c
                signalChannel.send( MessageBuilder.withPayload(it).build() )
            }

            if(c.backtestId != null) {

                BacktestMessage msg = new BacktestMessage()
                msg.backtestId = c.backtestId
                msg.action = "complete"
                msg.signalCount = signals.size()

                if(signals.isEmpty()) {
                    // no actions --> notify strategy runner to fire next candle
                    backtestChannel.send( MessageBuilder.withPayload(msg).build()  )
                } else {
                    // Todo.. register signal count....
                    msg.action = "setSignalCount"

                   // Thread.sleep( 150 )
                    backtestChannel.send( MessageBuilder.withPayload(msg).build() )
                }
            }

        } as Runnable

        positionTaskExecutor.execute(task)

    }


    void processBotUpdate(TradeBot bot, Candle c) {

        if(!isValidCandleSize(bot, c)) {
            return
        }

        // Exchange check needed?
       if(bot.exchange.equalsIgnoreCase(c.getMarket().getExchange())) {
           boolean isUSDBase = bot.getBaseCurrency().toUpperCase().indexOf("USD") >= 0
        if(c.getMarket().getName().equalsIgnoreCase("USDT-${bot.baseCurrency}") || isUSDBase) {
            if(isUSDBase) {
                bot.setFxDollar( 1 )
            } else {
                bot.setFxDollar( c.getClose() )

                if(!bot.backtest)
                    fxDollarChannel.send( MessageBuilder.withPayload( c ).build() )
            }

            // update start fx for position if not set
            bot.positions.findAll { p -> (p.buyFx == null || p.buyFx <= 0) && p.buyDate != null }.each {
                log.info("Setting buyFx for pos $it.id $it.market to ${bot.fxDollar}")
                it.setBuyFx(bot.fxDollar)
            }

            bot.startBalanceDollar = bot.startBalance * bot.fxDollar
            bot.currentBalanceDollar = bot.currentBalance * bot.fxDollar
            bot.totalBaseCurrencyValue = bot.currentBalance
            bot.totalBalanceDollar = bot.currentBalanceDollar

            bot.getPositions().findAll { !it.closed }.each {
                if(it.lastKnowBaseCurrencyValue != null) {
                    bot.totalBalanceDollar += it.lastKnowBaseCurrencyValue * bot.fxDollar
                }
                if(it.lastKnowBaseCurrencyValue != null) {
                    bot.totalBaseCurrencyValue += it.lastKnowBaseCurrencyValue
                }
            }
            bot.result = NumberHelper.xPercentFromBase(bot.startBalanceDollar, bot.totalBalanceDollar)
        }
       }
    }

    boolean isValidCandleSize(TradeBot bot, Candle c) {

        // period is always in minutes
        def candelSize = c.getPeriod().replace("min", "m")

        if(bot.config.candleSize) {

            if(candelSize.equalsIgnoreCase( bot.config.candleSize)) {
                return true
            }

        } else  {
            log.warn("No candle size configured for bot ${bot.id} ${bot.exchange}")

            if("1m".equals(candelSize)) {
                return true
            }
        }

        log.debug("${bot.shortName} -> Skipping candle for market ${c.market.name} candleSize: ${c.period}")

        return false
    }


    List<Signal> processPositions(TradeBot bot, Candle c) {

        if(!isValidCandleSize(bot, c)) {
            return Collections.EMPTY_LIST
        }

        List signals = []

        String candleMarket = c.getMarket().getName()

        // Check exchange
        if(bot.exchange.equalsIgnoreCase(c.getMarket().getExchange())) {
            bot.getPositions().findAll {
                (!it.closed && it.settings.traceClosedPosition)
                        candleMarket.equalsIgnoreCase(it.market) }.each { p ->

                log.debug("${bot.shortName} -> Processing position for market: ${c.getMarket().getName()} candlesize: ${c.getPeriod()} ${c.market.exchange}")

                if(checkOpenPosition(p, c, bot)) {
                    Signal s = openPosition(p, c, bot)
                    signals.add( s )
                    return signals
                }

                updatePosition(p, c, bot)

                if(checkClosePosition(p, c, bot)) {
                    Signal s = closePosition(p, c, bot)
                    if(s) signals.add( s )
                }
            }
        }

        return signals
    }

    BigDecimal calculatePositionResult(BigDecimal buyRate, BigDecimal rate, BigDecimal fallback) {

        def resultInPercent = calculatePositionResult(buyRate, rate)

        if(resultInPercent > 500) {
            log.error("calculatePositionResult: result is > 500% invalid rate? $rate buyRate: $buyRate")

            if(fallback != null)
                return fallback
        }

        return resultInPercent
    }

     BigDecimal calculatePositionResult(BigDecimal buyRate, BigDecimal rate) {

         if(buyRate == null || rate == null || rate == 0 || buyRate == 0) {
             return 0.0
         }

         def diff = (1/rate) - (1/buyRate)
         BigDecimal resultInPercent = diff / (1/rate) * 100

         if(buyRate > rate) {
             resultInPercent = resultInPercent.abs() * -1
         } else {
             resultInPercent = resultInPercent.abs()
         }


         return resultInPercent
    }


    private updatePosition(Position p, Candle c, TradeBot bot) {

        if(p.sellInPogress) {
            log.info("Skip update position $p.id. Sell is already in pogress! Candle ${c.end}  ${c.close}")
            return
        }

        log.debug("Updating position $p.id for candle: $c.end")

        p.lastKnowRate = c.close
        if(p.amount != null)
            p.lastKnowBaseCurrencyValue = c.close * p.amount

        p.lastUpdate = new Date()

        // calc age...
        if(!p.closed) {
            ZonedDateTime positionCreateDate = p.created.toInstant().atZone(ZoneOffset.UTC)

            // use buy date if set
            if(p.buyDate != null) positionCreateDate = p.buyDate.toInstant().atZone(ZoneOffset.UTC)

            ZonedDateTime now = p.lastUpdate.toInstant().atZone(ZoneOffset.UTC)
            def minutes = ChronoUnit.MINUTES.between(positionCreateDate, now)
            if(minutes > 59) {
                def hours = ChronoUnit.HOURS.between(positionCreateDate, now)

                if(hours > 24) {
                    def days = ChronoUnit.DAYS.between(positionCreateDate, now)
                    p.setAge("$days Days")
                } else {
                    p.setAge("$hours Hours")
                }
            } else {
                p.setAge("$minutes Min.")
            }

            BigDecimal resultInPercent = calculatePositionResult(p.getBuyRate(), c.close, p.result)
            p.setResult(resultInPercent)

            // waiting for buy
            if((p.buyRate == null || p.buyRate <= 0) &&
                    p.settings && p.settings.buyWhen
                    && p.settings.buyWhen.enabled) {

                if(c.close > p.settings.buyWhen.maxPrice) {
                    p.result = calculatePositionResult(p.settings.buyWhen.maxPrice, c.close, p.result)
                } else if(c.close < p.settings.buyWhen.minPrice) {
                    p.result = calculatePositionResult(p.settings.buyWhen.minPrice, c.close, p.result)
                }
            } else {
                // open position
                alertService.checkAlert(bot, p)
                // check alert!

            }

            if(p.minResult == null || resultInPercent < p.minResult) {
                p.minResult = resultInPercent
            }

            if(p.maxResult == null || resultInPercent > p.maxResult) {
                p.maxResult = resultInPercent
            }

        } else {
            // a closed position with setting traceClosed!
            if(p.getSellRate() != null) {
                BigDecimal traceResultInPercent = calculatePositionResult(p.getSellRate(), c.close, p.traceResult)
                p.traceResult = traceResultInPercent
            }
        }

        positionService.save(p)

        if(!bot.backtest)
            positionUpdateChannel.send(  MessageBuilder.withPayload( p ).build() )

        // buyRate: $p.buyRate curentRate: $c.close
        log.debug("PosId $p.id: $p.market: (range:${NumberHelper.twoDigits(p.minResult)}%  ${NumberHelper.twoDigits(p.maxResult)}%) -> ${NumberHelper.twoDigits(resultInPercent)}%")
    }

    Integer getAgeInHours(Position p) {
        ZonedDateTime positionCreateDate = p.created.toInstant().atZone(ZoneOffset.UTC)
        ZonedDateTime now = new Date().toInstant().atZone(ZoneOffset.UTC)
        def hours = ChronoUnit.HOURS.between(positionCreateDate, now)
        return hours
    }

    boolean checkOpenPosition(Position p, Candle c, TradeBot bot) {

        if((p.buyRate == null || p.buyRate <= 0) &&
                p.settings && p.settings.buyWhen
                && p.settings.buyWhen.enabled) {

            // check timeout
            def hours = getAgeInHours(p)

            if(hours <= p.settings.buyWhen.timeoutHours) {
                // valid
                def price = c.close
                if(price >= p.settings.buyWhen.minPrice && price <= p.settings.buyWhen.maxPrice ) {
                    log.info("Position ${p.market} is now in the given price range! price: ${price} range: ${p.settings.buyWhen.minPrice} - ${p.settings.buyWhen.maxPrice}")
                    return true
                }
            } else {
                // timeout
                log.info("Timout for position ${p.id} ${p.market}. ")
                p.closed = true
                p.setError(true)
                p.setErrorMsg("Timeout! Position is older than ${p.settings.buyWhen.timeoutHours} hours.")
                positionService.save(p)
            }
        }

        return false
    }

    boolean checkClosePosition(Position p, Candle c, TradeBot bot) {

        if(p.sellInPogress) {
            log.info("Skip check close position $p.id. Sell is already in pogress!")
            return false
        }

        if(p.buyRate == null || p.buyRate <= 0) {
            return false
        }

        if(p.closed) {
            return false
        }

        if(p.settings && p.settings.holdPosition) {
            return false
        }

        Map config = bot.config
        def age = getAgeInHours(p)
        BigDecimal positionValueInPercent = p.result

        StopLoss stopLoss = p.settings.stopLoss //config.stopLoss as StopLoss

        if(stopLoss && stopLoss.enabled) {

            def skipStopLoss = false

            // check if activeAfterHours
            if(stopLoss.activeAfterHours != null && stopLoss.activeAfterHours > 0 ) {
                if(age < stopLoss.activeAfterHours) {
                    skipStopLoss = true
                    log.debug("Skipping StopLoss deal is too jung (${age} hours old)")
                }
            }

            if(!skipStopLoss && positionValueInPercent <= stopLoss.value) {
                log.info("Stop Loss <= ${stopLoss.value}% detected: Position $p.id: $p.market result: ${positionValueInPercent}% candle: ${c}")
                return true
            }
        }

        TrailingStopLoss trailingStopLoss = p.settings.trailingStopLoss //config.trailingStopLoss as TrailingStopLoss

        if(trailingStopLoss && trailingStopLoss.enabled) {

            def skipTrailingStopLoss = false

            // check if activeAfterHours
            if(trailingStopLoss.activeAfterHours != null && trailingStopLoss.activeAfterHours > 0 ) {
                if(age < trailingStopLoss.activeAfterHours) {
                    skipTrailingStopLoss = true
                    log.debug("Skipping TrailingStopLoss deal is too jung (${age} hours old)")
                }
            }

            // check interval
            if(/*p.trailingStopLoss != null && */ trailingStopLoss.checkInterval > 1) {
                def minute = c.getEnd().getMinutes()
                def modulo = minute % trailingStopLoss.checkInterval
                if(modulo != 0) {
                    log.debug("Skipping TrailingStopLoss checkInterval does not match! Minute is $minute Interval: ${trailingStopLoss.checkInterval})")
                    skipTrailingStopLoss = true
                } else {
                    log.debug("TrailingStopLoss checkInterval does match: Minute is $minute Interval: ${trailingStopLoss.checkInterval}")
                }

            }

            if(!skipTrailingStopLoss) {

                // check if we need to sell
                if(p.trailingStopLoss != null && positionValueInPercent <= p.trailingStopLoss) {
                    log.info("Trailing-Stop-Loss <= ${p.trailingStopLoss}% detected: Position $p.id: $p.market result: ${positionValueInPercent}%")
                    return true
                }

                // update trailing
                if(p.trailingStopLoss != null) {
                    BigDecimal newTrailingStopLoss = positionValueInPercent - trailingStopLoss.value
                    if(newTrailingStopLoss > p.trailingStopLoss) {
                        log.info("Increase Trailing-Stop-Loss for Position $p.id: $p.market new: ${newTrailingStopLoss}%")
                        p.trailingStopLoss = newTrailingStopLoss
                        positionService.save(p)
                    }
                }

                // activate trailing
                if(p.trailingStopLoss == null && positionValueInPercent >= trailingStopLoss.startAt) {
                    BigDecimal trailingStopLossInitValue = positionValueInPercent - trailingStopLoss.value
                    log.info("Activate Trailing-Stop-Loss for Position $p.id: $p.market trailingStopLoss at: $trailingStopLossInitValue")
                    p.trailingStopLoss = trailingStopLossInitValue
                    positionService.save(p)
                }
            }
        }

        TakeProfit takeProfit = p.settings.takeProfit //config.takeProfit as TakeProfit

        if(takeProfit && takeProfit.enabled) {

            def skipTakeProfit = false

            // check if activeAfterHours
            if(takeProfit.activeAfterHours != null && takeProfit.activeAfterHours > 0 ) {
                if(age < takeProfit.activeAfterHours) {
                    skipTakeProfit = true
                    log.debug("Skipping TakeProfit deal is too jung (${age} hours old)")
                }
            }


            if(!skipTakeProfit && positionValueInPercent >= takeProfit.value) {
                log.info("Take-Profit for Position $p.id: $p.market profit: ${positionValueInPercent}%")
                return true
            }
        }

        return false
    }

    Signal closePosition(Position p, Candle c, TradeBot bot) {

        if(p.settings && p.settings.holdPosition) {
            log.info("Can't close position $p.id. Flag HoldPosition is active!")
            return
        }

       /* if(p.sellInPogress) {
            log.info("Can't close position $p.id. Sell is already in pogress!")
            return
        }*/

        if(!positionService.isTradingActive(bot)) {
            return
        }

        //p.sellInPogress = true

        Signal s = new Signal()
        s.buySell = "sell"
        s.asset = c.getMarket().getAsset()
        s.price = c.close
        s.signalDate = c.end
        s.exchange = bot.exchange
        s.triggerName = "Close Trigger"
        s.positionId = p.id
        s.botId = bot.id
        s.candle = c

        return s

        //signalChannel.send( MessageBuilder.withPayload(s).build() )

      /*  def task = {




            positionService.closePosition(p, c.close, bot, c.end)
        } as Runnable

        orderTaskExecutor.execute(task) */
    }

    Signal openPosition(Position p, Candle c, TradeBot bot) {

        if(p.settings && p.settings.holdPosition) {
            log.info("Can't close position $p.id. Flag HoldPosition is active!")
            return
        }

        if(p.buyInPogress) {
            log.info("Can't open position $p.id. Buy is already in pogress!")
            return
        }

        if(!positionService.isTradingActive(bot)) {
            return
        }

        p.buyInPogress = true

        Signal s = new Signal()
        s.buySell = "buy"
        s.asset = c.getMarket().getAsset()
        s.price = c.close
        s.signalDate = c.end
        s.exchange = bot.exchange
        s.triggerName = "Open Trigger"
        s.positionId = p.id
        s.botId = bot.id
        s.candle = c

        return s

        //signalChannel.send( MessageBuilder.withPayload(s).build() )



//        def task = {
//            // calc balance....
//            def balanceToSpend
//
//            if(p.settings.buyWhen.spend > 0) {
//                balanceToSpend = p.settings.buyWhen.spend
//                log.info("Open a new position using 'buyWhen.spend'. balanceToSpend: ${balanceToSpend}")
//            } else if (p.settings.buyWhen.quantity > 0) {
//                balanceToSpend = p.settings.buyWhen.quantity * c.close
//                p.settings.buyWhen.spend = balanceToSpend
//                log.info("Open a new position using 'buyWhen.quantity'. balanceToSpend: ${balanceToSpend}")
//            } else {
//                balanceToSpend = tradeBotManager.calcBalanceForNextTrade(bot)
//                p.settings.buyWhen.spend = balanceToSpend
//                log.info("Open a new position using 'tradeBotManager'. balanceToSpend: ${balanceToSpend}")
//            }
//
//            positionService.openPosition(bot, p, balanceToSpend , c.close, exchangeAdapter)
//        } as Runnable
//
//        orderTaskExecutor.execute(task)
    }

}
