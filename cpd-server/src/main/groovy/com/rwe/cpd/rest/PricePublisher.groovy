package com.rwe.cpd.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.stereotype.Controller

import javax.annotation.PostConstruct
import java.util.concurrent.atomic.AtomicBoolean

@Controller
class PricePublisher implements ApplicationListener<BrokerAvailabilityEvent>, MessageHandler {

    AtomicBoolean brokerAvailable = new AtomicBoolean()

    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel priceChannel


    @Autowired
    PricePublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }

    @PostConstruct
    init() {
        priceChannel.subscribe(this)
    }

    @Override
    void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable())
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {



        messagingTemplate.convertAndSend("/topic/price".toString() , c)
    }

}
