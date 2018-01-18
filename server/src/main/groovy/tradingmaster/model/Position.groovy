package tradingmaster.model

import groovy.transform.ToString
import groovy.util.logging.Commons
import org.springframework.context.annotation.Scope
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
    BigDecimal cost

    BigDecimal buyRate
    BigDecimal buyFee

    BigDecimal sellRate
    BigDecimal sellFee

    BigDecimal total
    BigDecimal result // result in percent

    String triggerName

    boolean open = true
    boolean hold = false


}
