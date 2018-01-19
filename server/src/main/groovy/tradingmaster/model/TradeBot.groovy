package tradingmaster.model

import groovy.transform.ToString
import groovy.util.logging.Commons
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Commons
@ToString
@Component
@Scope("prototype")
class TradeBot implements IPortfolio {

    Map config

    boolean backtest = false

    List<Position> positions = []

    IExchangeAdapter exchange

    // BTC, ETH, USDT
    String baseCurrency

    BigDecimal startBalance


    BigDecimal nextTradeBalance() {
        def next = startBalance / 10

        // Todo: check balance on exchange

        return next
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
