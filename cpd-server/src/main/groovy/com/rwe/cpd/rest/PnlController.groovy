package com.rwe.cpd.rest

import groovy.json.JsonBuilder
import groovy.sql.Sql
import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.sql.DataSource

@RestController
@RequestMapping("/api/pnl")
@Commons
class PnlController {

    @Autowired
    DataSource dataSource

    Map cache = [:]


    @RequestMapping(value = "/all", method = RequestMethod.GET)
    String getLatestOrderbook() {

        Sql sql = new Sql(dataSource)

        def snapshotId = 112855

        if(cache.containsKey(snapshotId))
            return cache.get(snapshotId)

        def res = []

        sql.eachRow("""select 
     DESK_NAME
     ,SNAPSHOT_DATE
     ,SNAPSHOT_ID
    ,DEAL_TRACKING_NUMBER
    ,INST_TYPE_NAME
    ,INDEX_NAME 
    ,PORTFOLIO_NAME
    ,STRATEGY_NAME
    ,TICKER
    ,IS_NEW
    ,TRADE_DATE
    ,TRADE_PRICE
    ,RANGE
    ,END_DATE
    ,POSITION
    ,MARKET_PRICE
    ,PREV_MAKET_PRICE as PREV_MARKET_PRICE
    ,FX_RATE
    ,PREV_FX_RATE
     , DTD
    ,YTD
      ,STRAT_DTD
     ,STRAT_YTD
 from 
    V_DEAL_VAL_ALL_DETAILS
where
    snapshot_id = ${snapshotId} and is_only_in_EOY = 0
    and ABS((DISC_MTM + REAL_CASH - EOY_DISC_MTM)) != 0""") {
            res << new JsonBuilder( it.toRowResult() ).toPrettyString()
        }

        cache.put(snapshotId, res)

        return res
    }
}
