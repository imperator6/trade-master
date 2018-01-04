package tradingmaster.exchange

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


}
