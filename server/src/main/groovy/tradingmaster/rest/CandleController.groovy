package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import tradingmaster.exchange.ExchangeService
import tradingmaster.model.Candle
import tradingmaster.model.CryptoMarket
import tradingmaster.model.ICandleStore
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.IHistoricDataExchangeAdapter
import tradingmaster.model.ITradeStore
import tradingmaster.model.RestResponse
import tradingmaster.service.CandleImportService

import java.time.LocalDateTime

@RestController
@RequestMapping("/api/candles")
@Commons
class CandleController {

    @Autowired
    ITradeStore store

    @Autowired
    ICandleStore candleStore

    @Autowired
    CandleImportService candleImportService

    @Autowired
    ExchangeService exchangeService

    @RequestMapping(value = "/{exchange}/{market}", method = RequestMethod.GET)
    List<Candle> list(@PathVariable String exchange, @PathVariable String market,
                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime  start,
                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime  end) {

        log.info("loading candles for $exchange, $market, $start, $end")

       // TradeBatch tb = store.loadTrades(exchange, market, start, end)

        //convert to candels

        List<Candle> candles = candleStore.find("1min", exchange, market, start, end )

        //List<Candle> candles = tradesToCandle(tb.market, tb.trades)
       // candleStore.saveAll(candles)

        return candles
    }

    @RequestMapping(value = "/importCandles/{exchange}/{market}", method = RequestMethod.GET)
    RestResponse<String> importCandles(@PathVariable String exchange, @PathVariable String market,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime  start,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime  end) {

        log.info("import candles for $exchange, $market, $start, $end")

        IExchangeAdapter exchangeAdapter = exchangeService.getExchangyByName(exchange)

        RestResponse<String> res = new RestResponse<String>()

        if(exchangeAdapter instanceof IHistoricDataExchangeAdapter) {

            candleImportService.importCandles(  Date.from(start), Date.from(end) , new CryptoMarket(exchange, market), exchangeAdapter)

            res.setData("OK")

        } else {
            res.setSuccess(false)
            res.setMessage("Exchange $exchange does not support the import of historic candles!")
        }

        return res
    }

//    List<Candle> tradesToCandle(IMarket market, List<ITrade> allTrades) {
//
//        def start = Instant.now()
//
//        List<Candle> candleList = []
//
//        if(allTrades) {
//
//            Map<Instant,List<ITrade>> tradesByMinute = allTrades.groupBy { ITrade t ->
//                t.date.toInstant().truncatedTo( ChronoUnit.MINUTES )
//            }
//
//            List<Instant> sortedMinutes = tradesByMinute.keySet().sort()
//
//            Instant firstMinute = sortedMinutes.first()
//            Instant lastMinute = sortedMinutes.last()
//
//            Instant current = firstMinute
//
//
//            CacheService candleCache = new CacheService<Candle>()
//
//            while(current.getEpochSecond() <= lastMinute.getEpochSecond()) {
//
//                log.debug("start candle loop")
//
//                List<ITrade> tradesForCandel = tradesByMinute.get(current) ?: []
//
//                Candle candle = new Candle()
//                candle.market = market // not needed in response will blow up the transfer data size
//                candle.start = Date.from(current)
//                candle.end = Date.from(current.plus(59, ChronoUnit.SECONDS))
//
//                if(tradesForCandel) {
//
//                    tradesForCandel = tradesForCandel.sort { it.date }
//                    candle.open = tradesForCandel.first().price
//                    candle.close = tradesForCandel.last().price
//                    candle.tradeCount = tradesForCandel.size()
//
//                    tradesForCandel.each { trade ->
//                        candle.high = Math.max( trade.price, candle.high)
//                        candle.low = Math.min( trade.price, candle.low)
//                        candle.volume += trade.quantity
//                        candle.volumnWeightedPrice += (trade.price * trade.quantity)
//                    }
//
//                    candle.volumnWeightedPrice /= candle.volume
//
//
//                } else {
//                    // empty candle!
//                    // - open, high, close, low, vwp are the same as the close of the previous candle.
//                    // - trades, volume are 0
//
//                    Candle prevCandel = candleCache.get(market)
//
//                    if(prevCandel) {
//
//                        candle.open = prevCandel.open
//                        candle.close = prevCandel.close
//                        candle.high = prevCandel.high
//                        candle.low = prevCandel.close
//                        candle.volume = prevCandel.volume
//                        candle.volumnWeightedPrice = prevCandel.volumnWeightedPrice
//                        candle.tradeCount = 0
//                        candle.volume = 0.0
//
//                    } else {
//                        log.error("No previous candle found for market ${tb.market} for minute ${current}")
//                    }
//                }
//
//                candleCache.set(market, candle)
//
//                current = current.plus(1, ChronoUnit.MINUTES)
//
//                log.debug("new minute candel: $candle")
//                candle.period = '1min'
//                candleList << candle
//
//            } // last candle is excluded !
//
//        }
//
//        def duration = Duration.between(start, Instant.now())
//
//        log.info("building ${candleList.size()} candles from ${allTrades.size()} trades took ${duration.getSeconds()}")
//
//        return candleList
//    }


}
