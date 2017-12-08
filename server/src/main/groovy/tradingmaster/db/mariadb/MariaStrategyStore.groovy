package tradingmaster.db.mariadb

import groovy.sql.Sql
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import tradingmaster.model.*

import javax.annotation.PostConstruct
import javax.sql.DataSource
import java.time.Duration
import java.time.Instant

@Commons
class MariaStrategyStore implements IStrategyStore {

    @Autowired
    DataSource dataSource

    @PostConstruct
    init() {
        //Sql sql = new Sql(dataSource)
        // TODO: init tables if not exsist

    }

    List<IStrategy> loadStrategies() {

        Sql sql = new Sql(dataSource)

        def start = Instant.now()

        List<IStrategy> strategies = []

        String query = """SELECT * from strategy s1
WHERE s1.version = 
  (SELECT max(version) FROM strategy s2 WHERE lower(s2.name) = lower(s1.name))
"""
        sql.eachRow(query.toString()) { row ->
            IStrategy s = new Strategy()
            s.id = row.id
            s.name = row.name
            s.language = row.language
            s.script = row.script
            strategies << s
        }

        def duration = Duration.between(start, Instant.now())

        log.debug("loading ${strategies.size()} strategies took ${duration.getSeconds()}")

        return strategies
    }
}
