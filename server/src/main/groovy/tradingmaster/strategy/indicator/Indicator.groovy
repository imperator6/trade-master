package tradingmaster.strategy.indicator;

import tradingmaster.model.Candle;

public interface Indicator<T> {

    T update(Candle c);

}
