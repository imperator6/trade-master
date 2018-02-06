package tradingmaster.db.mariadb

import groovy.sql.Sql
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import tradingmaster.model.*

import javax.annotation.PostConstruct
import javax.sql.DataSource
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@Commons
class MariaTradeStore implements ITradeStore {

    private static final String TRADE_TABLE_NAME = "tm_trades"

    private static final String INSERT_TRADE_QUERY = "insert into trades (ext_id, date, market, exchange, quantity, price) VALUES(?,?,?,?,?,?)"


    @Autowired
    DataSource dataSource

    @PostConstruct
    init() {
        //Sql sql = new Sql(dataSource)
        // TODO: init tables if not exsist

    }

    @Override
    Long getMaxTradeId(IMarket market) {

        Sql sql = new Sql(dataSource)
        def max = sql.firstRow("select max(ext_id) as MAX_EXT_ID from trades where market = ? and exchange = ?" , [market.name, market.exchange]).MAX_EXT_ID

        return max as Long
    }

    @Override
    void persistTrades(TradeBatch batch) {

        Sql sql = new Sql(dataSource)

        sql.withBatch(200, INSERT_TRADE_QUERY) { ps ->

            batch.trades.each {

                def data = [it.extId,
                        new java.sql.Timestamp(it.date.getTime()),
                        batch.market.name,
                        batch.market.exchange,
                        it.quantity,
                        it.price]

                ps.addBatch(data)
            }
        }
    }

    TradeBatch loadTrades(String exchange, String market, LocalDateTime startDate, LocalDateTime endDate) {

        Sql sql = new Sql(dataSource)

        def start = Instant.now()

        exchange = exchange.capitalize()

        List<ITrade> trades = []

        String query = "select * from trades where date between ? and ? and exchange = ? and market = ?"

        sql.eachRow(query.toString(),  [Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), exchange, market]) { row ->
            ITrade t = new CryptoTrade()
            t.date = new Date(row.date.getTime())
            t.extId = row.ext_id
            t.price = row.price
            t.quantity = row.quantity
            trades << t
        }

        TradeBatch tb = new TradeBatch()
        tb.setMarket(new CryptoMarket(exchange, market))
        tb.setTrades(trades)

        def duration = Duration.between(start, Instant.now())

        log.info("loading ${trades.size()} trades took ${duration.getSeconds()}")

        return tb
    }
}
