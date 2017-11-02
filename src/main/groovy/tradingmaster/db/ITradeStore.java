package tradingmaster.db;

import tradingmaster.model.IMarket;
import tradingmaster.model.TradeBatch;

public interface ITradeStore {


    Long getMaxTradeId(IMarket market);

    void persistTrades(TradeBatch batch);


}
