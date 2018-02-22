package com.rwe.cpd.service

import com.rwe.cpd.couchdb.model.Ask
import com.rwe.cpd.couchdb.model.Bid
import com.rwe.cpd.couchdb.model.Orderbook
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.concurrent.ConcurrentHashMap

@Service
@Commons
class OrderbookService {

    static Map<String, Orderbook> orderBookCache = new ConcurrentHashMap()
    static Map<String, Map<Integer, Bid>> bidCache = new ConcurrentHashMap()
    static Map<String, Map<Integer, Ask>> askCache = new ConcurrentHashMap()

    @Autowired
    ConfigService configService

    void updateOrderBook(Integer priceSeqNum, Map data) {

        String product = buildProduct(data)
        String _id = buildId(data, product)
        Orderbook orderbook = getOrCreateOrderBook(_id, data)

        configService.addProductIfNotExsists(product)

        synchronized (orderbook)  {
            executeAction( data, orderbook )
            orderbook.priceSeqNum = priceSeqNum
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
            b.id = data.PriceID

            return b

        } else if("Offer".equals(data.BuySell)) {

            Ask a = new Ask()
            a.quantity =  data.Volume as BigDecimal
            a.price = data.Price as BigDecimal
            a.broker = data.Broker
            a.id = data.PriceID

            return a
        }
    }

    String buildId(Map data) {

        def product = buildProduct(data)

        return buildId(data, product)
    }


    String buildId(Map data, product) {

        // id ->  S_Country, Type  S_Commodity
        String _id =  "${product}_${data.Expiry}"
        return _id
    }

    String buildProduct(Map data) {

        String type = data.Product ? data.Product : "base"
        if(type.toLowerCase().indexOf("peak") > -1) {
            type = "PEAK"
        } else if (type.toLowerCase().indexOf("offpeak") > -1) {
            type = "OFFPEAK"
        } else {
            type = "BASE"
        }

        // id ->  S_Country, Type  S_Commodity
        String _id =  "${data.S_Country},$type,${data.S_Commodity}"
        return _id
    }

    Orderbook getOrCreateOrderBook(String _id, Map data) {

        synchronized (orderBookCache) {
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


    Orderbook updateOrderbook(Orderbook orderbook, Integer maxOrderbookEntries) {

        if(!orderbook)
            return null

        synchronized (orderbook) {
            Map bidMap = bidCache.get(orderbook.id)

            Map askMap = askCache.get(orderbook.id)

            if(bidMap) {
                orderbook.entries.bids = bidMap.values().sort{  it.price }.reverse()
                orderbook.entries.bids =  orderbook.entries.bids.subList(0, Math.min( maxOrderbookEntries, orderbook.entries.bids.size()-1 ))
            }

            if(askMap) {
                orderbook.entries.asks = askMap.values().sort{  it.price }
                orderbook.entries.asks =  orderbook.entries.asks.subList(0, Math.min( maxOrderbookEntries, orderbook.entries.asks.size()-1 ))
            }
        }

        return orderbook
    }


}
