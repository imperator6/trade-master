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


    static BigDecimal xPercentFromBase(BigDecimal base, BigDecimal value ) {
        if(base == 0.0) return 0
        def result =  (value / base * 100) - 100
        return result
    }

    static String formatNumber(Number n) {

        if(n == null || n == 0.0) return "0"

        if(Math.abs(n) > 1) {

            if(Math.abs(n) > 1000) {
                DecimalFormat noDecimals = new DecimalFormat( "#,###")
                return noDecimals.format(n)
            }

            return twoDigits(n)
        } else {
            DecimalFormat s8= new DecimalFormat( "#.00000000")
            return s8.format(n)
        }
    }

}
