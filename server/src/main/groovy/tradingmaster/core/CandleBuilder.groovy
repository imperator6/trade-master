package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.model.Candle
import tradingmaster.model.ITrade
import tradingmaster.model.TradeBatch
import tradingmaster.service.cache.PreviousCandleCacheService
import tradingmaster.service.cache.PreviousTradeCacheService

import java.time.Instant
import java.time.temporal.ChronoUnit

@Commons
class CandleBuilder implements MessageHandler {

    @Autowired
    PreviousTradeCacheService tradeCache

    @Autowired
    PreviousCandleCacheService candleCache

    @Autowired
    PublishSubscribeChannel candelChannel1Minute

    @Autowired
    PublishSubscribeChannel lastRecentCandelChannel


    @Autowired
    PublishSubscribeChannel mixedCandelSizesChannel

    Instant serverStartMinute = Instant.now().truncatedTo( ChronoUnit.MINUTES ) // UTC Time now

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        TradeBatch tb = message.getPayload()

        def allTrades = tb.trades != null ? tb.trades : []

        log.debug("Building next minute candles for ${allTrades.size()} trade $tb.market")

        List<ITrade> prev = tradeCache.get(tb.market)

        if(prev) {
            log.debug("Previous trades: ${prev.size()} for minute ${prev.first().date.toInstant().truncatedTo(ChronoUnit.MINUTES)} $tb.market" )
            allTrades = allTrades + prev
        }

        Candle lastRecentCandel = null

        if(allTrades) {

            Map<Instant,List<ITrade>> tradesByMinute = allTrades.groupBy { ITrade t ->
                t.date.toInstant().truncatedTo( ChronoUnit.MINUTES )
            }

            List<Instant> sortedMinutes = tradesByMinute.keySet().sort()

            Instant firstMinute = sortedMinutes.first()
            Instant lastMinute = sortedMinutes.last()

            def tradesForLastMinute = tradesByMinute.remove(lastMinute)

            tradeCache.set(tb.market, tradesForLastMinute)

            Instant current = firstMinute
            while(current.getEpochSecond() < lastMinute.getEpochSecond()) {

                log.debug("start candle loop")

                List<ITrade> tradesForCandel = tradesByMinute.get(current) ?: []

                Candle candle = new Candle()
                candle.market = tb.market
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

                    Candle prevCandel = candleCache.get(tb.market)

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

                candleCache.set(tb.market, candle)

                current = current.plus(1, ChronoUnit.MINUTES)

                log.debug("new minute candel: $candle")
                candle.setPeriod("1min")

                def candelMinute = candle.end.toInstant()
                if(candelMinute > serverStartMinute) {
                    lastRecentCandel = candle
                    candelChannel1Minute.send( MessageBuilder.withPayload(candle).build() )
                } else {
                    //log.warn("Candele is to old: ($candelMinute is befor serverStartMinute $serverStartMinute) $candle")
                }

            } // last candle is excluded !

            if(lastRecentCandel != null) {
                lastRecentCandelChannel.send( MessageBuilder.withPayload(lastRecentCandel).build() )
                mixedCandelSizesChannel.send( MessageBuilder.withPayload(lastRecentCandel).build() )
            }

        } else {

            // no trades in list

            // TODO: clean the cache after a given timeout ?

            log.debug("no trades!")


        }

        //log.info("Candel Builder recevived ${trades.trades.size()} new trades!")
    }

}
