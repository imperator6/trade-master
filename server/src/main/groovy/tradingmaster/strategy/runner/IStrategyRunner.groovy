package tradingmaster.strategy.runner

import tradingmaster.db.entity.TradeBot
import tradingmaster.model.Candle

interface IStrategyRunner {

    void init(TradeBot bot)

    void nextCandle(Candle c)

    void close()

}