package tradingmaster.exchange.paper

import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.model.Balance
import tradingmaster.model.CryptoMarket
import tradingmaster.model.TradeBatch

class PaperExchange extends DefaultExchageAdapter {

    Map config

    PaperExchange() {
        super("PaperExchange")
    }

    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {
        return null
    }

    List<Balance> getBalances() {
        def balances = []

        Balance b = new Balance()
        b.currency = config.baseCurrency
        b.value = config.startBalance

        balances << b

        return balances
    }




}
