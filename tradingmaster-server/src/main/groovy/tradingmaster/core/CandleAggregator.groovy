package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.model.Candle
import tradingmaster.service.cache.PreviousCandleCacheService
import tradingmaster.service.cache.PreviousCandleListCacheService
import tradingmaster.util.DateHelper

import java.time.temporal.ChronoUnit


@Commons
class CandleAggregator implements MessageHandler {

    private int candleCount = 0

    PublishSubscribeChannel destinationChannel

    @Autowired
    PreviousCandleListCacheService candleCacheService

    CandleAggregator(int candleCount, PublishSubscribeChannel destinationChannel) {
        this.candleCount = candleCount
        this.destinationChannel = destinationChannel
    }

    //List<Candle> candleList = Collections.synchronizedList(new ArrayList<Candle>())


    @Override
    synchronized void handleMessage(Message<?> message) throws MessagingException {

        Candle minuteCandle = message.getPayload()

        // only accept minute candle
        if(!minuteCandle.isMinuteCandle()) return

        List<Candle> candleList = candleCacheService.get(minuteCandle.getMarket())

        if(!candleList) {
            candleList =  Collections.synchronizedList(new ArrayList<Candle>())
            candleCacheService.set(minuteCandle.getMarket(),candleList)
        }

        Candle firstCandle = candleList.isEmpty() ? minuteCandle : candleList.first()

        if(isValidCandle(firstCandle, minuteCandle, candleCount )) {

            candleList.add(minuteCandle)

            if(isAggregationComplete(firstCandle, minuteCandle, candleCount)) {

                Candle next = buildCandle( candleList, candleCount)
                candleList.clear()


                // log.info("Next ${candleCount} minutes candel $next")

                // log.info(next.getDurationInMinutes())

                destinationChannel.send( MessageBuilder.withPayload(next).build() )
            } else {
                // candle is not valid for the current bucket -> start a new bucket
                Candle next = buildCandle( candleList, candleCount)
                candleList.clear()

                candleList.add( minuteCandle )

                destinationChannel.send( MessageBuilder.withPayload(next).build() )
            }
        }
    }

    static boolean isValidCandle(Candle firstCandle, Candle currentCandle, Integer candleCount ) {

        def minutesBetween = ChronoUnit.MINUTES.between( DateHelper.toLocalDateTime(firstCandle.end), DateHelper.toLocalDateTime(currentCandle.end) )

        if(minutesBetween >= candleCount) {
            // in case we have no candles...
            return false
        }

        return true

    }

    static boolean isAggregationComplete(Candle firstCandle, Candle lastCandle,  candleCount) {


        def currentMinute =  DateHelper.toLocalDateTime(lastCandle.end).getMinute() + 1

        if(candleCount <= 60) {
            return (currentMinute % candleCount) == 0
        } else {
            def currentHour =  DateHelper.toLocalDateTime(lastCandle.end).getHour() + 1
            return (currentMinute % candleCount) == 0 && (currentHour % 2 == 0)
        }

    }
/*
    static boolean isAggregationComplete(Date endDate, Integer candleCount) {

        def currentMinute =  DateHelper.toLocalDateTime(endDate).getMinute() + 1
        if(candleCount <= 60) {
            return (currentMinute % candleCount) == 0
        } else {
            def currentHour =  DateHelper.toLocalDateTime(endDate).getHour() + 1
            return (currentMinute % candleCount) == 0 && (currentHour % 2 == 0)
        }
    } */

    static List<Candle> aggregate(Integer candleCount, List<Candle> candles) {

        List<Candle> tempList = []

        List<Candle> aggregatedCandles = []

        candles.each { c ->

            Candle firstCandle = tempList.isEmpty() ? c : tempList.first()

            if(isValidCandle(firstCandle, c, candleCount )) {

                tempList.add(c)

                if(isAggregationComplete(tempList.first(), c, candleCount)) {
                    Candle next = buildCandle( tempList , candleCount)
                    tempList.clear()
                    aggregatedCandles << next
                }

            } else {
                // candle is not valid for the current bucket -> start a new bucket
                Candle next = buildCandle( tempList, candleCount)
                tempList.clear()
                aggregatedCandles << next

                tempList.add( c )
            }
        }

        return aggregatedCandles
    }


    private static Candle buildCandle(List<Candle> minuteCandleList, candleCount) {

        Candle first = minuteCandleList.first()

        Candle aggregatedCandle = minuteCandleList.tail().inject(first) { Candle candle, Candle next ->
            candle.market = next.market
            candle.high = Math.max(candle.high, next.high)
            candle.low = Math.min(candle.low, next.low)
            candle.close = next.close
            candle.end = next.end
            candle.volume += next.volume
            candle.volumnWeightedPrice += (next.volumnWeightedPrice * next.volume)
            candle.tradeCount += next.tradeCount
            return candle
        }

        aggregatedCandle.period = candleCount + "min"

        aggregatedCandle.start = first.start
        aggregatedCandle.open = first.open

        //TODo... fix start and end date based on candleCount in case of missing candles...

        if(aggregatedCandle.volume > 0.0) {
            aggregatedCandle.volumnWeightedPrice /= aggregatedCandle.volume
        }

        return aggregatedCandle
    }

}
