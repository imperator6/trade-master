package tradingmaster.model

import groovy.transform.ToString
import groovy.util.logging.Commons
import org.springframework.stereotype.Component

@Commons
@ToString
@Component
class Position {

    Integer id

    String extbuyOrderId

    String extSellOrderId

    Date date

    String currencyName
    String assetName

    BigDecimal amount


    BigDecimal buyRate
    BigDecimal buyFee
    BigDecimal totalBuy

    BigDecimal sellRate
    BigDecimal sellFee
    BigDecimal totalSell

    BigDecimal total
    BigDecimal result // result in percent

    String triggerName

    boolean open = true

    boolean hold = false


}
