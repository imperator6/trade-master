package tradingmaster.service

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.exchange.bittrex.Bittrex
import tradingmaster.model.IOrder

@RunWith(value = SpringRunner.class)
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService

    @Autowired
    Bittrex bittrex

    @Test
    void saveNewOrder() {

        List<IOrder> orderList = bittrex.getOrderHistory()

        orderService.saveOrder( bittrex.getExchangeName(), orderList.first() )

    }

    @Test
    void importOrderListFromExchange() {



        orderService.importOrderListFromExchange(bittrex)

    }
}
