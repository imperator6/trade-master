package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.Signal;
import tradingmaster.db.entity.TradeBot;

import java.util.Date;
import java.util.List;

@Component
public interface SignalRepository extends PagingAndSortingRepository<Signal, Integer> {

     List<Signal> findBySignalDate(Date date);


}
