package tradingmaster.service.cache

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.ITradeStore
import tradingmaster.model.IMarket

@Service
@Commons
abstract class CacheService<T> {


    Map<String,T> cacheMap = [:]

    CacheService() {
      log.debug("New CacheService!")
    }


    synchronized T get(IMarket market) {
        def key = "${market.exchange}_${market.name}"

        T exchangeEntry = cacheMap.get(key)
        log.debug( "get cache for $market with result of $exchangeEntry")

        return exchangeEntry
    }

    synchronized void set(IMarket market, T value) {
        def key = "${market.exchange}_${market.name}"

        log.debug( "set cache for $market to $value")
        cacheMap.put(key, value)
    }

    synchronized void clear(IMarket market) {
        def key = "${market.exchange}_${market.name}"
        log.debug( "clear cache for $market")
        cacheMap.remove(key)
    }

}
