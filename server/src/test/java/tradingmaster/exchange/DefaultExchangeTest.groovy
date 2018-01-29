package tradingmaster.exchange

import junit.framework.TestCase
import org.junit.Test
import tradingmaster.model.IBalance

abstract class DefaultExchangeTest extends TestCase {


    @Test
    void testGetOrderHistory() {

        def orders = getExchangeAdapter().getOrderHistory()

        assertTrue( !orders.isEmpty() )
    }

    @Test
    void testBalances() {

        List<IBalance> balances = getExchangeAdapter().getBalances()

        assertTrue( !balances.isEmpty() )

        IBalance first = balances.last()

        assertNotNull( first.getCurrency() )
    }

    abstract IExchangeAdapter getExchangeAdapter()

}
