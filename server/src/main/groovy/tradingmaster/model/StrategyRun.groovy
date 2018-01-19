package tradingmaster.model;

interface StrategyRun {

    void nextCandle(Candle c)

    void close()

    BacktestResult getResult()
}
