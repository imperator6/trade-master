package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tradingmaster.db.mariadb.MariaStrategyStore
import tradingmaster.model.*
import tradingmaster.service.StrategyRunnerService

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/strategy")
@Commons
class StrategyController {

    @Autowired
    MariaStrategyStore store

    @Autowired
    StrategyRunnerService strategyRunnerService


    @RequestMapping(value = "/", method = RequestMethod.GET)
    List<ScriptStrategy> list() {


        return store.loadStrategies()
    }

    @RequestMapping(value = "/saveScript", method = RequestMethod.POST)
    ScriptStrategy saveScript(@RequestBody ScriptStrategy strategy) {

        log.info("Savaing startegy ${strategy.name} with id ${strategy.id}")

        return store.saveStrategy(strategy)
    }

    @RequestMapping(value = "/runStrategy", method = RequestMethod.POST)
    StrategyRunConfig runStrategy(@RequestBody StrategyRunConfig config) {

        log.info("StrategyRunConfig ${config}")

        // Todo... persit config....

        if(config.id == null) {
            config.id = UUID.randomUUID().toString()
        }

        strategyRunnerService.startStrategy(config)

        return config  // id is now included
    }

    @RequestMapping(value = "/backtestStrategy", method = RequestMethod.POST)
    StrategyRunConfig backtestStrategy(@RequestBody StrategyRunConfig config) {

        log.info("StrategyRunConfig ${config}")

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        LocalDateTime startDate = LocalDateTime.parse(config.start, dtf)
        LocalDateTime endDate = LocalDateTime.parse(config.end, dtf)

        if(config.id == null) {
            config.id = UUID.randomUUID().toString()
        }

        strategyRunnerService.startBacktest(startDate, endDate, config)

        return config  // id is now included
    }

    @RequestMapping(value = "/backtestResults", method = RequestMethod.GET)
    RestResponse<BacktestResult> backtestResults(@RequestParam String backtestId) {

        BacktestResult result = strategyRunnerService.getBacktestResults(backtestId)

        if(result == null) {
            return new RestResponse<BacktestResult>(false, "Backtest with id ${backtestId} not found!")
        }

        return new RestResponse<BacktestResult>(result)
    }









}
