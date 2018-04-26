package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tradingmaster.db.entity.Position;

import java.util.List;

@Component
@Transactional
public interface PositionRepository extends PagingAndSortingRepository<Position, Integer> {

     List<Position> findByBotId(Integer botId);

     List<Position> findByMarketAndClosed(String market, boolean closed);

     long deleteByBotId(Integer botId);


}
