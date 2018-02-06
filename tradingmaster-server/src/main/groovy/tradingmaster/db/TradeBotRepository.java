package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.TradeBot;

import java.util.List;

@Component
public interface TradeBotRepository extends PagingAndSortingRepository<TradeBot, Integer> {

     List<TradeBot> findByActive(boolean active);


}
