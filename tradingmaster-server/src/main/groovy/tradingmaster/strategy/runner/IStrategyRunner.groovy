package tradingmaster.strategy.runner

import tradingmaster.db.entity.Signal
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.Candle

interface IStrategyRunner {

    void init(TradeBot bot)

    List<Signal> nextCandle(Candle c)

    void close()

    void resetStrategies(Candle c)

}