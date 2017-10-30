package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.model.TradeBatch

@Commons
class TradeWriter implements MessageHandler {

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        TradeBatch tradeBatch = message.getPayload()

        log.info("Writing ${tradeBatch.trades.size()} trades from ${tradeBatch.exchange} for market ${tradeBatch.market.name}")

        // TODO..



    }
}
