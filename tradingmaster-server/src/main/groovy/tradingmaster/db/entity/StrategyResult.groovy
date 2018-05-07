package tradingmaster.db.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import groovy.transform.ToString;

import javax.persistence.Column;
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Entity
@ToString
public class StrategyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id

    Integer botId

    String name

    String market

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal price


    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    @Column(nullable = false)
    Date priceDate

    String advice = "neutral"; // short, long

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal value1

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal value2

    @Column(nullable = true, precision=25, scale=10)
    BigDecimal value3

}
