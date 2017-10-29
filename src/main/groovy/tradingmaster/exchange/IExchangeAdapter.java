package tradingmaster.exchange;

import tradingmaster.model.IMarket;
import tradingmaster.model.ITrade;

import java.util.Date;
import java.util.List;

public interface IExchangeAdapter {

    List<ITrade> getTrades(Date startDate, Date endDate, IMarket market);

}
