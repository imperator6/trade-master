package tradingmaster.db.entity

import groovy.transform.ToString

import javax.persistence.*
import java.util.concurrent.ConcurrentHashMap

@Entity
@ToString
class TradeBot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id

    @Column(nullable = false)
    Integer configId // script strategy hold the config with all parmas --> Map config

    @Column(nullable = false)
    boolean backtest = false

    @Column(nullable = false)
    String exchange

    // BTC, ETH, USDT
    @Column(nullable = false)
    String baseCurrency

    @Column(nullable = false, precision=25, scale=10)
    BigDecimal startBalance

    @Column(nullable = false, precision=25, scale=10)
    BigDecimal currentBalance = 0

    @Column(nullable = false)
    boolean active = true

    @Transient
    Map config

    @Transient
    Map<Integer, Position> positionMap = new ConcurrentHashMap<Integer,Position>()

    @Transient
    BigDecimal fxDollar // fx for base currency to dollar

    @Transient
    BigDecimal startBalanceDollar

    @Transient
    BigDecimal currentBalanceDollar

    @Transient
    BigDecimal totalBalanceDollar

    @Transient
    BigDecimal totalBaseCurrencyValue

    @Transient
    BigDecimal result




    List<Position> getPositions() {
       return new ArrayList(positionMap.values())
    }

    Position addPosition(Position pos) {
        return this.positionMap.put(pos.getId(), pos)
    }

    Position removePosition(Position pos) {
        return this.positionMap.remove(pos.getId())
    }






    /*

    String currencyName

    String assetName

    BigDecimal asset
    BigDecimal slippage
    BigDecimal tradingFee

    int trades = 0

    // calculated
    BigDecimal fee

    BigDecimal startPrice
    BigDecimal endPrice
    BigDecimal startCurrency
    BigDecimal startAsset

    BigDecimal holdBalance

    BigDecimal balance
 */

}
