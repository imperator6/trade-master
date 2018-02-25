package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tradingmaster.db.entity.TradeBot

@Service
@Commons
class ResetMinMaxService {

    @Autowired
    TradeBotManager tradeBotManager

    PositionUpdateHandler updateHandler

    @Scheduled(cron = "0 0 0 * * ?") // ever day at midnight
    void resetMinMaxValues() {

        log.info("Resetting min max values")

        tradeBotManager.getActiveBots().each { TradeBot bot ->

            bot.getPositions().each {

                if(!it.closed && !it.sellInPogress && !it.buyInPogress) {

                    Integer age = updateHandler.getAgeInHours(it)

                    if(age >= 23) {
                        log.info("Resetting min max values for pos ${it.getId()}")
                        it.maxResult = null
                        it.minResult = null
                    }
                }
            }
        }

    }

}
