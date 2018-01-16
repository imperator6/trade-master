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
class MariaStrategyStore {

    static SELECT_MAX_VERSION_QUERY =  """SELECT * from strategy s1
WHERE s1.version = 
  (SELECT max(version) FROM strategy s2 WHERE lower(s2.name) = lower(s1.name))
"""
    static UPDATE_QUERY = "update strategy set name = ?, language = ?, script=?, version=? where id = ?"

    static INSERT_QUERY =  """insert into strategy(name, language, script, version) VALUES(?,?,?,?)"""


    @Autowired
    DataSource dataSource

    @PostConstruct
    init() {
        //Sql sql = new Sql(dataSource)
        // TODO: init tables if not exsist

    }

    List<IScriptStrategy> loadStrategies() {

        Sql sql = new Sql(dataSource)

        def start = Instant.now()

        List<IScriptStrategy> strategies = []


        sql.eachRow(SELECT_MAX_VERSION_QUERY.toString()) { row ->
            strategies << rowToStrategy(row)
        }

        def duration = Duration.between(start, Instant.now())

        log.debug("loading ${strategies.size()} strategies took ${duration.getSeconds()}")

        return strategies
    }

    IScriptStrategy saveStrategy(ScriptStrategy s) {

        Sql sql = new Sql(dataSource)

        if(s.id == null) {
            // new strategy
            def params = [s.name, s.language, s.script, s.version++]
            def res = sql.executeInsert(INSERT_QUERY.toString(), params)

            log.info(res)

            return null

        } else {
            // update existing
            def nextVersion = s.version + 1
            def params = [s.name, s.language, s.script, nextVersion, s.id]
            sql.execute(UPDATE_QUERY.toString(), params)

            return loadStrategyById(s.id, null)
        }
    }

    IScriptStrategy loadStrategyById(Number id, Number version) {

        Sql sql = new Sql(dataSource)

        def query = SELECT_MAX_VERSION_QUERY + " and s1.id = ?"
        def params = [id]

        if(version != null) {
            query += " and s1.version = ?"
            params.push(version)
        }

        def row = sql.firstRow( query.toString(), params)

        return  rowToStrategy(row)
    }

    IScriptStrategy rowToStrategy(row) {

        IScriptStrategy s = new ScriptStrategy()
        s.id = row.id
        s.name = row.name
        s.language = row.language
        s.script = row.script
        s.version = row.version

        return s

    }
}
