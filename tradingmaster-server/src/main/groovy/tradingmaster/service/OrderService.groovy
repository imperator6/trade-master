package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.OrderRepository
import tradingmaster.db.entity.Order
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.model.IOrder

@Service
@Commons
class OrderService {

    @Autowired
    OrderRepository  orderRepository


    void importOrderListFromExchange(IExchangeAdapter exchangeAdapter) {

       List<IOrder> orders = exchangeAdapter.getOrderHistory()

        List<Order> exsistingOrders = orderRepository.findByExchange(exchangeAdapter.getExchangeName())
        List<String> existingExtIds = exsistingOrders.collect { it.extOrderId }

        orders.each { IOrder exchangeOrder ->

            if(exchangeOrder.getId() in existingExtIds) {
                log.warn("Order with id ${exchangeOrder.getId()} already exsists")
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

        return newOrder
    }


}
