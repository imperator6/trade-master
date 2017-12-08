package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import tradingmaster.model.IStrategyStore
import tradingmaster.model.Strategy

@RestController
@RequestMapping("/api/strategy")
@Commons
class StrategyController {

    @Autowired
    IStrategyStore store


    @RequestMapping(value = "/", method = RequestMethod.GET)
    List<Strategy> list() {


        return store.loadStrategies()
    }

    @RequestMapping(value = "/saveScript", method = RequestMethod.POST)
    Strategy saveScript(@RequestBody Strategy strategy) {

        log.info(strategy)

        return strategy
    }


}
