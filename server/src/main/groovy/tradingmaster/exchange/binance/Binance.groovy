package tradingmaster.exchange.binance

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.exchange.binance.model.BinanceAccount
import tradingmaster.exchange.binance.model.BinanceOrder
import tradingmaster.exchange.binance.model.BinanceProductInfo
import tradingmaster.exchange.binance.model.BinanceTicker
import tradingmaster.exchange.binance.model.BinanceTrade
import tradingmaster.model.*
/**
 *  https://github.com/binance-exchange/binance-official-api-docs/blob/master/rest-api.md
 */
@Service("Binance")
@Commons
class Binance extends DefaultExchageAdapter implements IHistoricDataExchangeAdapter {


    @Autowired
    BinanceExchangeImpl exchange

    Binance() {
        super("Binance")
    }



    @Override
    List<Candle> getCandles(Date startDate, Date endDate, CryptoMarket market, CandleInterval interval) {


        Map params = [:]
        params.put("symbol", convertMarketToSymbol(market.getName()))
        params.put("interval", interval.getKey())

        if(startDate) {
            params.put("startTime", startDate.getTime())
        }

        if(endDate) {
            params.put("endTime", startDate.getTime())
        }

        ArrayList[] res = exchange.getAsList("api/v1/klines", params ,  new ParameterizedTypeReference<ArrayList[]>(){})

        List<Candle> candles = res.collect {

            Candle c = new Candle()

            c.start = new Date(it[0])

            c.open = it[1] as Double
            c.high = it[2] as Double
            c.low = it[3] as Double
            c.close = it[4] as Double

            c.volume = it[5] as Double
            c.end = new Date(it[6])

            c.tradeCount = it[8] as Integer

            return c
        }

        return candles
    }

    List<CryptoMarket> getMakets() {

        BinanceProductInfo info = exchange.get("api/v1/exchangeInfo", new ParameterizedTypeReference<BinanceProductInfo>(){})

        return info.symbols.collect { new CryptoMarket(name, it.getQuoteAsset(), it.getBaseAsset()) }
    }

    List<IOrder> getOrderHistory() {

        List<BinanceOrder> orderList = []
        getMakets().each {
            orderList.addAll( getOrderHistory( it.getName()))
            Thread.sleep(200)
        }
        return orderList
    }

    List<IOrder> getOrderHistory(String market) {

        Map params = [:]
        params.put("symbol", convertMarketToSymbol(market))

        List<BinanceOrder> orderList = exchange.get("api/v3/allOrders", params, new ParameterizedTypeReference<List<BinanceOrder>>(){})

        return orderList
    }

    @Override
    List<IBalance> getBalances() {

        BinanceAccount res = exchange.get("/api/v3/account", new ParameterizedTypeReference<BinanceAccount>(){})

        if(res) {
            return res.getBalances()
        }

        return []

    }

    @Override
    Boolean cancelOrder(String id) {
        return null
    }

    @Override
    ExchangeResponse<String> sellLimit(String market, BigDecimal quantity, BigDecimal rate) {
        return newOrder(market, "SELL", "LIMIT", quantity, rate)
    }

    @Override
    ExchangeResponse<String> buyLimit(String market, BigDecimal quantity, BigDecimal rate) {
        return null
    }

    ExchangeResponse<String> newOrder(String market, String buySell, String type, BigDecimal quantity, BigDecimal rate) {

        Map params = [:]
        params.put("symbol", convertMarketToSymbol(market))
        params.put("side", buySell)
        params.put("type", type)
        params.put("timeInForce", "GTC")
        params.put("quantity", quantity)
        params.put("price", rate)


        ExchangeResponse<String> res = new ExchangeResponse()

        try {
            String ticker = exchange.post("api/v3/order", params, new ParameterizedTypeReference<String>(){}, "")

            if(res) {
                res.setSuccess(true)
                res.setResult(ticker)
            }

        } catch (all) {
            res.setSuccess(false)
            res.setMessage(all.getMessage())
        }

        return handeleResponseError(res)

        return null
    }

    @Override
    ExchangeResponse<ITicker> getTicker(String market) {
        Map params = [:]
        params.put("symbol", convertMarketToSymbol(market))

        ExchangeResponse<ITicker> res = new ExchangeResponse()

        try {
            BinanceTicker ticker = exchange.get("api/v3/ticker/bookTicker", params, new ParameterizedTypeReference<BinanceTicker>(){})

            if(res) {
                res.setSuccess(true)
                res.setResult(ticker)
            }

        } catch (all) {
            res.setSuccess(false)
            res.setMessage(all.getMessage())
        }

        return handeleResponseError(res)
    }

    @Override
    ExchangeResponse<IOrder> getOrder(String id) {
        return null
    }

    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        Map params = [:]
        params.put("symbol", convertMarketToSymbol(market.getName()))

        List<BinanceTrade> trades = exchange.getAsList("api/v1/aggTrades", params, new ParameterizedTypeReference<BinanceTrade[]>(){})

        return new TradeBatch(market, trades)
    }

    String convertMarketToSymbol(String market) {
        String[] symbols = market.split("-")
        return symbols[1]+ symbols[0]
    }
}
