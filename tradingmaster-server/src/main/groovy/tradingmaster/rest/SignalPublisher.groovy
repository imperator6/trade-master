package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.stereotype.Controller
import tradingmaster.db.SignalRepository
import tradingmaster.db.entity.Signal

import javax.annotation.PostConstruct

@Controller
@Commons
class SignalPublisher implements MessageHandler {

    @Autowired
    SignalRepository signalRepository

    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel executedSignalChannel

    @Autowired
    SignalPublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }


    @PostConstruct
    init() {
        executedSignalChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        //log.info("Publishing new signal signal")

        Signal s =  message.getPayload()

        messagingTemplate.convertAndSend("/topic/signal".toString() , s)
        signalRepository.save( message.getPayload() )
    }


}
