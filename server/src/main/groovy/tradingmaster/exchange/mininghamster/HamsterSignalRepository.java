package tradingmaster.exchange.mininghamster;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import tradingmaster.db.entity.Signal;
import tradingmaster.db.entity.TradeBot;
import tradingmaster.exchange.mininghamster.model.HamsterSignal;

import java.util.Date;
import java.util.List;

@Component
public interface HamsterSignalRepository extends PagingAndSortingRepository<HamsterSignal, Integer> {

    List<HamsterSignal> findBySignalDate(Date date);

    @Query("select max(signalDate) from HamsterSignal")
    Date findMaxSignalDate();


}
