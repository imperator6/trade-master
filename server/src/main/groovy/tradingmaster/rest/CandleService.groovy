package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import tradingmaster.model.*
import tradingmaster.service.cache.CacheService

import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/candles")
@Commons
class CandleService {

    @Autowired
    ITradeStore store

    @RequestMapping(value = "/{exchange}/{market}", method = RequestMethod.GET)
    List<Candle> list(@PathVariable String exchange, @PathVariable String market,
                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime  start,
                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") LocalDateTime  end) {

        log.info("loading candles for $exchange, $market, $start, $end")

        TradeBatch tb = store.loadTrades(exchange, market, start, end)

        //convert to candels

        List<Candle> candles = tradesToCandle(tb.market, tb.trades)



        return candles
    }

    List<Candle> tradesToCandle(IMarket market, List<ITrade> allTrades) {

        List<Candle> candleList = []

        if(allTrades) {

            Map<Instant,List<ITrade>> tradesByMinute = allTrades.groupBy { ITrade t ->
                t.date.toInstant().truncatedTo( ChronoUnit.MINUTES )
            }

            List<Instant> sortedMinutes = tradesByMinute.keySet().sort()

            Instant firstMinute = sortedMinutes.first()
            Instant lastMinute = sortedMinutes.last()

            Instant current = firstMinute


            CacheService candleCache = new CacheService<Candle>()

            while(current.getEpochSecond() <= lastMinute.getEpochSecond()) {

                log.debug("start candle loop")

                List<ITrade> tradesForCandel = tradesByMinute.get(current) ?: []

                Candle candle = new Candle()
                //candle.market = market // not needed in response will blow up the transfer data size
                candle.start = Date.from(current)
                candle.end = Date.from(current.plus(59, ChronoUnit.SECONDS))

                if(tradesForCandel) {

                    tradesForCandel = tradesForCandel.sort { it.date }
                    candle.open = tradesForCandel.first().price
                    candle.close = tradesForCandel.last().price
                    candle.tradeCount = tradesForCandel.size()

                    tradesForCandel.each { trade ->
                        candle.high = Math.max( trade.price, candle.high)
                        candle.low = Math.min( trade.price, candle.low)
                        candle.volume += trade.quantity
                        candle.volumnWeightedPrice += (trade.price * trade.quantity)
                    }

                    candle.volumnWeightedPrice /= candle.volume


                } else {
                    // empty candle!
                    // - open, high, close, low, vwp are the same as the close of the previous candle.
                    // - trades, volume are 0

                    Candle prevCandel = candleCache.get(market)

                    if(prevCandel) {

                        candle.open = prevCandel.open
                        candle.close = prevCandel.close
                        candle.high = prevCandel.high
                        candle.low = prevCandel.close
                        candle.volume = prevCandel.volume
                        candle.volumnWeightedPrice = prevCandel.volumnWeightedPrice
                        candle.tradeCount = 0
                        candle.volume = 0.0

                    } else {
                        log.error("No previous candle found for market ${tb.market} for minute ${current}")
                    }
                }

                candleCache.set(market, candle)

                current = current.plus(1, ChronoUnit.MINUTES)

                log.info("new minute candel: $candle")

                candleList << candle

            } // last candle is excluded !

        }

        return candleList
    }


}
