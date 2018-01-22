package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import tradingmaster.core.CandleAggregator
import tradingmaster.db.PositionRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.*
import tradingmaster.strategy.*
import tradingmaster.strategy.runner.CombinedStrategyRun
import tradingmaster.strategy.runner.IStrategyRunner
import tradingmaster.strategy.runner.ScriptStrategyRun

import javax.annotation.PostConstruct
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
@Commons
class PositionUpdateHandler implements  MessageHandler {


    @Autowired
    PublishSubscribeChannel candelChannel1Minute

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PositionRepository positionRepository

    @PostConstruct
    init() {
        candelChannel1Minute.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Candle c = message.getPayload()

        // TODO... skip old candels

        log.info("Processing position for market: ${c.getMarket().getName()}")

        tradeBotManager.getActiveBots().each {
            processPositions(it, c)
        }
    }


    @Async
    void processPositions(TradeBot bot, Candle c) {

        String candleMarket = c.getMarket().getName()

        bot.getPositions().findAll {
            !it.error &&
                !it.closed &&
                candleMarket.equalsIgnoreCase(it.market) }.each { p ->

            updatePosition(p, c, bot)

        }
    }

    private updatePosition(Position p, Candle c, TradeBot bot) {

        log.info("Updating position $p.id for candle: $c.end")

        def currentPrice = c.close

        def buyRate = p.getBuyRate()

        def diff = (1/currentPrice) - (1/buyRate)

        BigDecimal resultInPercent = diff / (1/currentPrice) * 100

        p.setResult(resultInPercent)

        positionRepository.save(p)

        log.info("Position $p.id: $p.market buyRate: $p.buyRate  curentRate: $currentPrice result: $resultInPercent")

        checkClosePosition(p, c, bot)
    }

    void checkClosePosition(Position p, Candle c, TradeBot bot) {

        Map config = bot.config

        if(config.stopLoss && config.stopLoss.enabled) {
            if(p.result <= config.stopLoss.value) {
                log.info("Stop Loss < ${config.stopLoss.value}% detected: Position $p.id: $p.market result: $p.result")

                // TODO... call close...
            }
        }

        // TODO... check trailing stop loss....


        // TODO... check take Profit


    }










}
