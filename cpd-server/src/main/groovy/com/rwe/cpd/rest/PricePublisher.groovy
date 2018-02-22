package com.rwe.cpd.rest

import com.rwe.cpd.service.OrderbookService
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.concurrent.atomic.AtomicBoolean

@Service
@Commons
class PricePublisher implements ApplicationListener<BrokerAvailabilityEvent>, MessageHandler {

    AtomicBoolean brokerAvailable = new AtomicBoolean()

    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel priceChannel

    @Autowired
    OrderbookService orderbookService


    @Autowired
    PricePublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }

    @PostConstruct
    init() {
        //priceChannel.subscribe(this)
    }

    @Override
    void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable())
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Map priceMap = message.getPayload()

        // build the id
        priceMap._id = orderbookService.buildId(priceMap)


        messagingTemplate.convertAndSend("/topic/price".toString() , priceMap)
    }

}
