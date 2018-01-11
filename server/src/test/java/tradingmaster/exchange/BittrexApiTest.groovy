package tradingmaster.exchange

import junit.framework.Test
import junit.framework.TestCase
import org.junit.runner.RunWith
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.support.AnnotationConfigContextLoader
import tradingmaster.config.RestTemplateConfig
import tradingmaster.exchange.bittrex.Bittrex
import tradingmaster.model.CryptoMarket

@RunWith(value = SpringRunner.class)
@SpringBootTest
class BittrexApiTest extends TestCase {

    @Autowired
    Bittrex bittrex


    @Test
    void testgetMarkets() {
        assertTrue( !bittrex.getMakets().isEmpty() )

    }

    @Test
    void testgetTrades() {
        assertTrue( !bittrex.getTrades( null, null, new CryptoMarket("Bittrex", "BTC", "ETH")).trades.isEmpty() )
    }




}
