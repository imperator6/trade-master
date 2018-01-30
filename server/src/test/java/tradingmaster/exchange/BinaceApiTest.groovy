package tradingmaster.exchange

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.exchange.binance.Binance
import tradingmaster.model.Candle
import tradingmaster.model.CandleInterval
import tradingmaster.model.CryptoMarket
import tradingmaster.model.ITicker
import tradingmaster.service.CandleImportService

import java.text.SimpleDateFormat

@RunWith(value = SpringRunner.class)
//@ContextConfiguration(classes=[TestConfig.class, RestTemplateConfig.class], loader = AnnotationConfigContextLoader.class)
@SpringBootTest
class BinaceApiTest extends DefaultExchangeTest {

    @Autowired
    Binance binance

    @Autowired
    CandleImportService candleImportService

    @Override
    IExchangeAdapter getExchangeAdapter() {
        return binance
    }

    @Test
    void testGetTicker() {

        ///def list = bittrexApi11.getOrderHistory("")

        String market = "USDT-BTC"

        ITicker ticker = getExchangeAdapter().getTicker(market).getResult()

        assertNotNull( ticker )

        assertNotNull( ticker.bid )
        assertNotNull( ticker.ask )

        assertEquals( ticker.market, market )

    }

    @Test
    @Override
    void testGetOrderHistory() {

        def orders = getExchangeAdapter().getOrderHistory("USDT-BTC")

        assertNotNull(orders)
    }

    @Test
    void testsellLimit() {

        String result = binance.sellLimit("BTC-LTC", 0.1, 0.000)

        assertNotNull(result)
    }



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

        assertTrue( !binance.getMakets().isEmpty() )
    }













}
