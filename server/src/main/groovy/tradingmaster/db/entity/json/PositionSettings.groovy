package tradingmaster.db.entity.json

import groovy.transform.ToString

@ToString(includeSuper = true)
class PositionSettings extends GlobalSettings {

    Boolean holdPosition = false

    BuyWhen buyWhen = new BuyWhen()


}
