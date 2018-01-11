package tradingmaster.exchange.binance

import groovy.transform.Memoized
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.binance.model.BinanceCandle
import tradingmaster.exchange.binance.model.BinanceProductInfo
import tradingmaster.exchange.binance.model.BinanceTrade
import tradingmaster.model.Candle
import tradingmaster.model.CandleInterval
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IHistoricDataExchangeAdapter
import tradingmaster.model.TradeBatch
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

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromPath("api/v1/klines")

        urlBuilder.queryParam("symbol", "${market.getAsset()}${market.getCurrency()}")

        urlBuilder.queryParam("interval", interval.getKey())

        if(startDate) {
            urlBuilder.queryParam("startTime", startDate.getTime())
        }

        if(endDate) {
            urlBuilder.queryParam("endTime", startDate.getTime())
        }

        ArrayList[] res = exchange.getAsList(urlBuilder.toUriString(), new ParameterizedTypeReference<ArrayList[]>(){})

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
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromPath("api/v1/aggTrades")

        urlBuilder.queryParam("symbol", "${market.getAsset()}${market.getCurrency()}")


        List<BinanceTrade> trades = exchange.getAsList(urlBuilder.toUriString(), new ParameterizedTypeReference<BinanceTrade[]>(){})


        return new TradeBatch(market, trades)
    }
}
