package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import tradingmaster.db.ITradeStore
import tradingmaster.model.IMarket

@Service
@Commons
class TradeIdService {

    @Autowired
    ITradeStore store

    Map exchangeMap = [:]

    TradeIdService() {
      log.info("New TradeIdService!")
    }


    synchronized Long getMaxTradeId( IMarket market ) {
        Map exchangeEntry = exchangeMap.get(market.exchange)

        if(exchangeEntry == null) {
            exchangeEntry = [:]
            exchangeMap.put(market.exchange, exchangeEntry)
        }

        Long marketEntry = exchangeEntry.get(market.getName())

        if(marketEntry == null) {
            marketEntry = store.getMaxTradeId(market) // grabs the latest id out of the database!
            exchangeEntry.put(market.getName(), marketEntry)
        }
        return marketEntry
    }

    synchronized void setMaxTradeId(IMarket market, Long id) {
        Map exchangeEntry = exchangeMap.get(market.exchange)

        if(exchangeEntry == null) {
            exchangeEntry = [:]
            exchangeMap.put(market.exchange, exchangeEntry)
        }

        exchangeEntry.put(market.getName(), id)
    }

}
