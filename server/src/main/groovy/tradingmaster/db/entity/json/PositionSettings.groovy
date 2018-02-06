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


}
