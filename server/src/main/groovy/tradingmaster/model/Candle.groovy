package tradingmaster.model

import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.ToString

import java.time.Duration

@ToString
class Candle {

    IMarket market

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


    transient Long getDurationInMinutes() {
        return Duration.between(start.toInstant(), end.toInstant()).toMinutes() + 1
    }

}
