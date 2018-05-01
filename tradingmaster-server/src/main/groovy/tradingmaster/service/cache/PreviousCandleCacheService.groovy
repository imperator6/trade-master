package tradingmaster.service.cache

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import tradingmaster.model.Candle

@Service
@Scope("prototype")
class PreviousCandleCacheService extends CacheService<Candle> {


}
