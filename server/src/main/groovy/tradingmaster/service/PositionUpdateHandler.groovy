package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service
import tradingmaster.db.PositionRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.Candle
import tradingmaster.util.NumberHelper

import javax.annotation.PostConstruct
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
@Commons
class PositionUpdateHandler implements  MessageHandler {


    @Autowired
    PublishSubscribeChannel lastRecentCandelChannel

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PositionRepository positionRepository

    @Autowired
    TaskExecutor positionTaskExecutor

    @Autowired
    TaskExecutor orderTaskExecutor

    @Autowired
    PositionService positionService

    @PostConstruct
    init() {
        lastRecentCandelChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Candle c = message.getPayload()

        log.debug("Processing position for market: ${c.getMarket().getName()}")

        tradeBotManager.getActiveBots().each { TradeBot bot ->

            def task = {
                processBotUpdate(bot, c)
                processPositions(bot, c)
            } as Runnable

            positionTaskExecutor.execute(task)
        }
    }

    void processBotUpdate(TradeBot bot, Candle c) {

        if(c.getMarket().getName().equalsIgnoreCase("USDT-${bot.baseCurrency}")) {
            bot.setFxDollar( c.getClose() )
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


    void processPositions(TradeBot bot, Candle c) {

        String candleMarket = c.getMarket().getName()

        bot.getPositions().findAll {
            !it.error &&
                !it.closed &&
                candleMarket.equalsIgnoreCase(it.market) }.each { p ->

            updatePosition(p, c, bot)

            if(checkClosePosition(p, c, bot)) {
                closePosition(p, c, bot)
            }
        }
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
            log.info("Skip update position $p.id. Sell is already in pogress!")
            return
        }

        log.debug("Updating position $p.id for candle: $c.end")

        p.lastKnowRate = c.close
        if(p.amount != null)
            p.lastKnowBaseCurrencyValue = c.close * p.amount

        p.lastUpdate = new Date()

        // calc age...
        ZonedDateTime positionCreateDate = p.created.toInstant().atZone(ZoneOffset.UTC)
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

        BigDecimal resultInPercent = calculatePositionResult(p.getBuyRate(), c.close)

        p.setResult(resultInPercent)

        if(p.minResult == null || resultInPercent < p.minResult) {
            p.minResult = resultInPercent
        }

        if(p.maxResult == null || resultInPercent > p.maxResult) {
            p.maxResult = resultInPercent
        }

        positionRepository.save(p)

        // buyRate: $p.buyRate curentRate: $c.close
        log.debug("PosId $p.id: $p.market: (range:${NumberHelper.twoDigits(p.minResult)}%  ${NumberHelper.twoDigits(p.maxResult)}%) -> ${NumberHelper.twoDigits(resultInPercent)}%")
    }

    boolean checkClosePosition(Position p, Candle c, TradeBot bot) {

        if(p.sellInPogress) {
            log.info("Skip cehck close position $p.id. Sell is already in pogress!")
            return false
        }

        if(p.holdPosition) {
            return false
        }

        Map config = bot.config
        BigDecimal positionValueInPercent = p.result

        if(p.fixResultTarget != null) {
            // let's onyl sell if we reached the target!
            if(positionValueInPercent >= p.fixResultTarget) {
                log.info("Position $p.id reached the amined target of ${p.fixResultTarget}%.")
                return true
            } else {
                // no other check is needed in this case, as we only sell on te target!
                return false
            }
        }

        if(config.stopLoss && config.stopLoss.enabled) {
            if(positionValueInPercent <= config.stopLoss.value) {
                log.info("Stop Loss <= ${config.stopLoss.value}% detected: Position $p.id: $p.market result: ${positionValueInPercent}%")
                return true
            }
        }

        if(config.trailingStopLoss && config.trailingStopLoss.enabled) {

            if(p.trailingStopLoss != null && positionValueInPercent <= p.trailingStopLoss) {
                log.info("Trailing-Stop-Loss <= ${p.trailingStopLoss}% detected: Position $p.id: $p.market result: ${positionValueInPercent}%")
                return true
            }

            // update trailing
            if(p.trailingStopLoss != null) {
                BigDecimal newTrailingStopLoss = positionValueInPercent - config.trailingStopLoss.value
                if(newTrailingStopLoss > p.trailingStopLoss) {
                    log.info("Increase Trailing-Stop-Loss for Position $p.id: $p.market new: ${newTrailingStopLoss}%")
                    p.trailingStopLoss = newTrailingStopLoss
                    positionRepository.save(p)
                }
            }

            // activate trailing
            if(p.trailingStopLoss == null && positionValueInPercent >= config.trailingStopLoss.startAt) {
                BigDecimal trailingStopLoss = positionValueInPercent - config.trailingStopLoss.value
                log.info("Activate Trailing-Stop-Loss for Position $p.id: $p.market trailingStopLoss at: $trailingStopLoss")
                p.trailingStopLoss = trailingStopLoss
                positionRepository.save(p)
            }
        }

        if(config.takeProfit && config.takeProfit.enabled) {
            if(positionValueInPercent >= config.takeProfit.value) {
                log.info("Take-Profit for Position $p.id: $p.market profit: ${positionValueInPercent}%")
                return true
            }
        }

        return false
    }

    synchronized void closePosition(Position p, Candle c, TradeBot bot) {

        if(p.holdPosition) {
            log.info("Can't close position $p.id. Flag HoldPosition is active!")
            return
        }

        if(p.sellInPogress) {
            log.info("Can't close position $p.id. Sell is already in pogress!")
            return
        }

        p.sellInPogress = true

        def task = {
            positionService.closePosition(p, c, bot)
        } as Runnable

        orderTaskExecutor.execute(task)
    }



}
