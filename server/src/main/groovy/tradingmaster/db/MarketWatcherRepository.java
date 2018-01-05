package tradingmaster.db;

import org.springframework.data.repository.CrudRepository;
import tradingmaster.model.MarketWatcher;

public interface MarketWatcherRepository extends CrudRepository<MarketWatcher, Integer> {

    MarketWatcher findByExchangeAndMarket(String exchange, String market);

    MarketWatcher findByActive(boolean active);


}
