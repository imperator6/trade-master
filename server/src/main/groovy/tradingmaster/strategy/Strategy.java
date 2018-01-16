package tradingmaster.strategy;

import tradingmaster.model.Candle;

public interface Strategy {

    String getName();

    StrategyResult execute(Candle c);
}
