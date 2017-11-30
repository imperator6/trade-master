package tradingmaster.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.stereotype.Controller
import tradingmaster.model.TradeBatch

import javax.annotation.PostConstruct
import java.util.concurrent.atomic.AtomicBoolean

@Controller
class TradeContoller implements ApplicationListener<BrokerAvailabilityEvent>, MessageHandler {

    AtomicBoolean brokerAvailable = new AtomicBoolean()

    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel tradeChannel


    @Autowired
    TradeContoller(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }

    @PostConstruct
    init() {
        tradeChannel.subscribe(this)
    }

    @Override
    void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable())
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        TradeBatch tradeBatch = message.getPayload()
        messagingTemplate.convertAndSend("/topic/trades" , tradeBatch)
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }


}
