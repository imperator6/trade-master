package tradingmaster.db.couchdb

import org.springframework.beans.factory.annotation.Autowired
import tradingmaster.model.ITradeStore
import tradingmaster.model.IMarket
import tradingmaster.model.TradeBatch
import javax.annotation.PostConstruct
import java.time.LocalDateTime


@groovy.util.logging.Commons
class CouchDBTradeStore implements ITradeStore {

    private static final String TRADE_TABLE_NAME = "tm_trades"

    @Autowired
    CouchDBClient couch

    @PostConstruct
    void init() {
        try {
            if(!couch.containsDb(TRADE_TABLE_NAME)) {
                couch.createDb(TRADE_TABLE_NAME)
            }
        } catch (all) {
            log.error("Error while creation db ${TRADE_TABLE_NAME}", all)
        }
    }

    @Override
    Long getMaxTradeId(IMarket marke) {



        return null
    }

    @Override
    void persistTrades(TradeBatch batch) {

        if(batch.trades) {

            def trades = batch.trades.collect() {

                [
                    extId: it.extId,
                    quantity: it.quantity,
                    date:     it.date,
                    price:  it.price,
                    market: batch.market.name,
                    exchange: batch.market.exchange
                ]

            }

            couch.updateBulk(TRADE_TABLE_NAME, trades)
        }
    }

    @Override
    TradeBatch loadTrades(String exchange, String market, LocalDateTime startDate, LocalDateTime endDate) {
        return null
    }
}
