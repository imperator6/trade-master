package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import tradingmaster.db.entity.MarketWatcher;

import java.util.List;

public interface MarketWatcherRepository extends PagingAndSortingRepository<MarketWatcher, Integer> {

    MarketWatcher findByExchangeAndMarket(String exchange, String market);

    List<MarketWatcher> findByActive(boolean active);


}
