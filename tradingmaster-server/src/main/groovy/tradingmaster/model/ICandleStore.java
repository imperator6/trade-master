package tradingmaster.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ICandleStore {

    void save(Candle c);

    void saveAll(Collection<Candle> candles);

    List<Candle> find(String period, String exchange, String market, LocalDateTime startDate, LocalDateTime endDate);

    void delete(String period, String exchange, String market, LocalDateTime startDate, LocalDateTime endDate);

}
