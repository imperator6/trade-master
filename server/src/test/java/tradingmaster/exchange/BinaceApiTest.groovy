package tradingmaster.exchange

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.support.AnnotationConfigContextLoader
import tradingmaster.config.RestTemplateConfig
import tradingmaster.exchange.binance.Binance

@RunWith(value = SpringRunner.class)
//@ContextConfiguration(classes=[TestConfig.class, RestTemplateConfig.class], loader = AnnotationConfigContextLoader.class)
@SpringBootTest
class BinaceApiTest extends TestCase {

    @Autowired
    Binance binance

    @Test
    void testGetMarkets() {

        assertTrue( !binance.getMakets().isEmpty() )
    }


}
