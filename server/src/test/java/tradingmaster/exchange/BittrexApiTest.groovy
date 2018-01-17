package tradingmaster.exchange

import com.tictactec.ta.lib.Core
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.exchange.bittrex.Bittrex
import tradingmaster.exchange.bittrex.BittrexApi11
import tradingmaster.model.CryptoMarket

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

        Core c = new Core();

        c.stochRsi()

    }

    @Test
    void testgetTrades() {
        assertTrue( !bittrex.getTrades( null, null, new CryptoMarket("Bittrex", "BTC", "ETH")).trades.isEmpty() )
    }

    @Test
    void testGetOrderHistory() {

        ///def list = bittrexApi11.getOrderHistory("")

        def orders = bittrex.getOrderHistory()

        assertTrue( !orders.isEmpty() )
    }




}
