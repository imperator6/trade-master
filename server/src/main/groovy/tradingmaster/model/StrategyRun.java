package tradingmaster.model;

public interface StrategyRun {

    void nextCandle(Candle c);

    void close();

    BacktestResult getResult();
}
