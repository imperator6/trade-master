package tradingmaster.exchange.bittrex

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.model.IMarket
import tradingmaster.model.ITrade

@Service
@Commons
class Bittrex implements IExchangeAdapter {

    @Autowired
    BittrexApi api


    @Override
    List<ITrade> getTrades(Date startDate, Date endDate, IMarket market) {


        BittrexApi.Response res = api.getMarketHistory(market.getName())

        if(res.success) {

            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(res.result, new TypeReference<List<BittrexTrade>>(){})


           // log.info(res.getResult())

        } else {
            log.error("getTrades was not successful. message: $res.message" )
        }

        return []
    }
}
