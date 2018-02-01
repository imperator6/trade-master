package tradingmaster.exchange.binance.model

import groovy.transform.ToString
import groovy.util.logging.Commons

@ToString
class BinanceProductInfo {

    List<Symbol> symbols
}

@Commons
class Symbol {

    String baseAsset

    Integer baseAssetPrecision

    String quoteAsset

    Integer quotePrecision

    List<Filter> filters


    BigDecimal getLotStepSize() {

        Filter f = this.filters.find { it.filterType == "LOT_SIZE" }

        if(f) {
            return f.stepSize
        }


        return 0.0001
    }

    BigDecimal getMinNotional() {

        Filter f = this.filters.find { it.filterType == "MIN_NOTIONAL" }

        if(f) {
            return f.minNotional
        }


        return 0.0001
    }

}

class Filter {

    String filterType

    // PRICE_FILTER
    BigDecimal minPrice
    BigDecimal maxPrice
    BigDecimal tickSize

    // LOT_SIZE
    BigDecimal minQty
    BigDecimal maxQty
    BigDecimal stepSize

    // MIN_NOTIONAL
    BigDecimal minNotional



}