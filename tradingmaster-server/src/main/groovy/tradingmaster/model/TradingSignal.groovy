package tradingmaster.model

import com.fasterxml.jackson.annotation.JsonFormat

class TradingSignal {

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date date

    String type

    BigDecimal value

}
