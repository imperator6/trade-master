package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.model.ITrade

@Commons
class CandelBuilder implements MessageHandler {

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        List<ITrade> trades = message.getPayload()

        log.info("Building candels from trades!")
    }
}
