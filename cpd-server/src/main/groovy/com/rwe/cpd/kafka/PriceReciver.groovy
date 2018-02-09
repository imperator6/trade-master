package com.rwe.cpd.kafka

import com.rwe.cpd.service.OrderbookService
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.stereotype.Component

@Component
@Commons
class PriceReciver  /* implements ConsumerSeekAware */ {

    @Autowired
    OrderbookService orderbookService


    private final ThreadLocal<ConsumerSeekAware.ConsumerSeekCallback> seekCallBack = new ThreadLocal<>()

    void registerSeekCallback(ConsumerSeekAware.ConsumerSeekCallback callback) {
        this.seekCallBack.set(callback);
    }


    @KafkaListener(topics = "Sit.smart.prices")
    void receive(Map data) {



        orderbookService.updateOrderBook(data)
//        prices.each {
//            log.info(it)
//        }
    }


}
