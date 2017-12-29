package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.stereotype.Service
import tradingmaster.model.BuySell
import tradingmaster.model.Candle
import tradingmaster.model.PaperPortfolio
import tradingmaster.model.PortfolioChange

import java.text.DecimalFormat

@Service
@Commons
class PaperPortfolioService {

    void init(Map params, PaperPortfolio p) {
        p.assetName = params.assetName
        p.currencyName = params.currencyName

        p.currency = params.currency ? params.currency as BigDecimal : 0
        p.asset = params.asset ? params.asset as BigDecimal : 0.1
        p.startCurrency = p.currency
        p.startAsset = p.asset

        p.tradingFee = params.slippage ? params.slippage as BigDecimal : 0.25
        p.slippage = params.slippage ? params.slippage as BigDecimal : 0.05

        p.fee = (p.tradingFee + p.slippage) / 100

        log.info("A new paper portfolio has ben initilized: ${p}")
    }

    void onFirstCandle(Candle c, PaperPortfolio p) {
        p.startPrice = c.close
        p.startBalance = p.currency + c.close * p.asset
        log.info("Starting balance for portfolio ${p.currencyName}-${p.assetName} is ${format(p.startBalance)} $p.currencyName ${format(p.asset)} $p.assetName")
    }

    void onLastCandle(Candle c, PaperPortfolio p) {
        p.endPrice = c.close
        p.holdBalance = p.startCurrency + c.close * p.startAsset
        log.info("${p.trades} Trades. Balance: ${format(p.balance)} vs. " +
                "is ${format(p.holdBalance)} $p.currencyName Hold Balance. startAsset ${format(p.startAsset)} $p.assetName." +
                " startPrice: ${format(p.startPrice)} $p.currencyName endPrice: ${format(p.endPrice)} $p.currencyName ")
    }

    BigDecimal extractFee(BigDecimal amount, BigDecimal fee) {
        amount *= 1e8
        amount *= fee
        amount = Math.floor(amount)
        amount /= 1e8
        return amount
    }


    PortfolioChange buy(BigDecimal price, BigDecimal amount, Candle c, PaperPortfolio p) {

        def tradePrice = price
        def fee = extractFee(p.currency / price, p.fee)
        PortfolioChange pc = new PortfolioChange()
        pc.date = c.end
        pc.type = BuySell.BUY.getName()
        pc.value = tradePrice
        pc.fee = fee

        pc.assetOld = p.asset
        p.asset +=  p.currency / tradePrice //extractFee(this.currency / price)
        pc.assetNew = p.asset

        pc.currencyOld = p.currency
        p.currency = 0
        pc.currencyNew = p.currency

        pc.balanceOld = p.balance
        p.balance = p.currency + tradePrice * p.asset
        pc.balanceNew = p.balance

        p.trades++
        pc.tradeNumber = p.trades

        log.debug("New buy trade @${format(tradePrice)} $p.currencyName Balance is now: ${format(p.balance)} $p.currencyName ${format(p.asset)} $p.assetName")
        return pc
    }

    PortfolioChange sell(BigDecimal price, BigDecimal amount, Candle c, PaperPortfolio p) {

        def tradePrice = price
        def fee = extractFee(p.asset * price, p.fee)
        PortfolioChange pc = new PortfolioChange()
        pc.date = c.end
        pc.type = BuySell.SELL.getName()
        pc.value = tradePrice
        pc.fee = fee

        pc.currencyOld = p.currency
        p.currency += p.asset * tradePrice //extractFee(asset * price)
        pc.currencyNew = p.currency

        pc.assetOld = p.asset
        p.asset = 0
        pc.assetNew = p.asset

        pc.balanceOld = p.balance
        p.balance = p.currency + tradePrice * p.asset
        pc.balanceNew = p.balance

        p.trades++
        pc.tradeNumber = p.trades

        log.debug("New sell trade @${format(tradePrice)} $p.currencyName Balance is now: ${format(p.balance)} $p.currencyName ${format(p.asset)} $p.assetName")
        return pc
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
