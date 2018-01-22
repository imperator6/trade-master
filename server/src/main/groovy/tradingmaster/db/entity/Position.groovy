package tradingmaster.db.entity

import groovy.transform.ToString
import groovy.util.logging.Commons
import org.springframework.stereotype.Component

import javax.persistence.*

@Entity(name = "positions")
@Commons
@ToString
@Component
class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id

    //@OneToOne(cascade = CascadeType.ALL)
    //@JoinColumn(name = "botId")
    @Column(nullable = false)
    Integer botId

    //@OneToOne(cascade = CascadeType.ALL)
    //@JoinColumn(name = "buySignalId")
    @Column(nullable = false)
    Integer buySignalId

    @Column(nullable = false)
    Date created = new Date()

    @Column(nullable = true)
    String extbuyOrderId

    @Column(nullable = true)
    String extSellOrderId

    @Column(nullable = false)
    String market

    @Column(nullable = false)
    String status

    @Column(nullable = false, precision=25, scale=10)
    BigDecimal signalRate

   // String currencyName
   // String assetName

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal amount

    Date buyDate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal buyRate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal buyFee

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal totalBuy

    Date sellDate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal sellRate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal sellFee

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal totalSell

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal total

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal result

    //String triggerName
    boolean holdPosition = false

    boolean closed = false

    boolean error = false

    @Column(nullable = true)
    String errorMsg


}
