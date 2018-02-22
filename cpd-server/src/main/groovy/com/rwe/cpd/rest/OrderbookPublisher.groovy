package com.rwe.cpd.rest

import com.rwe.cpd.couchdb.model.Orderbook
import com.rwe.cpd.service.OrderbookService
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Commons
class OrderbookPublisher {


    MessageSendingOperations<String> messagingTemplate

    @Autowired
    OrderbookService orderbookService


    @Autowired
    OrderbookPublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }


    @Scheduled(initialDelay=5000l, fixedRate=1000l)
    void publichChangedOrderbooks() {

        //synchronized (orderbookService.orderBookCache) {
            def dirtyBooks = new ArrayList<Orderbook>(orderbookService.orderBookCache.values()).findAll { it.hasChanged }

            def orderbooks  = dirtyBooks.collect { Orderbook orderbook ->

                if(orderbook) {
                    orderbook = orderbookService.updateOrderbook(orderbook, 25)

                    orderbook.hasChanged = false
                }

                // TODO: user config to determine max orderbook entries
                return orderbook
            }.findAll { it != null }

            if(orderbooks.size() > 0) {
                log.info("Publishing ${dirtyBooks.size()} orderbooks!")
                messagingTemplate.convertAndSend("/topic/orderbook".toString() , orderbooks)
            }

       // }
    }



}
