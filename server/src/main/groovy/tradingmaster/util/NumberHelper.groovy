package tradingmaster.util

import java.text.DecimalFormat

class NumberHelper {


    static String twoDigits(Number n) {
        DecimalFormat percentform = new DecimalFormat( "#,###.00")
        return percentform.format(n)
    }
}
