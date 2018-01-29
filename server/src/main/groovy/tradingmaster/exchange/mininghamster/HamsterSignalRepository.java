package tradingmaster.exchange.mininghamster;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import tradingmaster.exchange.mininghamster.model.HamsterSignal;

import java.util.Date;
import java.util.List;

@Component
public interface HamsterSignalRepository extends PagingAndSortingRepository<HamsterSignal, Integer> {

    List<HamsterSignal> findBySignalDate(Date date);

    @Query("select max(signalDate) from HamsterSignal")
    Date findMaxSignalDate();

    List<HamsterSignal> findByExchangeAndMarketAndSignalDateAfter(String exchange, String market, Date date);


}
