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

        //synchronized (orderbookService.orderBookCache) {

            def orderbooks  = orderbookIds.collect {

                Orderbook orderbook = orderbookService.orderBookCache.get(it)

                return orderbookService.updateOrderbook(orderbook, maxOrderbookEntries)

            }.findAll { it != null }

            result.setData(orderbooks)
        //}

        return result
    }



}
