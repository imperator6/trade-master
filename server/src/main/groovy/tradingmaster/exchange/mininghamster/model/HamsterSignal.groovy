package tradingmaster.exchange.mininghamster.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class HamsterSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id

    @JsonProperty("time")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Date signalDate

    String market

    @Column(precision=25, scale=10)
    BigDecimal lastprice

    String signalmode

    String exchange

}
