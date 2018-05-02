package tradingmaster.db.entity.json

import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.AutoClone
import groovy.transform.ToString

@AutoClone
@ToString
class Backtest {

    Boolean enabled = false

    BigDecimal startBalance = 1000
    BigDecimal fee = 0.001

    String market

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date startDate

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date endDate

}
