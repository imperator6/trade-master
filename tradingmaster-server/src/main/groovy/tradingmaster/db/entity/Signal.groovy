package tradingmaster.db.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString
import tradingmaster.model.Candle

import javax.persistence.*

@Entity(name = "tradingSignal")
@ToString
class Signal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id

    @Column(nullable = true)
    Integer botId

    @Column(nullable = true)
    Integer positionId

    @Column(nullable = true)
    Integer extSignalId

    @Column(nullable = true)
    String exchange

    @Column(nullable = false)
    String buySell

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    @Column(nullable = false)
    Date signalDate

    @Column(nullable = false)
    String asset

    @Column(nullable = false, precision=25, scale=10)
    BigDecimal price

    @Column(nullable = false)
    String triggerName

    @Column(nullable = true)
    String triggerValues

    @Column(nullable = false)
    Integer executionCount = 0

    @JsonIgnore
    @Transient
    Candle candle


    //Map triggerValues = [:]

}
