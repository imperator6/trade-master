package tradingmaster.service.cache

import org.springframework.stereotype.Service
import tradingmaster.model.ITrade


import java.time.Instant

@Service
class PreviousTradeCacheService extends CacheService<Map<Instant,List<ITrade>>> {


}
