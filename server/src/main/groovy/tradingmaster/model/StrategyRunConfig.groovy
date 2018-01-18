package tradingmaster.model

import groovy.transform.ToString

@ToString
class StrategyRunConfig {

    String id

    BigDecimal strategyId

    Map strategyParams

    String exchange

    String market

    String start

    String end

    Integer candleSize

    Integer warmup

    //StrategyConfig config

    boolean backtest = false

}
