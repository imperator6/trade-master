package tradingmaster.model;

import java.util.Date;
import java.util.List;

public interface IExchangeAdapter {


    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market);

    List<CryptoMarket> getMakets();


}
