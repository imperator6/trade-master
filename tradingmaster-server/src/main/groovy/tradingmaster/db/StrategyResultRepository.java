package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tradingmaster.db.entity.Signal;
import tradingmaster.db.entity.StrategyResult;

import java.util.Date;
import java.util.List;

@Component
@Transactional
public interface StrategyResultRepository extends PagingAndSortingRepository<StrategyResult, Integer> {

     long deleteByBotId(Integer botId);

}
