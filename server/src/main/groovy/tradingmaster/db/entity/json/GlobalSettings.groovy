package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString
class GlobalSettings {

    TakeProfit takeProfit = new TakeProfit()

    TrailingStopLoss trailingStopLoss = new TrailingStopLoss()

    StopLoss stopLoss = new StopLoss()
}
