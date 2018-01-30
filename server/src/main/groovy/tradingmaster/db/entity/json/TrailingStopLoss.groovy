package tradingmaster.db.entity.json

class TrailingStopLoss {

    Boolean enabled

    BigDecimal value  // sell if it lost 5% after the 20% has reached

    BigDecimal startAt  // start trailing at 20% profit
}
