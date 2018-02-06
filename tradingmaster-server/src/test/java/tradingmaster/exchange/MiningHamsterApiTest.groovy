package tradingmaster.exchange

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.exchange.bittrex.Bittrex
import tradingmaster.exchange.bittrex.BittrexApi11
import tradingmaster.exchange.mininghamster.HamsterSignalRepository
import tradingmaster.exchange.mininghamster.MiningHamster
import tradingmaster.model.CryptoMarket

@RunWith(value = SpringRunner.class)
@SpringBootTest
class MiningHamsterApiTest extends TestCase {

    @Autowired
    MiningHamster miningHamster

    @Autowired
    HamsterSignalRepository repo


    @Test
    void testgetMarkets() {
        List signals = miningHamster.getLatestSignals()


        def s2 = repo.findAll()

        assertTrue( !signals.isEmpty() )




    }





}
