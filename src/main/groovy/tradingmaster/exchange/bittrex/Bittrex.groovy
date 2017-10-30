package tradingmaster.exchange.bittrex

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.IMarket
import tradingmaster.model.TradeBatch

@Service("Bittrex")
@Commons
class Bittrex implements IExchangeAdapter {

    @Autowired
    BittrexApi api


    @Override
    TradeBatch getTrades(Date startDate, Date endDate, IMarket market) {

        List tradeList = []


        BittrexApi.Response res = api.getMarketHistory(market.getName())

        if(res.success) {

            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            tradeList = objectMapper.readValue(res.result, new TypeReference<List<BittrexTrade>>(){})

        } else {
            log.error("getTrades was not successful. message: $res.message" )
        }

        return new TradeBatch(market, this.getClass().getSimpleName(), tradeList )
    }
}
