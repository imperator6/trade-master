package tradingmaster.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.stereotype.Controller

import javax.annotation.PostConstruct

@Controller
class PositionPublisher implements MessageHandler {


    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel positionUpdateChannel

    @Autowired
    PositionPublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }

    @PostConstruct
    init() {
        positionUpdateChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        messagingTemplate.convertAndSend("/topic/position".toString() , message.getPayload())
    }

}
