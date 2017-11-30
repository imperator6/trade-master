package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.model.Candle


@Commons
class CandleAggregator implements MessageHandler {

    private int candleCount = 0

    CandleAggregator(int candleCount) {
        this.candleCount = candleCount
    }

    List<Candle> candleList = Collections.synchronizedList(new ArrayList<Candle>())

    @Override
    synchronized void handleMessage(Message<?> message) throws MessagingException {

        Candle minuteCandle = message.getPayload()

        candleList.add(minuteCandle)

        if(candleList.size() == candleCount) {

            Candle next = buildCandle( candleList )
            candleList.clear()

            log.info("Next  ${candleCount} minutes candel $next")

            log.info(next.getDurationInMinutes())

        }



    }

    Candle buildCandle(List<Candle> minuteCandleList) {

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

        aggregatedCandle.start = first.start
        aggregatedCandle.open = first.open

        if(aggregatedCandle.volume > 0.0) {
            aggregatedCandle.volumnWeightedPrice /= aggregatedCandle.volume
        }

        return aggregatedCandle

    }

}
