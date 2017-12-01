package tradingmaster.model;


import java.time.LocalDateTime;

public interface ITradeStore {


    Long getMaxTradeId(IMarket market);

    void persistTrades(TradeBatch batch);

    TradeBatch loadTrades(String exchange, String market, LocalDateTime start, LocalDateTime endDate);


}
