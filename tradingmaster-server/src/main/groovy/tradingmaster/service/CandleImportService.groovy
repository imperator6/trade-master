package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import tradingmaster.db.mariadb.MariaCandleStore
import tradingmaster.model.Candle
import tradingmaster.model.CandleInterval
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IHistoricDataExchangeAdapter

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
@Commons
class CandleImportService {


    @Autowired
    TaskExecutor candleImportTaskExecutor

    @Autowired
    MariaCandleStore candleStore

    void importCandles(final Date startDate, final Date endDate,final CryptoMarket market, final IHistoricDataExchangeAdapter exchange) {

        candleStore.delete("1min" /*CandleInterval.ONE_MINUTE.getKey()*/,
                market.getExchange(),
                market.getName(),
                LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.of("UTC")),
                LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.of("UTC")))

        Runnable task
        task = {

            log.info("Starting Candle import for market $market")

            Instant currentTime = startDate.toInstant()
            Instant endTime = endDate.toInstant()

            while (currentTime.getEpochSecond() < endTime.getEpochSecond()) {

                log.info("Candle import: next candles $currentTime")

                List<Candle> data = exchange.getCandles(Date.from(currentTime) , null, market, CandleInterval.ONE_MINUTE)

                data = data.collect {
                    it.period = "1min"
                    it.market = market
                    return it
                }

                candleStore.saveAll(data)

                if(!data.isEmpty())
                    currentTime = data.last().getStart().toInstant().plus(1, ChronoUnit.MINUTES)
                else {
                    log.info("No import data found for $market startDate: $currentTime")
                    currentTime = endTime //currentTime.plus(1, ChronoUnit.DAYS)
                }



                Thread.sleep(200) // slow down to avoid to many request
            }

            log.info("Candle import done!")

        } as Runnable



        candleImportTaskExecutor.execute(task)


    }
}
