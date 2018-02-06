package tradingmaster.strategy.indicator;


interface Indicator<T> {

    T update(BigDecimal n)

}
