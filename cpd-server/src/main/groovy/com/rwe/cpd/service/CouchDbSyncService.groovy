package com.rwe.cpd.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.rwe.cpd.couchdb.model.Orderbook
import groovy.util.logging.Commons
import org.ektorp.CouchDbConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Commons
class CouchDbSyncService {

    @Autowired
    OrderbookService orderbookService

    @Autowired
    CouchDbConnector orderBookStorage

    ObjectMapper objectMapper

    CouchDbSyncService() {
        this.objectMapper = new ObjectMapper()
        this.objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Scheduled(initialDelay=5000l, fixedRate=5000l)
    void syncDirtyOrderbooks() {

        def dirtyBooks = new ArrayList<Orderbook>(orderbookService.orderBookCache.values()).findAll { it.hasChanged }

        log.info("Updating ${dirtyBooks.size()} orderbooks!")

        dirtyBooks.each { Orderbook orderbook ->

            synchronized (orderbook) {

                log.info(orderbook.id)

                Map bidMap = orderbookService.bidCache.get(orderbook.id)

                Map askMap = orderbookService.askCache.get(orderbook.id)

                if(bidMap) {
                    orderbook.entries.bids = bidMap.values().sort{  it.price }.reverse()

                }

                if(askMap) {
                    orderbook.entries.asks = askMap.values().sort{  it.price }
                }

                Orderbook orderBookWithRev
                try {
                    orderBookWithRev = orderBookStorage.get(Orderbook.class, orderbook.id)
                } catch(all) {

                }


                if(orderBookWithRev) {
                    orderbook.revision = orderBookWithRev.revision


                    //String os = objectMapper.writeValueAsString( orderbook )
                    orderBookStorage.update( orderbook )


                } else {
                   // String os = objectMapper.writeValueAsString( orderbook )
                    orderBookStorage.create( orderbook )
                }

                orderbook.hasChanged = false
            }

        }
    }


}
