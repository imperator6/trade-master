package tradingmaster.service.cache

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import tradingmaster.core.CandleAggregator
import tradingmaster.model.Candle

@Service
@Scope("prototype")
class PreviousCandleListCacheService extends CacheService<List<Candle>> {


}
