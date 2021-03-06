package tradingmaster.db.entity

import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.AutoClone
import groovy.transform.ToString
import groovy.util.logging.Commons
import org.hibernate.annotations.Type
import org.springframework.stereotype.Component
import tradingmaster.db.entity.json.PositionSettings

import javax.persistence.*

@Entity(name = "positions")
@Commons
@ToString
@Component
@AutoClone
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
    @Column(nullable = true)
    Integer buySignalId

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
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

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal signalRate

   // String currencyName
   // String assetName

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal amount

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date buyDate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal buyRate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal buyFee

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal totalBuy

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
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

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal maxResult

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal minResult

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal traceResult

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal buyFx

    boolean closed = false

    boolean error = false

    @Column(nullable = true)
    String errorMsg

    boolean sellInPogress = false

    boolean buyInPogress = false

    @Column(nullable = true)
    BigDecimal trailingStopLoss // in percent

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal lastKnowRate

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal lastKnowBaseCurrencyValue // Quantitiy * lastKnowRate

    @Column(nullable = true)
    String age

    @Column(nullable = true)
    Date lastUpdate = new Date()

    @Type(type = "json")
    //@Column(columnDefinition = "json")
    @Column(columnDefinition = "TEXT")
    PositionSettings settings = new PositionSettings()

    @Column(columnDefinition = "TEXT")
    String comment

    //boolean deleted = false

}
