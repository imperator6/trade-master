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
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.RestResponse
import tradingmaster.service.TradeBotManager

@RestController
@RequestMapping("/api/bot")
@Commons
class TradeBotController {

    @Autowired
    TradeBotRepository tradeBotRepository

    @Autowired
    PositionRepository positionRepository

    @Autowired
    TradeBotManager tradeBotManager

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

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    RestResponse<Position> sell(@RequestParam Integer botId, @RequestParam Integer positionId) {

        Position pos = tradeBotManager.findPositionById(botId, positionId)

        TradeBot bot = tradeBotManager.findBotById(botId)

        if(pos != null && bot != null) {
            tradeBotManager.closePosition(pos, null, bot)
            return new RestResponse(pos)
        }

        return new RestResponse(false, "Psosition $positionId ot TradeBot not found!")
    }

    @RequestMapping(value = "/syncPosition", method = RequestMethod.GET)
    RestResponse<Position> syncPosition(@RequestParam Integer botId, @RequestParam Integer positionId) {

        Position pos = tradeBotManager.findPositionById(botId, positionId)

        TradeBot bot = tradeBotManager.findBotById(botId)

        if(pos != null && bot != null) {
            tradeBotManager.syncPosition(pos, bot)
            return new RestResponse(pos)
        }

        return new RestResponse(false, "Psosition $positionId ot TradeBot not found!")
    }
}
