package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Sort
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tradingmaster.db.MarketWatcherRepository
import tradingmaster.db.entity.MarketWatcher
import tradingmaster.model.CryptoMarket
import tradingmaster.model.RestResponse
import tradingmaster.service.MarketWatcherService

@RestController
@RequestMapping("/api/marketWatcher")
@Commons
class MarketWatcherController {

    @Autowired
    ApplicationContext ctx

    @Autowired
    MarketWatcherRepository marketWatcherRepository

    @Autowired
    MarketWatcherService marketWatcherService


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    RestResponse<List<MarketWatcher>> watcherList() {

        List<MarketWatcher> list = marketWatcherRepository.findAll(new Sort(Sort.Direction.DESC,"active"))

        return new RestResponse(list)
    }

    @Secured("TRADER") // TODO...  Secured dosent work !
    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    RestResponse<MarketWatcher> stopWacther(@RequestParam Integer watcherId) {

        MarketWatcher  w = marketWatcherService.stopMarketWatcher(watcherId)

        return new RestResponse(w)
    }


    @RequestMapping(value = "/start", method = RequestMethod.GET)
    RestResponse<MarketWatcher> startWacther(@RequestParam String exchange, @RequestParam String market) {

        MarketWatcher  w = marketWatcherService.createMarketWatcher(new CryptoMarket(exchange, market))


        return new RestResponse(w)
    }
}
