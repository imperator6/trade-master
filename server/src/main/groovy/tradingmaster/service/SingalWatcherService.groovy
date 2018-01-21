package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Component
import tradingmaster.db.entity.Signal
import tradingmaster.model.Candle
import tradingmaster.model.ICandleStore

import javax.annotation.PostConstruct

@Component
@Commons
class SingalWatcherService implements MessageHandler {

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PublishSubscribeChannel signalChannel

    @PostConstruct
    init() {
        signalChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        Signal s = message.getPayload()

        tradeBotManager.getActiveBots().each {
            tradeBotManager.handleSignal(it, s)
        }

    }
}
