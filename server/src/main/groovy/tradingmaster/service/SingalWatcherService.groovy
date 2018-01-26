package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Component
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot

import javax.annotation.PostConstruct

@Component
@Commons
class SingalWatcherService implements MessageHandler {

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PositionService positionService

    @Autowired
    PublishSubscribeChannel signalChannel

    @Autowired
    TaskExecutor signalExecutor

    @PostConstruct
    init() {
        signalChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        Signal s = message.getPayload()

        tradeBotManager.getActiveBots().each { TradeBot bot ->

            def task = {
                handleSignal(bot, s)
            } as Runnable

            signalExecutor.execute(task)
        }
    }

    void handleSignal(TradeBot b, Signal s) {

        if("buy".equalsIgnoreCase( s.getBuySell())) {

            String triggerName = s.getTriggerName()
            boolean skipSignal = true

            if(b.config.signal && b.config.signal.enabled) {
                List<String> listenTo =  b.config.signal.listenTo
                if(listenTo.contains(triggerName)) {
                    skipSignal = false
                }
            }

            if(skipSignal) {
                log.info("Skipping signal for trigger: $triggerName bot: ${b.id}")
                return
            }

            if(tradeBotManager.isValidSignalForBot(b, s)) {
                positionService.openPosition(b, s)
            }

        } else if ("sell".equalsIgnoreCase( s.getBuySell())) {

            // TODO... implement ...

        } else {
            log.error("Unsupported buysell flag ${s.getBuySell()}")
        }
    }
}
