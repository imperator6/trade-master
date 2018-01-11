package tradingmaster.exchange.bittrex

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.bittrex.model.BittrexMarket
import tradingmaster.exchange.bittrex.model.BittrexMarketResponse
import tradingmaster.exchange.bittrex.model.BittrexTradeResponse
import tradingmaster.model.CryptoMarket
import tradingmaster.model.TradeBatch

@Service("Bittrex")
@Commons
class Bittrex extends DefaultExchageAdapter {


    @Autowired
    BittrexExchangeImpl exchange

    Bittrex() {
        super("Bittrex")
    }

    List<CryptoMarket> getMakets() {

        BittrexMarketResponse info = exchange.get("public/getmarkets", new ParameterizedTypeReference<BittrexMarketResponse>(){})
        return info.getResult().collect { new CryptoMarket(name, it.getCurrency(), it.getAsset()) }
    }


    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromPath("public/getmarkethistory")

        urlBuilder.queryParam("market", "${market.getCurrency()}-${market.getAsset()}")

        BittrexTradeResponse res = exchange.get(urlBuilder.toUriString(), new ParameterizedTypeReference<BittrexTradeResponse>(){})

        List tradeList = []

        if(res.success) {

           // res.result.collect { objectMapper.}

            tradeList = res.getResult()

        } else {
            log.error("getTrades was not successful. message: $res.message" )
        }

        return new TradeBatch(market, tradeList)
    }
}
