package tradingmaster.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.Position;

import java.util.List;

@Component
public interface PositionRepository extends PagingAndSortingRepository<Position, Integer> {

     List<Position> findByBotId(Integer botId);

     List<Position> findByMarketAndClosed(String market, boolean closed);


}
