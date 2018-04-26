package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tradingmaster.db.entity.Signal;

import java.util.Date;
import java.util.List;

@Component
@Transactional
public interface SignalRepository extends PagingAndSortingRepository<Signal, Integer> {

     List<Signal> findBySignalDate(Date date);

     long deleteByBotId(Integer botId);


}
