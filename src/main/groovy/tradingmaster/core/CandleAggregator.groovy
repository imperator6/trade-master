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

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Candle minuteCandle = message.getPayload()

        log.info("Next minute candel received for aggregation of $candleCount candles: $minuteCandle")

    }

}
