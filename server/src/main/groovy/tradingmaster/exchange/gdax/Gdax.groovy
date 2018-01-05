package tradingmaster.exchange.gdax

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.gdax.model.GdaxProduct
import tradingmaster.exchange.gdax.model.GdaxTrade
import tradingmaster.model.CryptoMarket
import tradingmaster.model.TradeBatch

@Service("Gdax")
@Commons
class Gdax extends DefaultExchageAdapter {

    @Autowired
    GdaxExchangeImpl exchange

    Gdax() {
        super("Gdax")
    }

    List<CryptoMarket> getMakets() {

        def tradesEndpoint = "products"

        List<GdaxProduct> trades = exchange.getAsList(tradesEndpoint, new ParameterizedTypeReference<GdaxProduct[]>(){})

        def markets = trades.collect { new CryptoMarket(name, it.asset, it.currency)}

        return markets
    }

    @Override
    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {

        def productId = "${market.getAsset()}-${market.getCurrency()}"

        def tradesEndpoint = "products/" + productId + "/trades"

        List<GdaxTrade> trades = exchange.getAsList(tradesEndpoint, new ParameterizedTypeReference<GdaxTrade[]>(){})


        return new TradeBatch(market, trades)
    }
}
