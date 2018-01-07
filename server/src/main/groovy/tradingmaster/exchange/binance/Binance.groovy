package tradingmaster.exchange.binance

import groovy.transform.Memoized
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.binance.model.BinanceProductInfo
import tradingmaster.exchange.binance.model.BinanceTrade
import tradingmaster.model.CryptoMarket
import tradingmaster.model.TradeBatch
/**
 *  https://github.com/binance-exchange/binance-official-api-docs/blob/master/rest-api.md
 */
@Service("Binance")
@Commons
class Binance extends DefaultExchageAdapter {


    @Autowired
    BinanceExchangeImpl exchange

    Binance() {
        super("Binance")
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
