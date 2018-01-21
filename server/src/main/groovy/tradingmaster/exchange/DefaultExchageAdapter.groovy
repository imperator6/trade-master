package tradingmaster.exchange

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Async
import tradingmaster.model.Balance
import tradingmaster.model.BuySell
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.Order

import java.util.concurrent.Future

abstract class DefaultExchageAdapter implements IExchangeAdapter {

    @Autowired
    TaskExecutor orderTaskExecutor

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

    //@Async("orderTaskExecutor")
    Order placeOrder(BuySell bs, BigDecimal spend, String currency, String asset, boolean marketOrder) {

        try {

            // 1. get price

            // 2. calc amount based on spend



            // 44. send to complete order channel

        } catch (Exception e) {

        }


        return null
    }

    @Override
    String getExchangeName() {
        return name
    }


}
