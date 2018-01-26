package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tradingmaster.db.PositionRepository
import tradingmaster.db.TradeBotRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.model.RestResponse
import tradingmaster.service.PositionService
import tradingmaster.service.TradeBotManager

@RestController
@RequestMapping("/api/position")
@Commons
class PositionController {

    @Autowired
    TradeBotRepository tradeBotRepository

    @Autowired
    PositionRepository positionRepository

    @Autowired
    TradeBotManager tradeBotManager

    @Autowired
    PositionService positionService


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    RestResponse<List<Position>> positions(@RequestParam Integer botId) {

        List<TradeBot> list = positionRepository.findByBotId(botId)

        return new RestResponse(list)
    }

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    RestResponse<Position> sell(@RequestParam Integer botId, @RequestParam Integer positionId) {

        Position pos = positionService.findPositionById(botId, positionId)

        TradeBot bot = tradeBotManager.findBotById(botId)

        if(pos != null && bot != null) {
            positionService.closePosition(pos, (BigDecimal) null, bot)
            return new RestResponse(pos)
        }

        return new RestResponse(false, "Psosition $positionId ot TradeBot not found!")
    }

    @RequestMapping(value = "/syncPosition", method = RequestMethod.GET)
    RestResponse<Position> syncPosition(@RequestParam Integer botId, @RequestParam Integer positionId) {

        Position pos = positionService.findPositionById(botId, positionId)

        TradeBot bot = tradeBotManager.findBotById(botId)

        if(pos != null && bot != null) {
            positionService.syncPosition(pos, bot)
            return new RestResponse(pos)
        }

        return new RestResponse(false, "Position $positionId ot TradeBot not found!")
    }

    @RequestMapping(value = "/deletePos", method = RequestMethod.GET)
    RestResponse<Position> deletePosition(@RequestParam Integer botId, @RequestParam Integer positionId) {

        Position pos = positionService.findPositionById(botId, positionId)
        //TradeBot bot = tradeBotManager.findBotById(botId)

        if(pos != null) {
            positionService.deletePosition(pos)
            return new RestResponse(true)
        }

        return new RestResponse(false, "Position $positionId  not found!")
    }

    @RequestMapping(value = "/importFromExchange", method = RequestMethod.GET)
    RestResponse<Position> importFromExchange(@RequestParam Integer botId) {

        TradeBot bot = tradeBotManager.findBotById(botId)

        if(bot != null) {
            positionService.loadPositionsFromExchange(bot)
            return new RestResponse(true)
        }

        return new RestResponse(false, "TradeBot not found!")
    }


}
