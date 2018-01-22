package tradingmaster.exchange;

import tradingmaster.model.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface IExchangeAdapter {

    Boolean cancelOrder(String id);

    ExchangeResponse<String> sellLimit(String market, BigDecimal quantity, BigDecimal rate);

    ExchangeResponse<String> buyLimit(String market, BigDecimal quantity, BigDecimal rate);

    ExchangeResponse<ITicker> getTicker(String market);

    String getExchangeName();

    ExchangeResponse<IOrder> getOrder(String id);

    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market);

    List<CryptoMarket> getMakets();

    List<IBalance> getBalances();

    IBalance getBalance(String currency);

    String buildMarket(String currency, String asset);




}
