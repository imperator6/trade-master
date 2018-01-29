package tradingmaster.db.entity

import groovy.transform.ToString
import groovy.util.logging.Commons
import org.springframework.stereotype.Component

import javax.persistence.*

@Entity(name = "orderHistory")
@Commons
@ToString
@Component
class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id

    @Column(nullable = false)
    String extOrderId

    @Column(nullable = false)
    String exchange

    @Column(nullable = false)
    String market

    Date date

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal quantity

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal price

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal pricePerUnit

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal commission


}
