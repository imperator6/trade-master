package tradingmaster.exchange.bittrex

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.exchange.bittrex.model.*
import tradingmaster.model.*

@Service("Bittrex")
@Commons
class Bittrex extends DefaultExchageAdapter {

    @Autowired
    BittrexExchangeImpl exchange

    Bittrex() {
        super("Bittrex")
    }


    @Override
    ExchangeResponse<String> sellLimit(String market, BigDecimal quantity, BigDecimal rate) {

        log.info("---------------------- SELL LIMIT ---------------------")
        log.info("- market: $market   quantity: $quantity   rate: $rate ")
        log.info("-------------------------------------------------------")

        Map parmas = ["market": market, "quantity": quantity, "rate": rate ]
        ExchangeBuySellResponse res = exchange.get("market/selllimit", parmas,  new ParameterizedTypeReference<ExchangeBuySellResponse>(){})

        res = handeleResponseError(res)

        if(res && res.success) {
            ExchangeResponse<String> res2 = new ExchangeResponse<String>()
            res2.success = true
            res2.setResult( res.getResult().getOrderId())
            return res2
        }


        return res
    }

    /**
     * https://bittrex.com/api/v1.1/market/buylimit?apikey=API_KEY&market=BTC-LTC&quantity=1.2&rate=1.3
     */
    @Override
    ExchangeResponse<String> buyLimit(String market, BigDecimal quantity, BigDecimal rate) {

        log.info("---------------------- BUY LIMIT ----------------------")
        log.info("- market: $market   quantity: $quantity   rate: $rate ")
        log.info("-------------------------------------------------------")

        Map parmas = ["market": market, "quantity": quantity, "rate": rate ]
        ExchangeBuySellResponse res = exchange.get("market/buylimit", parmas,  new ParameterizedTypeReference<ExchangeBuySellResponse>(){})

        res = handeleResponseError(res)

        if(res && res.success) {
            ExchangeResponse<String> res2 = new ExchangeResponse<String>()
            res2.success = true
            res2.setResult( res.getResult().getOrderId())
            return res2
        }

        return res
    }

    @Override
    Boolean cancelOrder(String market, String id) {

        Map parmas = ["uuid": id]
        ExchangeCancelOrderResponse res = exchange.get("market/cancel", parmas,  new ParameterizedTypeReference<ExchangeCancelOrderResponse>(){})

        handeleResponseError(res)

        return (res && res.success)
    }

    @Override
    ExchangeResponse<ITicker> getTicker(String market) {
        Map parmas = ["market": market]
        ExchangeTickerResponse res = exchange.get("public/getticker", parmas,  new ParameterizedTypeReference<ExchangeTickerResponse>(){})

        if(res && res.success) {
            ITicker t =  res.getResult()
            t.setMarket(market)
        }

        return handeleResponseError(res)
    }

    ExchangeResponse<IOrder> getOrder(String market, String id) {
        Map parmas = ["uuid": id]
        ExchangeOrderResponse res = exchange.get("account/getorder", parmas,  new ParameterizedTypeReference<ExchangeOrderResponse>(){})

        return handeleResponseError(res)
    }

    List<IOrder> getOrderHistory() {

        ExchangeHistoricOrderResponse res = exchange.get("account/getorderhistory", new ParameterizedTypeReference<ExchangeHistoricOrderResponse>(){})

        handeleResponseError(res)

        if(res && res.success) {
            return res.getResult()
        }

        return []
    }

    List<CryptoMarket> getMakets() {

        ExchangeMarketResponse info = exchange.get("public/getmarkets", new ParameterizedTypeReference<ExchangeMarketResponse>(){})

        handeleResponseError(info)

        if(info && info.success) {
            return info.getResult().collect { new CryptoMarket(name, it.getCurrency(), it.getAsset()) }
        }

        return  []
    }

    /**
     * https://bittrex.com/api/v1.1/account/getbalances?apikey=API_KEY
     */
    @Override
    List<IBalance> getBalances() {

        ExchangeBalanceResponse res = exchange.get("account/getbalances", new ParameterizedTypeReference<ExchangeBalanceResponse>(){})

        handeleResponseError(res)

        if(res && res.success) {
            return res.getResult()
        }

        return []
    }

    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        Map parmas = ["market": "${market.getCurrency()}-${market.getAsset()}".toString()]

        ExchangeTradeResponse res = exchange.get("public/getmarkethistory", parmas,  new ParameterizedTypeReference<ExchangeTradeResponse>(){})

        handeleResponseError(res)

        List tradeList = []

        if(res && res.success) {
            tradeList = res.getResult()
        }

        return new TradeBatch(market, tradeList)
    }

}
