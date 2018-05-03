package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString
import tradingmaster.strategy.DemaSettings
import tradingmaster.strategy.MacdSettings
import tradingmaster.strategy.RsiSettings

@AutoClone
@ToString
class Config {

    String exchange = "binance"
    String baseCurrency = "USDT"
    String chartLink = 'https://us.binance.com/trade.html?symbol=$asset_$baseCurrency'

    BigDecimal warmup = 12 // warmup candle count

    Backtest backtest = new Backtest()

    BigDecimal amountPerOrder = 1000
    BigDecimal maxOpenPositions = 1

    Integer candleSize = 1
    Boolean liveTrading = false

    BigDecimal sellPriceLimitPercent = -2
    BigDecimal buyPriceLimitPercent = 1

    AssetFilter assetFilter = new AssetFilter()


    TakeProfit takeProfit = new TakeProfit()
    StopLoss stopLoss = new StopLoss()
    TrailingStopLoss trailingStopLoss = new TrailingStopLoss()

    Boolean resetStrategiesOnStopLoss = true
    BigDecimal dustTradeProtection = 0 // don't sell within x% range

    DemaSettings dema = new DemaSettings()

    MacdSettings macd = new MacdSettings()

    RsiSettings rsi = new RsiSettings()

    // for backtest

}
