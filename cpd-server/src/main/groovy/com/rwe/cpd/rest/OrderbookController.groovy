package com.rwe.cpd.rest

import com.rwe.cpd.couchdb.model.Orderbook
import com.rwe.cpd.service.OrderbookService
import com.rwe.platform.rest.RestResponse
import groovy.util.logging.Commons
import org.ektorp.CouchDbConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orderbook")
@Commons
class OrderbookController {

    @Autowired
    CouchDbConnector configStorage

    @Autowired
    OrderbookService orderbookService

    @RequestMapping(value = "/latest", method = RequestMethod.POST)
    RestResponse<List<Orderbook>> getLatestOrderbook(@RequestParam Integer maxOrderbookEntries, @RequestBody List<String> orderbookIds) {

        log.info(orderbookIds)

        RestResponse<List<Orderbook>> result = new RestResponse<>()

        synchronized (orderbookService.orderBookCache) {

            def orderbooks  = orderbookIds.collect {

                Orderbook orderbook = orderbookService.orderBookCache.get(it)
                if(orderbook) {

                    synchronized (orderbook) {
                        Map bidMap = orderbookService.bidCache.get(orderbook.id)

                        Map askMap = orderbookService.askCache.get(orderbook.id)

                        if(bidMap) {
                            orderbook.entries.bids = bidMap.values().sort{  it.price }.reverse()
                            orderbook.entries.bids =  orderbook.entries.bids.subList(0, Math.min( maxOrderbookEntries, orderbook.entries.bids.size()-1 ))
                        }

                        if(askMap) {
                            orderbook.entries.asks = askMap.values().sort{  it.price }
                            orderbook.entries.asks =  orderbook.entries.asks.subList(0, Math.min( maxOrderbookEntries, orderbook.entries.asks.size()-1 ))
                        }
                    }
                }

                return orderbook
            }.findAll { it != null }

            result.setData(orderbooks)
        }

        return result

    }

}
