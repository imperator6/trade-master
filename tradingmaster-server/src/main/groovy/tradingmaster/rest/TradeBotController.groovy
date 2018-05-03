package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tradingmaster.db.PositionRepository
import tradingmaster.db.TradeBotRepository
import tradingmaster.db.entity.TradeBot
import com.rwe.platform.rest.RestResponse
import tradingmaster.db.entity.json.Config
import tradingmaster.model.ScriptStrategy
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
    RestResponse<Collection<TradeBot>> botList() {

        Collection<TradeBot> list =  tradeBotManager.TRADE_BOT_MAP.values() //tradeBotRepository.findAll(new Sort(Sort.Direction.DESC,"active"))

        return new RestResponse(list)
    }

    @RequestMapping(value = "/saveConfig", method = RequestMethod.POST)
    RestResponse<TradeBot> saveConfig(@RequestBody TradeBot bot) {

        TradeBot oldBot = tradeBotManager.findBotById(bot.id)
        if(oldBot != null) {
            oldBot.setConfig(bot.config)
            tradeBotRepository.save(oldBot)
            tradeBotManager.refreshBotConfig(oldBot)
            return new RestResponse(oldBot)
        }


        return new RestResponse(false,"Can't find bot with id ${bot.id}!")
    }

    @RequestMapping(value = "/clone", method = RequestMethod.POST)
    RestResponse<TradeBot> cloneBot(@RequestBody TradeBot bot) {

        saveConfig(bot)

        TradeBot oldBot = tradeBotManager.findBotById(bot.id)
        if(oldBot != null) {
            TradeBot clone = tradeBotManager.cloneBot( oldBot )
            return new RestResponse(clone)
        }

        return new RestResponse(false,"Can't find bot with id ${bot.id}!")
    }

    @RequestMapping(value = "/syncBalance", method = RequestMethod.GET)
    RestResponse<Collection<TradeBot>> syncBalance(@RequestParam Integer botId) {

        TradeBot bot = tradeBotManager.findBotById(botId)
        if(bot != null) {
            tradeBotManager.syncBanlance(bot)
            return new RestResponse(bot)
        }

        return new RestResponse(false, "Bot with id $botId not found.")


    }








}
