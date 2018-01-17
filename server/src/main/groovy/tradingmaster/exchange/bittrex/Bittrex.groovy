package tradingmaster.exchange.bittrex

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.bittrex.model.BittrexMarketResponse
import tradingmaster.exchange.bittrex.model.BittrexOrder
import tradingmaster.exchange.bittrex.model.BittrexOrderResponse
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

    List<BittrexOrder> getOrderHistory() {

        BittrexOrderResponse res = exchange.get("account/getorderhistory", new ParameterizedTypeReference<BittrexOrderResponse>(){})

        if(res.success) {
            return res.getResult()
        }

        log.error("Error: ${res.getMessage()}")

        return []
    }

    List<CryptoMarket> getMakets() {

        BittrexMarketResponse info = exchange.get("public/getmarkets", new ParameterizedTypeReference<BittrexMarketResponse>(){})
        return info.getResult().collect { new CryptoMarket(name, it.getCurrency(), it.getAsset()) }
    }


    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        Map parmas = ["market": "${market.getCurrency()}-${market.getAsset()}".toString()]

        BittrexTradeResponse res = exchange.get("public/getmarkethistory", parmas,  new ParameterizedTypeReference<BittrexTradeResponse>(){})

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
