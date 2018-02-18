package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tradingmaster.db.OrderRepository
import tradingmaster.db.entity.Order
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.exchange.ExchangeService
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IOrder

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
@Commons
class OrderService {

    @Autowired
    OrderRepository  orderRepository

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    ExchangeService exchangeService

    @Scheduled(initialDelay=60000l, fixedRate=3600000l) // every hour
    void importOrders() {

        log.info("Starting scheduled order import...")

        List exchangeList = exchangeService.getAvailableExchanges()

        exchangeList.each {

            IExchangeAdapter exchangeAdapter = exchangeService.getExchangyByName(it)

            importOrderListFromExchange(exchangeAdapter)


        }
    }

    Order findLastBuyOrderForAsset(CryptoMarket market) {

         List<Order> orderList = orderRepository.findByExchangeAndBuySellOrderByDateDesc(market.getExchange(), "buy")

         return orderList.find { it.market.indexOf(market.asset) > -1 }

    }


    void importOrderListFromExchange(IExchangeAdapter exchangeAdapter) {

        log.info("Importing orders for exchange ${exchangeAdapter.getExchangeName()}")

       List<IOrder> orders = exchangeAdapter.getOrderHistory()

        List<Order> exsistingOrders = orderRepository.findByExchange(exchangeAdapter.getExchangeName())
        List<String> existingExtIds = exsistingOrders.collect { it.extOrderId }

        orders.each { IOrder exchangeOrder ->

            if(exchangeOrder.getId() in existingExtIds) {
                log.debug("Order with id ${exchangeOrder.getId()} already exsists")
            } else {
                // new historic order
                Order newOrder = transform(exchangeOrder, exchangeAdapter.getExchangeName() )


                orderRepository.save( newOrder )
            }
        }
    }

    void saveOrder(String exchangeName, IOrder exchangeOrder) {

        List<Order> exsistingOrders = orderRepository.findByExtOrderIdAndExchange( exchangeOrder.getId() ,exchangeName )

        if(exsistingOrders.isEmpty()) {
            Order newOrder = transform(exchangeOrder, exchangeName )
            orderRepository.save( newOrder )
        } else {
            log.warn("Order with id ${exchangeOrder.getId()} already exsists")
        }
    }

    Order transform(IOrder exchangeOrder, String exchangeName) {

        Order newOrder = new Order()

        newOrder.setExtOrderId( exchangeOrder.getId() )
        newOrder.setMarket( exchangeOrder.getMarket() )
        newOrder.setExchange( exchangeName )
        newOrder.setDate( exchangeOrder.getTimeStamp() )
        newOrder.setQuantity( exchangeOrder.getQuantity() )
        newOrder.setPrice( exchangeOrder.getPrice() )
        newOrder.setPricePerUnit( exchangeOrder.getPricePerUnit() )
        newOrder.setCommission( exchangeOrder.getCommissionPaid() )
        newOrder.setBuySell( exchangeOrder.getBuySell())


        return newOrder
    }


}
