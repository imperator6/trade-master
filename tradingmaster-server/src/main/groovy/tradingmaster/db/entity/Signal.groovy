package tradingmaster.db.entity

import groovy.transform.ToString

import javax.persistence.*

@Entity(name = "tradingSignal")
@ToString
class Signal {

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

    //Map triggerValues = [:]

}
