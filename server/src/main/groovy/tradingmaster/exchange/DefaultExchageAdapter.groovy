package tradingmaster.exchange

import tradingmaster.model.Balance
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IExchangeAdapter

abstract class DefaultExchageAdapter implements IExchangeAdapter {

    String name

    protected DefaultExchageAdapter(String name) {
        this.name = name
    }

    List<CryptoMarket> getMakets() {
        return Collections.emptyList()
    }

    List<Balance> getBalances() {
        Collections.emptyList()
    }

    Balance getBalance(String currency) {
        Balance b = getBalances().find { it.currency.equalsIgnoreCase(currency)}

        if(!b) {
            b = new Balance()
            b.currency = currency
        }

        return b
    }


}
