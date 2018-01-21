package tradingmaster.service

import groovy.json.JsonSlurper
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.PositionRepository
import tradingmaster.db.TradeBotRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.mariadb.MariaStrategyStore
import tradingmaster.exchange.ExchangeService
import tradingmaster.exchange.paper.PaperExchange
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.ScriptStrategy

import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap

@Service
@Commons
class TradeBotManager {

    static Map<Integer, TradeBot> TRADE_BOT_MAP = new ConcurrentHashMap()

    @Autowired
    ExchangeService exchangeService

    @Autowired
    MariaStrategyStore strategyStore

    @Autowired
    TradeBotRepository tradeBotRepository

    @Autowired
    PositionRepository positionRepository


    List<TradeBot> getActiveBots() {
        return new ArrayList(this.TRADE_BOT_MAP.values().findAll { it.active })
    }

    void startBots() {

        tradeBotRepository.findByActive(true).each { TradeBot b ->

            log.info("********************************************")
            log.info("* Starting trading bot $b")
            log.info("********************************************")

            // load the config
            ScriptStrategy strategy = strategyStore.loadStrategyById(b.configId, null)
            Map params = new JsonSlurper().parseText(strategy.getScript())

            b.config = params

            b.positions = positionRepository.findByBotId(b.id)


            TRADE_BOT_MAP.put( b.getId(), b)

        }

    }

    void syncBanlance(TradeBot b) {
        // todo..
        b.startBalance = getExchangeAdapter(b).getBalance(b.config.baseCurrency).getValue()
    }

    IExchangeAdapter getExchangeAdapter(TradeBot b) {
        if(b.isBacktest()) {

            PaperExchange exchange = new PaperExchange()
            exchange.config = b.config
            return exchange

        } else {
           return exchangeService.getExchangyByName( b.config.exchange )
        }
    }


    TradeBot createNewBot(Integer configId, boolean backtest) {

        TradeBot p = new TradeBot()
        p.configId = configId
        p.backtest = backtest

        ScriptStrategy strategy = strategyStore.loadStrategyById(configId, null)

        Map params = new JsonSlurper().parseText(strategy.getScript())

        p.config = params
        p.baseCurrency = params.baseCurrency
        p.exchange = params.exchange

        syncBanlance(p)

        tradeBotRepository.save(p)

        log.info("A new trade bot has been initilized: ${p}")

        return p
    }

    void handleSignal(TradeBot b, Signal s) {

        if("buy".equalsIgnoreCase( s.getBuySell())) {

            if(isValidSignalForBot(b, s)) {
                openPosition(b, s)
            }

        } else if ("sell".equalsIgnoreCase( s.getBuySell())){


            // TODO... implement ...

        } else {
            log.error("Unsupported buysell flag ${s.getBuySell()}")
        }
    }


    boolean isValidSignalForBot(TradeBot b, Signal s) {
        boolean valid = false

        if(s.getExchange().equalsIgnoreCase(b.exchange)) {

            // check if asset is supported
            //getExchangeAdapter(b).

            if(b.positions.findAll { !it.closed }.size() < b.config.maxPositions) {
                valid = true
            } else {
                log.info("Max open positions  (${b.config.maxPosition}) has reached! Signal $s.id is not valid for TradeBot $b.id. ")
            }


        } else {
            log.info("Signal $s.id is not valid for TradeBot $b.id. Exchange does not match! $s.exchange != $b.exchange")
        }

        return valid
    }

    void openPosition(TradeBot bot, Signal s) {

        // TODO: calculate based on settings
        BigDecimal currencyAmount = nextTradeBalance(bot)

        if(currencyAmount > 0) {

            Position pos = new Position()

            pos.created = new Date()
            pos.botId = bot.getId()
            pos.buySignalId = s.getId()

            pos.market = "${bot.config.baseCurrency}-${s.asset}"
            pos.status = "placing buy order"
            pos.signalRate = s.price

            // Too: call exc to place the order...
            def currency  = bot.config.baseCurrency
            def asset = s.asset
            def price = s.price

            // clac price range ....

            /* pos.buyFee = 0.0
            pos.buyRate = tradePrice
            pos.amount = currencyAmount / tradePrice //extractFee(this.currency / price)
            pos.totalBuy =  pos.amount + pos.buyFee

            */

            positionRepository.save(pos)
            bot.positions << pos

            // TODO... place the order...
            getExchangeAdapter(b).

            log.debug "New position created! $pos"
        } else {
            // balance to small

        }
    }

    BigDecimal extractFee(BigDecimal amount, BigDecimal fee) {
        amount *= 1e8
        amount *= fee
        amount = Math.floor(amount)
        amount /= 1e8
        return amount
    }

    void closePosition(Position p, Signal s, TradeBot bot) {

        bot.signals << s

        // TODO: Delegate to exchange
        if(p.isOpen()) {

            p.extSellOrderId = UUID.randomUUID()

            p.sellRate = s.price
            p.sellFee = 0
            p.totalSell = (p.sellRate * p.amount) - p.sellFee

            p.total = p.totalBuy - p.totalSell
            p.open = false
        }

    }

    BigDecimal nextTradeBalance(TradeBot bot) {
        def next = bot.startBalance / 10

        // Todo: check balance on exchange

        return next
    }


    static  String format(BigDecimal value) {
        DecimalFormat myFormatter = null
        if(value > 1) {
            myFormatter = new DecimalFormat( "###.###")
        } else {
            myFormatter = new DecimalFormat( "#.#######")
        }

        return myFormatter.format(value)
    }

}
