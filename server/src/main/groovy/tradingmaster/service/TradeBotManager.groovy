package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.exchange.ExchangeService
import tradingmaster.exchange.paper.PaperExchange
import tradingmaster.model.*

import java.text.DecimalFormat

@Service
@Commons
class TradeBotManager {

    @Autowired
    ExchangeService exchangeService

    void init(Map params, TradeBot p, boolean backtest) {

       /* p.assetName = params.assetName
        p.currencyName = params.currencyName

        p.currency = params.currency ? params.currency as BigDecimal : 0
        p.asset = params.asset ? params.asset as BigDecimal : 0.1
        p.startCurrency = p.currency
        p.startAsset = p.asset

        p.tradingFee = params.slippage ? params.slippage as BigDecimal : 0.25
        p.slippage = params.slippage ? params.slippage as BigDecimal : 0.05

        p.fee = (p.tradingFee + p.slippage) / 100 */

        p.config = params
        p.baseCurrency = params.baseCurrency

        IExchangeAdapter exchangeAdapter = null

        if(backtest) {
            p.backtest = backtest

            PaperExchange exchange = new PaperExchange()
            exchange.config = params
            exchangeAdapter = exchange

        } else {
            exchangeAdapter = exchangeService.getExchangyByName( p.exchange )
        }

        p.exchange = exchangeAdapter

        // Todo... load/sync open position from db
        p.startBalance = exchangeAdapter.getBalance(params.baseCurrency).getValue()


        log.info("A new trade bot has been initilized: ${p}")
    }

    void onFirstCandle(Candle c, TradeBot p) {
      //  p.startPrice = c.close
      //  p.startBalance = p.currency + c.close * p.asset
      //  log.info("Starting balance for portfolio ${p.currencyName}-${p.assetName} is ${format(p.startBalance)} $p.currencyName ${format(p.asset)} $p.assetName")
    }

    void onLastCandle(Candle c, TradeBot p) {
       // p.endPrice = c.close
       // p.holdBalance = p.startCurrency + c.close * p.startAsset
/*        log.info("${p.trades} Trades. Balance: ${format(p.balance)} vs. " +
                "is ${format(p.holdBalance)} $p.currencyName Hold Balance. startAsset ${format(p.startAsset)} $p.assetName." +
                " startPrice: ${format(p.startPrice)} $p.currencyName endPrice: ${format(p.endPrice)} $p.currencyName ") */
    }

    BigDecimal extractFee(BigDecimal amount, BigDecimal fee) {
        amount *= 1e8
        amount *= fee
        amount = Math.floor(amount)
        amount /= 1e8
        return amount
    }


    void openPosition(String triggerName, Candle c, TradeBot bot) {

        // TODO: Delegate to exchange
        BigDecimal currencyAmount = bot.nextTradeBalance()

        if(currencyAmount > 0) {
            def tradePrice = c.close

            Position pos = new Position()
            pos.id = 1
            pos.extbuyOrderId = UUID.randomUUID()
            pos.date = new Date()
            pos.buyFee = 0.0
            pos.buyRate = tradePrice
            pos.amount = currencyAmount / tradePrice //extractFee(this.currency / price)
            pos.triggerName = triggerName
            pos.totalBuy =  pos.amount + pos.buyFee

            pos.assetName = c.getMarket().getAsset()
            pos.currencyName = bot.config.baseCurrency

            // add sync method
            bot.positions << pos

            log.debug "New position $pos"
        } else {
            // balance to small

        }
    }

    void closePosition(String triggerName, Position p, Candle c, TradeBot bot) {

        // TODO: Delegate to exchange
        if(p.isOpen()) {

            p.extSellOrderId = UUID.randomUUID()

            p.sellRate = c.close
            p.sellFee = 0
            p.totalSell = (p.sellRate * p.amount) - p.sellFee

            p.total = p.totalBuy - p.totalSell
            p.open = false
        }

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
