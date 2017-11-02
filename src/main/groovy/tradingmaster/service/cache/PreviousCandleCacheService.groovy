package tradingmaster.service.cache

import org.springframework.stereotype.Service
import tradingmaster.model.Candle
import tradingmaster.service.cache.CacheService


@Service
class PreviousCandleCacheService extends CacheService<Candle> {


}
