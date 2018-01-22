package tradingmaster.db.entity

import groovy.transform.ToString

import javax.persistence.*

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
    List<Position> positions = []


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
