package tradingmaster.model

import groovy.transform.ToString
import groovy.util.logging.Commons
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Commons
@ToString
@Component
@Scope("prototype")
class PaperPortfolio implements IPortfolio {

    String currencyName

    String assetName

    BigDecimal currency
    BigDecimal asset
    BigDecimal slippage
    BigDecimal tradingFee

    int trades = 0

    // calculated
    BigDecimal fee

    BigDecimal startPrice
    BigDecimal endPrice
    BigDecimal startCurrency
    BigDecimal startAsset
    BigDecimal startBalance
    BigDecimal holdBalance

    BigDecimal balance


//    void init(Map params) {
//        this.assetName = params.assetName
//        this.currencyName = params.currencyName
//
//        this.currency = params.currency ? params.currency as BigDecimal : 0
//        this.asset = params.asset ? params.asset as BigDecimal : 0.1
//        this.startCurrency = this.currency
//        this.startAsset = this.asset
//
//        this.tradingFee = params.slippage ? params.slippage as BigDecimal : 0.25
//        this.slippage = params.slippage ? params.slippage as BigDecimal : 0.05
//
//        fee = (tradingFee + slippage) / 100
//
//        log.info("portfolio has ben initilized with ${this}")
//    }

//    void onFirstCandle(Candle c) {
//        this.startPrice = c.close
//        this.startBalance = this.currency + c.close * this.asset
//        log.info("Starting balance for portfolio ${currencyName}-${assetName} is ${format(startBalance)} $currencyName ${format(this.asset)} $assetName")
//    }
//
//    void onLastCandle(Candle c) {
//        this.endPrice = c.close
//        this.holdBalance = this.startCurrency + c.close * this.startAsset
//        log.info("${trades} Trades. Balance: ${format(this.balance)} vs. " +
//                "is ${format(holdBalance)} $currencyName Hold Balance. startAsset ${format(this.startAsset)} $assetName." +
//                " startPrice: ${format(startPrice)} $currencyName endPrice: ${format(endPrice)} $currencyName ")
//    }

//    BigDecimal extractFee(BigDecimal amount) {
//        amount *= 1e8
//        amount *= this.fee
//        amount = Math.floor(amount)
//        amount /= 1e8
//        return amount
//    }

//    void buy(BigDecimal price, BigDecimal amount, Candle c) {
//
//        def tradePrice = price
//        def fee = extractFee(this.currency / price)
//        PortfolioChange pc = new PortfolioChange()
//        pc.date = c.end
//        pc.type = BuySell.BUY.getName()
//        pc.value = tradePrice
//        pc.fee = fee
//
//        pc.assetOld = this.asset
//        this.asset +=  this.currency / tradePrice //extractFee(this.currency / price)
//        pc.assetNew = this.asset
//
//        pc.currencyOld = this.currency
//        this.currency = 0
//        pc.currencyNew = this.currency
//
//        pc.balanceOld = this.balance
//        this.balance = this.currency + tradePrice * this.asset
//        pc.balanceNew = this.balance
//
//        this.trades++
//        pc.tradeNumber = this.trades
//
//        changes << pc
//        log.debug("New buy trade @${format(tradePrice)} $currencyName Balance is now: ${format(this.balance)} $currencyName ${format(this.asset)} $assetName")
//    }

//    void sell(BigDecimal price, BigDecimal amount, Candle c) {
//
//        def tradePrice = price
//        def fee = extractFee(asset * price)
//        PortfolioChange pc = new PortfolioChange()
//        pc.date = c.end
//        pc.type = BuySell.SELL.getName()
//        pc.value = tradePrice
//        pc.fee = fee
//
//        pc.currencyOld = this.currency
//        this.currency += asset * tradePrice //extractFee(asset * price)
//        pc.currencyNew = this.currency
//
//        pc.assetOld = this.asset
//        this.asset = 0
//        pc.assetNew = this.asset
//
//        pc.balanceOld = this.balance
//        this.balance = this.currency + tradePrice * this.asset
//        pc.balanceNew = this.balance
//
//        this.trades++
//        pc.tradeNumber = this.trades
//
//        changes << pc
//        log.debug("New sell trade @${format(tradePrice)} $currencyName Balance is now: ${format(this.balance)} $currencyName ${format(this.asset)} $assetName")
//    }


//    static  String format(BigDecimal value) {
//        DecimalFormat myFormatter = null
//        if(value > 1) {
//             myFormatter = new DecimalFormat( "###.###")
//        } else {
//             myFormatter = new DecimalFormat( "#.#######")
//        }
//
//        return myFormatter.format(value)
//
//    }







}
