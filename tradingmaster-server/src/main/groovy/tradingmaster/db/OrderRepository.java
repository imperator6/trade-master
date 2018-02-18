package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.Order;

import java.util.List;

@Component
public interface OrderRepository extends PagingAndSortingRepository<Order, Integer> {

     List<Order> findByExtOrderIdAndExchange(String extOrderId, String exchange);

     List<Order> findByExchange(String exchange);

     List<Order> findByExchangeAndMarketAndBuySellOrderByDateDesc(String exchange, String market, String buySell);

     List<Order> findByExchangeAndBuySellOrderByDateDesc(String exchange, String buySell);



}
