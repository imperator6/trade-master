package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.stereotype.Controller
import tradingmaster.db.entity.Position

import javax.annotation.PostConstruct
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Controller
@Commons
class PositionPublisher implements MessageHandler {


    MessageSendingOperations<String> messagingTemplate

    Map timestamps = new ConcurrentHashMap()

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

        Position p = message.getPayload()

        if(timestamps.containsKey(p.getMarket())) {
            def diff = ChronoUnit.SECONDS.between( timestamps.get(p.getMarket()),  LocalDateTime.now())

            if(diff< 60) {
                log.debug("Skipping position publish for market ${p.getMarket()}. Diff is $diff")
                return
            }
        }

        timestamps.put(p.getMarket(), LocalDateTime.now())
        messagingTemplate.convertAndSend("/topic/position".toString() , p )
    }

}
