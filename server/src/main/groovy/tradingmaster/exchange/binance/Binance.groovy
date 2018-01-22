package tradingmaster.exchange.binance

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.binance.model.BinanceProductInfo
import tradingmaster.exchange.binance.model.BinanceTrade
import tradingmaster.exchange.ExchangeResponse
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
        params.put("symbol", "${market.getAsset()}${market.getCurrency()}".toString())
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

    @Override
    List<IBalance> getBalances() {
        return null
    }

    @Override
    Boolean cancelOrder(String id) {
        return null
    }

    @Override
    ExchangeResponse<String> sellLimit(String market, BigDecimal quantity, BigDecimal rate) {
        return null
    }

    @Override
    ExchangeResponse<String> buyLimit(String market, BigDecimal quantity, BigDecimal rate) {
        return null
    }

    @Override
    ExchangeResponse<ITicker> getTicker(String market) {
        return null
    }

    @Override
    ExchangeResponse<IOrder> getOrder(String id) {
        return null
    }

    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        Map params = [:]
        params.put("symbol", "${market.getAsset()}${market.getCurrency()}".toString())

        List<BinanceTrade> trades = exchange.getAsList("api/v1/aggTrades", params, new ParameterizedTypeReference<BinanceTrade[]>(){})

        return new TradeBatch(market, trades)
    }
}
