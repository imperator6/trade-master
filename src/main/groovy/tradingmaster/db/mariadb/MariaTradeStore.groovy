package tradingmaster.db.mariadb

import groovy.sql.Sql
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import tradingmaster.db.ITradeStore
import tradingmaster.model.IMarket
import tradingmaster.model.TradeBatch

import javax.sql.DataSource

@Commons
class MariaTradeStore implements ITradeStore {

    private static final String TRADE_TABLE_NAME = "tm_trades"

    private static final String INSERT_TRADE_QUERY = "insert into trades (ext_id, date, market, exchange, quantity, price) VALUES(?,?,?,?,?,?)"


    @Autowired
    DataSource dataSource

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
}
