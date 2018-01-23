package tradingmaster.exchange

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.exchange.bittrex.Bittrex
import tradingmaster.exchange.bittrex.BittrexApi11
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IBalance
import tradingmaster.model.ITicker

@RunWith(value = SpringRunner.class)
@SpringBootTest
class BittrexApiTest extends TestCase {

    @Autowired
    Bittrex bittrex

    @Autowired
    BittrexApi11 bittrexApi11


    @Test
    void testgetMarkets() {
        assertTrue( !bittrex.getMakets().isEmpty() )
    }

    @Test
    void testgetTrades() {
        assertTrue( !bittrex.getTrades( null, null, new CryptoMarket("Bittrex", "BTC", "ETH")).trades.isEmpty() )
    }

    @Test
    void testGetOrderHistory() {

        def orders = bittrex.getOrderHistory()

        assertTrue( !orders.isEmpty() )
    }

    @Test
    void testGetOrder() {

        ///def list = bittrexApi11.getOrderHistory("")

        List orders = bittrex.getOrderHistory()

        String orderId = orders.first().getId()

        def order = bittrex.getOrder(orderId)

        assertNotNull( order )
    }

    @Test
    void testGetTicker() {

        ///def list = bittrexApi11.getOrderHistory("")

        ITicker ticker = bittrex.getTicker("USDT-BTC")


        assertNotNull( ticker )

        assertNotNull( ticker.bid )
        assertNotNull( ticker.ask )
        assertNotNull( ticker.last )
        assertNotNull( ticker.market )

    }

    @Test
    void testCancelOrder() {
        String orderid = "5caa7705-915e-4db4-b395-eedd11cf1882"

        Boolean isCanceld = bittrex.cancelOrder(orderid)

        assertFalse( isCanceld )
    }

    @Test
    void testBalances() {

        List<IBalance> balances = bittrex.getBalances()

        assertTrue( !balances.isEmpty() )

        IBalance first = balances.last()

        assertNotNull( first.getCurrency() )
    }

    @Test
    void testSell() {

        String orderId = bittrex.sellLimit("ETH-FUN", 1, 0.0001)



        assertNotNull( orderId )
    }

    @Test
    void testBuy() {

        String orderId = bittrex.buyLimit("ETH-FUN", 1, 0.0001)



        assertNotNull( orderId )
    }




}
