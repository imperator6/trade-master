package tradingmaster.exchange

import groovy.util.logging.Commons
import tradingmaster.exchange.bittrex.model.BittrexBalance
import tradingmaster.model.IBalance
import tradingmaster.model.IOrder

@Commons
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

    @Override
    List<IOrder> getOrderHistory(String market) {
        return []
    }

    ExchangeResponse<Object> handeleResponseError(ExchangeResponse<Object> res) {
        if(res == null) {
            res = new ExchangeResponse<Object>()
            res.success = false
            res.message = "No response from server! (res=null)"
        }

        if(res && !res.success) {
            log.error( "ExchangeResponse errors: ${res.message}!")
        }

        return res
    }

}
