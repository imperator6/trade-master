package com.rwe.cpd.kafka

import com.rwe.cpd.service.OrderbookService
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicInteger

@Component
@Commons
class PriceReciver  /* implements ConsumerSeekAware */ {

    @Autowired
    OrderbookService orderbookService

    @Autowired
    PublishSubscribeChannel priceChannel

    AtomicInteger priceSequenze = new AtomicInteger()


    private final ThreadLocal<ConsumerSeekAware.ConsumerSeekCallback> seekCallBack = new ThreadLocal<>()

    void registerSeekCallback(ConsumerSeekAware.ConsumerSeekCallback callback) {
        this.seekCallBack.set(callback);
    }


    @KafkaListener(topics = "Sit.smart.prices")
    void receive(Map data) {

        Integer nextSeq = priceSequenze.incrementAndGet()

        data.priceSeqNum = nextSeq

        orderbookService.updateOrderBook(nextSeq, data)

        //log.info(data)

        priceChannel.send( MessageBuilder.withPayload(data).build() )
    }


}
