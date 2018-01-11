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
import tradingmaster.model.Candle
import tradingmaster.model.CandleInterval
import tradingmaster.model.CryptoMarket
import tradingmaster.service.CandleImportService

import java.text.SimpleDateFormat

@RunWith(value = SpringRunner.class)
//@ContextConfiguration(classes=[TestConfig.class, RestTemplateConfig.class], loader = AnnotationConfigContextLoader.class)
@SpringBootTest
class BinaceApiTest extends TestCase {

    @Autowired
    Binance binance

    @Autowired
    CandleImportService candleImportService

    @Test
    void testGetMarkets() {

        assertTrue( !binance.getMakets().isEmpty() )
    }

    @Test
    void testGetCandles() {

        List<Candle> candles = binance.getCandles( null, null, new CryptoMarket("Binance", "BTC", "ETH"), CandleInterval.ONE_MINUTE)

        assertTrue( !binance.getMakets().isEmpty() )
    }


    @Test
    void testCandleImport() {

        Date startDate = new SimpleDateFormat("dd.MM.yyyy").parse("01.11.2017")
        Date endDate = new SimpleDateFormat("dd.MM.yyyy").parse("02.11.2017")

        List<Candle> candles = candleImportService.importCandles( startDate, endDate, new CryptoMarket("Binance", "BTC", "ETH"), binance)

        while(true) {

            Thread.sleep(1000)
        }
        assertTrue( !binance.getMakets().isEmpty() )
    }







}
