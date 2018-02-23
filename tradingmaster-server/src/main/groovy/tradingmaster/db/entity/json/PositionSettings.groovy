package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString(includeSuper = true)
class PositionSettings extends GlobalSettings {

    Boolean holdPosition = false

    Boolean traceClosedPosition = false

    Boolean pingPong = false

    BuyWhen buyWhen = new BuyWhen()

    ReBuy rebuy = new ReBuy()

    List alerts = [new Alert(-10), new Alert(2), new Alert(10), new Alert(25)]

    //Alert alert = new Alert()

}
