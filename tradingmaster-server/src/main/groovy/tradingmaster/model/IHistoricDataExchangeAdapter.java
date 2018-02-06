package tradingmaster.model;

import java.util.Date;
import java.util.List;

public interface IHistoricDataExchangeAdapter {

    List<Candle> getCandles(Date startDate, Date endDate, CryptoMarket market, CandleInterval interval);


}
