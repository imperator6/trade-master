package tradingmaster.exchange

import tradingmaster.exchange.bittrex.model.BittrexBalance
import tradingmaster.model.IBalance

abstract class DefaultExchageAdapter implements IExchangeAdapter {

    String name

    protected DefaultExchageAdapter(String name) {
        this.name = name
    }

    @Override
    String getExchangeName() {
        return name
    }

    IBalance getBalance(String currency) {
        IBalance b = getBalances().find { it.currency.equalsIgnoreCase(currency)}

        if(!b) {
            b = new BittrexBalance()
            b.currency = currency
        }

        return b
    }

    String buildMarket(String currency, String asset) {
        return "${currency}-${asset}".toUpperCase().toString()
    }

}
