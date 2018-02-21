package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tradingmaster.db.PositionRepository
import tradingmaster.db.TradeBotRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.db.entity.json.PositionSettings
import tradingmaster.exchange.ExchangeService
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.model.CryptoMarket
import tradingmaster.model.ITicker
import com.rwe.platform.rest.RestResponse
import tradingmaster.service.MarketWatcherService
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

    @Autowired
    MarketWatcherService marketWatcherService

    @Autowired
    ExchangeService exchangeService


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

    @RequestMapping(value = "/applySettings", method = RequestMethod.POST)
    RestResponse<Position> applySettings(@RequestParam Integer botId, @RequestParam Integer positionId, @RequestBody PositionSettings settings) {

        log.info(settings)
        Position pos = positionService.findPositionById(botId, positionId)

        TradeBot bot = tradeBotManager.findBotById(botId)

        if(pos != null && bot != null) {

            // reset an prev. stored trailing stop loss value
            if(pos.settings.trailingStopLoss.enabled != settings.trailingStopLoss.enabled) {
                pos.setTrailingStopLoss(null)
            }

            pos.settings = settings
            positionRepository.save(pos)

            if(settings.traceClosedPosition) {
                marketWatcherService.createMarketWatcher(new CryptoMarket(bot.exchange, pos.getMarket()))
            }

            return new RestResponse(true)
        }

        return new RestResponse(false, "TradeBot not found!")
    }

    @RequestMapping(value = "/newPosition", method = RequestMethod.POST)
    RestResponse<Position> newPosition(@RequestParam Integer botId, @RequestParam String market, @RequestBody PositionSettings settings) {

        log.info("New position: $botId $market $settings")
        // find bot.
        TradeBot bot = tradeBotManager.findBotById(botId)
        if(bot == null) {
            return new RestResponse(false, "No active tradebot for id $botId")
        }

        try
        {
           Position pos = positionService.newPosition(bot, market, settings)

            return new RestResponse(pos)

        } catch(Exception e) {
            log.error(e)
            return new RestResponse(false, e.getMessage())
        }
    }

    @RequestMapping(value = "/ticker", method = RequestMethod.GET)
    RestResponse<ITicker> getTicker(@RequestParam String exchangeName, @RequestParam String market) {

        try
        {
            IExchangeAdapter exchange = exchangeService.getExchangyByName(exchangeName)
            ITicker ticker = exchange.getTicker(market).getResult()

            Map res = [:]
            res.put("bidPrice", ticker.getBid())
            res.put("askPrice", ticker.getAsk())

            return new RestResponse(res)

        } catch(Exception e) {
            return new RestResponse(false, e.getMessage())
        }
    }


}
