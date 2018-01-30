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
import tradingmaster.model.IOrder
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

        def orders = getExchangeAdapter().getOrderHistory("ETH-BCC")

        assertFalse(orders.isEmpty())
    }

    @Test
    void testGetOrder() {

        List orders = getExchangeAdapter().getOrderHistory("ETH-BCC")

        IOrder order1 = orders.first()

        def order2 = getExchangeAdapter().getOrder(order1.getMarket(), order1.getId()).getResult()

        assertNotNull( order2 )
    }

    @Test
    void testSellAndCancelOrder() {

        //
        /* String orderId = getExchangeAdapter().sellLimit("ETH-BCC", 0.01, 2).getResult()

        IOrder order = getExchangeAdapter().getOrder("ETH-BCC", orderId).getResult()

        Thread.sleep(2000)

        boolean cancel = getExchangeAdapter().cancelOrder(order.getMarket(), order.getId())

        assertTrue(cancel) */
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
