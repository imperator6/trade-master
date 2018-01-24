package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tradingmaster.db.PositionRepository
import tradingmaster.db.TradeBotRepository
import tradingmaster.db.entity.MarketWatcher
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.RestResponse


@RestController
@RequestMapping("/api/bot")
@Commons
class TradeBotController {

    @Autowired
    TradeBotRepository tradeBotRepository

    @Autowired
    PositionRepository positionRepository

    @RequestMapping(value = "/", method = RequestMethod.GET)
    RestResponse<List<TradeBot>> botList() {

        List<TradeBot> list = tradeBotRepository.findAll(new Sort(Sort.Direction.DESC,"active"))

        return new RestResponse(list)
    }

    @RequestMapping(value = "/positions", method = RequestMethod.GET)
    RestResponse<List<Position>> positions(@RequestParam Integer botId) {

        List<TradeBot> list = positionRepository.findByBotId(botId)

        return new RestResponse(list)
    }
}
