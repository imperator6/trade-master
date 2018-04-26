package tradingmaster.model

import tradingmaster.db.entity.Signal

class TradingContext {

    // optional
    Integer botId

    boolean backtest

    Date start

    Date end

    List<Candle> candles = []

    List<Signal> signals = []


}
