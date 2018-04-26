package tradingmaster.db.mariadb

import groovy.sql.Sql
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import tradingmaster.model.Candle
import tradingmaster.model.CryptoMarket
import tradingmaster.model.ICandleStore
import tradingmaster.model.IMarket

import javax.annotation.PostConstruct
import javax.sql.DataSource
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@Commons
class MariaCandleStore implements ICandleStore {

    private static final String TRADE_TABLE_NAME = "tm_trades"

    private static final String INSERT_QUERY = "insert into candle (period, exchange, market, start, end, open, high, low, close, volume, price, trade_count) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"


    @Autowired
    DataSource dataSource

    @PostConstruct
    init() {
        //Sql sql = new Sql(dataSource)
        // TODO: init tables if not exsist

    }


    @Override
    void save(Candle c) {

        if(c.volume == 0) return

        Sql sql = new Sql(dataSource)

        def data = [c.period,
                c.market.exchange,
                c.market.name,
                new java.sql.Timestamp(c.start.getTime()),
                new java.sql.Timestamp(c.end.getTime()),
                c.open,
                c.high,
                c.low,
                c.close,
                c.volume,
                c.volumnWeightedPrice,
                c.tradeCount]

        sql.executeInsert(INSERT_QUERY, data)
    }

    void saveAll(Collection<Candle> candles) {

        Sql sql = new Sql(dataSource)

        candles = candles.findAll { it.volume > 0 }

        sql.withBatch(200, INSERT_QUERY) { ps ->

            candles.each { c ->

                def data = [c.period,
                            c.market.exchange,
                            c.market.name,
                            new java.sql.Timestamp(c.start.getTime()),
                            new java.sql.Timestamp(c.end.getTime()),
                            c.open,
                            c.high,
                            c.low,
                            c.close,
                            c.volume,
                            c.volumnWeightedPrice,
                            c.tradeCount]

                ps.addBatch(data)
            }
        }
    }

    void delete(String period, String exchange, String market, LocalDateTime startDate, LocalDateTime endDate) {

        Sql sql = new Sql(dataSource)

        def start = Instant.now()

        exchange = exchange.capitalize()

        // TODO: query is not perfect as avg(price) is not weighted!
        // need to find onother solution to prevent double candles on startup
        String query = """delete
from candle 
where 
    start >= ? and 
    end <= ? and 
    exchange = ? and 
    market = ? and 
    period = ? 
    """

        def values = [Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), exchange, market, period]

        log.info(values)

       def deleteCount = sql.executeUpdate( query, values)

        def duration = Duration.between(start, Instant.now())

        log.info("deleteing of ${deleteCount} candles took ${duration.getSeconds()}")

    }


    List<Candle> find(String period, String exchange, String market, LocalDateTime startDate, LocalDateTime endDate) {

        Sql sql = new Sql(dataSource)

        def start = Instant.now()

        exchange = exchange.capitalize()

        IMarket crypteMarket = new CryptoMarket(exchange, market)

        List<Candle> candles = []

        // TODO: query is not perfect as avg(price) is not weighted!
        // need to find onother solution to prevent double candles on startup
        String query = """select 
     start,
     end, 
     min(open) as open,
     max(high) as high,
     min(low) as low,
     min(close) as close,
     sum(volume) as volume,
     avg(price) as price
    from candle where start >= ? and end <= ? and exchange = ? and market = ? and period = ? 
    group by 
    start, end
    order by end asc
    """

        sql.eachRow(query.toString(),  [Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), exchange, market, period]) { row ->
            Candle c = new Candle()
            c.start = new Date(row.start.getTime())
            c.end = new Date(row.start.getTime())
            c.open = row.open
            c.high = row.high
            c.low = row.low
            c.close = row.close
            c.volume = row.volume
            c.market = crypteMarket
            c.volumnWeightedPrice = row.price

            candles << c
        }

        def duration = Duration.between(start, Instant.now())

        log.info("loading ${candles.size()} candles took ${duration.getSeconds()}")

        return candles
    }
}
