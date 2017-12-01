package tradingmaster.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.stereotype.Controller
import tradingmaster.model.Candle

import javax.annotation.PostConstruct
import java.util.concurrent.atomic.AtomicBoolean

@Controller
class CandlePublisher implements ApplicationListener<BrokerAvailabilityEvent>, MessageHandler {

    AtomicBoolean brokerAvailable = new AtomicBoolean()

    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel candelChannel1Minute


    @Autowired
    CandlePublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }

    @PostConstruct
    init() {
        candelChannel1Minute.subscribe(this)
    }

    @Override
    void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable())
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        Candle c = message.getPayload()
        messagingTemplate.convertAndSend("/topic/candle/1min" , c)
    }

}
