package tradingmaster.util

import java.text.DecimalFormat

class NumberHelper {

    static String twoDigits(Number n) {
        DecimalFormat percentform = new DecimalFormat( "#,###.00")
        return percentform.format(n)
    }

    static BigDecimal addXPercentTo(BigDecimal value, BigDecimal percent ) {
        def toAdd =  (value * percent/ 100)
        return value + toAdd
    }
}
