package tradingmaster.db.entity.json

import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString
class AssetFilter {

    Boolean enabled = false

    List<String> forbidden = []
    List<String> allowed = []


}
