package tradingmaster.strategy;

import tradingmaster.model.Candle;

public interface Strategy {

    StrategyResult next(Candle c);
}
