package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.model.Candle
import tradingmaster.model.ICandleStore

import javax.annotation.PostConstruct

@Commons
class CandleWriter implements MessageHandler {

    @Autowired
    ICandleStore store

    @Autowired
    PublishSubscribeChannel candelChannel1Minute

    @PostConstruct
    init() {
        candelChannel1Minute.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        Candle c = message.getPayload()
        store.save(c)
    }
}
