package tradingmaster.core

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import tradingmaster.db.ITradeStore
import tradingmaster.model.TradeBatch

@Commons
class TradeWriter implements MessageHandler {

    @Autowired
    ITradeStore store

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        TradeBatch tradeBatch = message.getPayload()

        log.debug("${tradeBatch.market.name}: Writing  ${tradeBatch.trades.size()} trades from ${tradeBatch.market.exchange}")

        store.persistTrades(tradeBatch)
    }
}
