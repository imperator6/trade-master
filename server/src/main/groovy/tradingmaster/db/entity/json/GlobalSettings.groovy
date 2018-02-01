package tradingmaster.db.entity.json

import groovy.transform.ToString

@ToString
class GlobalSettings {

    TakeProfit takeProfit = new TakeProfit()

    TrailingStopLoss trailingStopLoss = new TrailingStopLoss()

    StopLoss stopLoss = new StopLoss()
}
