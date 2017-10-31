package tradingmaster.service

import org.springframework.stereotype.Service
import tradingmaster.model.IMarket

@Service
class TradeIdService {

    Map exchangeMap = [:]

    synchronized Long getMaxTradeId(String exchange, IMarket market ) {
        Map exchangeEntry = exchangeMap.get(exchange)

        if(exchangeEntry == null) {
            exchangeEntry = [:]
            exchangeMap.put(exchange, exchangeEntry)
        }

        Long marketEntry = exchangeEntry.get(market.getName())

        if(marketEntry == null) {
            marketEntry = -1 as Long
            exchangeEntry.put(market.getName(), marketEntry)
        }
        return marketEntry
    }

    synchronized void setMaxTradeId(String exchange, IMarket market, Long id) {
        Map exchangeEntry = exchangeMap.get(exchange)

        if(exchangeEntry == null) {
            exchangeEntry = [:]
            exchangeMap.put(exchange, exchangeEntry)
        }

        exchangeEntry.put(market.getName(), id)
    }

}
