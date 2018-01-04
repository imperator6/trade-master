package tradingmaster.exchange.bittrex

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.model.CryptoMarket
import tradingmaster.model.TradeBatch

@Service("Bittrex")
@Commons
class Bittrex extends DefaultExchageAdapter {


    @Autowired
    BittrexApi11 api

    Bittrex() {
        super("Bittrex")
    }

    List<CryptoMarket> getMakets() {
        return [new CryptoMarket(name, "USDT", "ETH")]
    }


    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        List tradeList = []
        String jsonRes = api.getMarketHistory(market.getName())

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        BittrexResponse res = objectMapper.readValue(jsonRes, new TypeReference<BittrexResponse>(){})

        //def jsonSlurper= new JsonSlurper()
        //def res = jsonSlurper.parseText(jsonRes)

        if(res.success) {

           // res.result.collect { objectMapper.}

            tradeList = res.result

        } else {
            log.error("getTrades was not successful. message: $res.message" )
        }

        return new TradeBatch(market, tradeList)
    }
}
