package tradingmaster.model;


public interface ITradeStore {


    Long getMaxTradeId(IMarket market);

    void persistTrades(TradeBatch batch);


}
