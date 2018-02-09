package com.rwe.cpd.service

import com.rwe.cpd.couchdb.model.Ask
import com.rwe.cpd.couchdb.model.Bid
import com.rwe.cpd.couchdb.model.Orderbook
import groovy.util.logging.Commons
import org.springframework.stereotype.Service

import java.util.concurrent.ConcurrentHashMap

@Service
@Commons
class OrderbookService {

    static Map<String, Orderbook> orderBookCache = new ConcurrentHashMap()
    static Map<String, Map<Integer, Bid>> bidCache = new ConcurrentHashMap()
    static Map<String, Map<Integer, Ask>> askCache = new ConcurrentHashMap()

    static Set periods = new HashSet()


    void updateOrderBook(Map data) {

        String _id = buildId(data)
        Orderbook orderbook = getOrCreateOrderBook(_id, data)

        synchronized (orderbook)  {
            executeAction( data, orderbook )
            orderbook.hasChanged = true
        }
    }

    void executeAction(Map data, Orderbook orderbook) {

        String action = data.Action

        Map bidOrAsk
        if("Bid".equals(data.BuySell)) {
            bidOrAsk = bidCache.get(orderbook.id)
            if(bidOrAsk == null) bidOrAsk = [:]
            bidCache.put(orderbook.id, bidOrAsk)

        } else if("Offer".equals(data.BuySell)) {
            bidOrAsk = askCache.get(orderbook.id)
            if(bidOrAsk == null) bidOrAsk = [:]
            askCache.put(orderbook.id, bidOrAsk)
        } else {
            log.error("Skipping Action $action -> Unknown 'BuySell' flag '${data.BuySell}'. Data: ${data}")
            return
        }


        Integer priceId = data.PriceID as Integer

        if("Remove".equalsIgnoreCase(action)) {

            bidOrAsk.remove(priceId)

        } else if("Insert".equalsIgnoreCase(action)) {

            bidOrAsk.put(priceId, extractBidOrAsk(data))

        } else if("Update".equalsIgnoreCase(action)) {

            Integer oldPriceId = data.OrigPriceID as Integer

            bidOrAsk.remove(oldPriceId)
            bidOrAsk.put(priceId, extractBidOrAsk(data))

        } else {
            log.error("Skipping Action $action -> Unknown 'Action'  ${action}. Data: ${data}")
            return
        }
    }

    def extractBidOrAsk(Map data) {
        if("Bid".equals(data.BuySell)) {

            Bid b = new Bid()
            b.quantity =  data.Volume as BigDecimal
            b.price = data.Price as BigDecimal
            b.broker = data.Broker

            return b

        } else if("Offer".equals(data.BuySell)) {

            Ask a = new Ask()
            a.quantity =  data.Volume as BigDecimal
            a.price = data.Price as BigDecimal
            a.broker = data.Broker

            return a
        }
    }


    String buildId(Map data) {

        String type = data.Product ? data.Product : "base"
        if(type.toLowerCase().indexOf("peak") > -1) {
            type = "PEAK"
        } else if (type.toLowerCase().indexOf("offpeak") > -1) {
            type = "OFFPEAK"
        } else {
            type = "BASE"
        }

        if(periods.add(data.Expiry)) {
            log.info(data.Expiry)
        }

        // id ->  S_Country, Type  S_Commodity
        String _id =  "${data.S_Country},$type,${data.S_Commodity}_${data.Expiry}"
        return _id

    }

    Orderbook getOrCreateOrderBook(String _id, Map data) {
        Orderbook orderbook = orderBookCache.get(_id)

        if(!orderbook) {
            orderbook = new Orderbook()
            orderbook.id = _id
            orderbook.type = data.S_Commodity
            orderBookCache.put(_id, orderbook)
        }

        return orderbook
    }


}
