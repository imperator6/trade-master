package tradingmaster.strategy

import groovy.transform.CompileStatic

@CompileStatic
class RsiSettings {

    public Boolean enabled = true
    public Integer interval = 14
    public Integer low = 30
    public Integer high = 70
    public Integer persistence = 1

}
