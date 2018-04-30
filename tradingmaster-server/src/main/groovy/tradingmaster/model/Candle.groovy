package tradingmaster.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

import javax.persistence.Transient
import java.time.Duration

@ToString
class Candle implements Serializable {

    CryptoMarket market

    String period

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date start

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date end

    BigDecimal open = 0.0
    BigDecimal high = 0.0
    BigDecimal low = Integer.MAX_VALUE
    BigDecimal close = 0.0

    BigDecimal volume = 0.0
    BigDecimal volumnWeightedPrice = 0.0

    Integer tradeCount = 0

    Integer botId = null

    @Transient
    Boolean backtest = false  // no back

    @JsonIgnore
    @Transient
    String backtestId = null

    transient Long getDurationInMinutes() {
        return Duration.between(start.toInstant(), end.toInstant()).toMinutes() + 1
    }

}
