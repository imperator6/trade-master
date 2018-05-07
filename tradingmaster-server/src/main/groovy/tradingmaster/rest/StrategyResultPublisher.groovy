package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.stereotype.Controller
import tradingmaster.db.SignalRepository
import tradingmaster.db.StrategyResultRepository
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.StrategyResult
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.BacktestMessage
import tradingmaster.service.TradeBotManager

import javax.annotation.PostConstruct

@Controller
@Commons
class StrategyResultPublisher implements MessageHandler {

    @Autowired
    StrategyResultRepository strategyResultRepository

    MessageSendingOperations<String> messagingTemplate

    @Autowired
    PublishSubscribeChannel strategyResultChannel

    @Autowired
    PublishSubscribeChannel backtestChannel

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    StrategyResultPublisher(MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate
    }

    List msgCache = []

    @PostConstruct
    init() {
        strategyResultChannel.subscribe(this)
        backtestChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        //log.info("Publishing new signal signal")

        Object obj =  message.getPayload()

        if(obj instanceof  StrategyResult) {

            StrategyResult res = (StrategyResult) obj

            TradeBot bot = tradeBotManager.findBotById(res.getBotId())

            if(bot.config.backtest.enabled) {

                synchronized (msgCache) {

                    msgCache.add( res )

                    if(msgCache.size() > 1000) {
                        strategyResultRepository.save( msgCache )
                        msgCache.clear()
                    }

                    // messagingTemplate.convertAndSend("/topic/strategyResult".toString() , res)
                }

            } else {
                strategyResultRepository.save( message.getPayload() )
                messagingTemplate.convertAndSend("/topic/strategyResult".toString() , res)
            }
        } else if (obj instanceof BacktestMessage) {

            // Write all masseges left in the cache
            BacktestMessage msg = (BacktestMessage) obj

            if("backtestComplete".equals(msg.getAction())) {
                synchronized (msgCache) {
                    strategyResultRepository.save( msgCache )
                    msgCache.clear()
                }
            }
        }
    }


}
