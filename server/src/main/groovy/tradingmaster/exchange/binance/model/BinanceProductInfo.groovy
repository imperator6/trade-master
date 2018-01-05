package tradingmaster.exchange.binance.model

import groovy.transform.ToString

@ToString
class BinanceProductInfo {

    List<Symbol> symbols
}


class Symbol {

    String baseAsset

    String quoteAsset

}