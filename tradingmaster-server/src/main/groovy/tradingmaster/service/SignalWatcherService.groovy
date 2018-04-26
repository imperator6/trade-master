package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Component
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.BacktestMessage

import javax.annotation.PostConstruct

@Component
@Commons
class SignalWatcherService implements MessageHandler {

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PositionService positionService

    @Autowired
    PublishSubscribeChannel signalChannel

    @Autowired
    PublishSubscribeChannel executedSignalChannel

    @Autowired
    TaskExecutor signalExecutor

    @Autowired
    PublishSubscribeChannel backtestChannel

    @PostConstruct
    init() {
        signalChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {
        Signal s = message.getPayload()

        execute(s)
    }

    void execute(Signal s) {

        if(s.botId != null) {

            TradeBot bot = tradeBotManager.findBotById(s.botId)

            def task = {
                Position pos = handleSignal(bot, s)
                notifyBacktest(bot, s, pos)
            } as Runnable

            signalExecutor.execute(task)

        } else {
            // unknown bot --> try all
            tradeBotManager.getActiveBots().each { TradeBot bot ->

                def task = {
                    Position pos = handleSignal(bot, s)
                    notifyBacktest(bot, s, pos)
                } as Runnable

                signalExecutor.execute(task)
            }

        }


    }

    void notifyBacktest(TradeBot b, Signal s, Position pos) {
        if(b.backtest) {
            BacktestMessage msg = new BacktestMessage()
            msg.backtestId = s.candle.backtestId
            msg.action = "signalComplete"

            if(pos !=null) {
                msg.positionId = pos.getId()
            }

            backtestChannel.send( MessageBuilder.withPayload(msg).build() )
        }

    }

    Position handleSignal(TradeBot b, Signal s) {

        Position pos = null
/*
        String triggerName = s.getTriggerName()
        boolean skipSignal = true

        if(b.config.signal && b.config.signal.enabled) {
            List<String> listenTo =  b.config.signal.listenTo
            if(listenTo.contains(triggerName)) {
                skipSignal = false
            }
        }

        if(skipSignal) {
                log.info("Skipping signal for trigger: $triggerName bot: ${b.id}")
            return
        }
        */

        if(!tradeBotManager.isValidSignalForBot(b, s)) {
            return
        }

        if("buy".equalsIgnoreCase( s.getBuySell())) {

            synchronized (b) {

                Position posToOpen

                if(s.positionId != null) {
                    posToOpen = tradeBotManager.findPositionById(b.getId(), s.positionId)
                }

                if(posToOpen) {

                    def balanceToSpend

                    if(posToOpen.settings.buyWhen.spend > 0) {
                        balanceToSpend = posToOpen.settings.buyWhen.spend
                        log.info("Open a new position using 'buyWhen.spend'. balanceToSpend: ${balanceToSpend}")
                    } else if (posToOpen.settings.buyWhen.quantity > 0) {
                        balanceToSpend = posToOpen.settings.buyWhen.quantity * s.price
                        posToOpen.settings.buyWhen.spend = balanceToSpend
                        log.info("Open a new position using 'buyWhen.quantity'. balanceToSpend: ${balanceToSpend}")
                    } else {
                        balanceToSpend = tradeBotManager.calcBalanceForNextTrade(b)
                        posToOpen.settings.buyWhen.spend = balanceToSpend
                        log.info("Open a new position using 'tradeBotManager'. balanceToSpend: ${balanceToSpend}")
                    }


                    pos = positionService.openPosition(b, posToOpen, balanceToSpend , s.price, s.signalDate)
                    executedSignalChannel.send( MessageBuilder.withPayload(s).build() )

                } else {

                    pos = positionService.openPosition(b, s)
                    executedSignalChannel.send( MessageBuilder.withPayload(s).build() )
                }
            }

        } else if ("sell".equalsIgnoreCase( s.getBuySell())) {

            synchronized (b) {

                Position posToSell

                if(s.positionId != null) {
                    posToSell = tradeBotManager.findPositionById(b.getId(), s.positionId)
                }

                if(posToSell) {
                    pos = positionService.closePosition(posToSell, s.price, b, s.signalDate)
                    executedSignalChannel.send( MessageBuilder.withPayload(s).build() )
                } else {
                    log.fatal("No position to sell found.... ${s}")
                }
            }

        } else {
            log.error("Unsupported buysell flag ${s.getBuySell()}")
        }

        return pos
    }
}
