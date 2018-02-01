package tradingmaster.exchange.binance

import groovy.util.logging.Commons

@Commons
class BinanceHelper {

    static String convertMarketToSymbol(String market) {

        if(market == null) {
            return null
        }

        String[] data = market.split("-")

        return data[1] + data[0]
    }


    static String extractAssetFromSymbol(String symbol) {

        if(symbol == null) {
            return null
        }

        symbol = symbol.toUpperCase()

        // find currency
        if(symbol.endsWith("USDT")) {

            String asset = symbol.replace("USDT", "")

            return asset
        } else if(symbol.endsWith("BTC")) {

            String asset = symbol.replace("BTC", "")

            return asset
        } else if(symbol.endsWith("ETH")) {

            String asset = symbol.replace("ETH", "")

            return asset
        } else if(symbol.endsWith("BNB")) {

            String asset = symbol.replace("BNB", "")

            return asset
        } else {
            log.error("Can't extract asset from symbol $symbol !")

            return symbol
        }

    }


    static String convertSymbolToMarket(String symbol) {

        if(symbol == null) {
            return null
        }

        symbol = symbol.toUpperCase()

        // find currency
        if(symbol.endsWith("USDT")) {

            String asset = symbol.replace("USDT", "")

            return "USDT-$asset"
        } else if(symbol.endsWith("BTC")) {

            String asset = symbol.replace("BTC", "")

            return "BTC-$asset"
        } else if(symbol.endsWith("ETH")) {

            String asset = symbol.replace("ETH", "")

            return "ETH-$asset"
        } else if(symbol.endsWith("BNB")) {

            String asset = symbol.replace("BNB", "")

            return "BNB-$asset"
        } else {
            log.error("Can't convert symbol $symbol to market!")

            return symbol
        }

    }

}
