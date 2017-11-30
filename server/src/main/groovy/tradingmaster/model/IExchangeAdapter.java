package tradingmaster.model;

import java.util.Date;

public interface IExchangeAdapter {

    TradeBatch getTrades(Date startDate, Date endDate, IMarket market);

}
