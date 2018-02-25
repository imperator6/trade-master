package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot

@Service
@Commons
class AlertService {

    @Autowired
    PushoverService pushoverService


    void checkAlert(TradeBot bot, Position pos) {


        if(pos.settings.alerts) {

            pos.settings.alerts.each {

                if(it.enabled) {

                    if(it.value > 0 && pos.result >= it.value) {

                        pushoverService.send(bot,
                                "${pos.market} >= ${it.value}%",
                                 pos)

                        it.enabled = false // deactivate the alert
                    } else if(it.value < 0 && pos.result <= it.value) {

                        pushoverService.send(bot,
                                "${pos.market} <= ${it.value}%",
                                pos)


                        it.enabled = false // deactivate the alert
                    }
                }
            }

        }

    }

}
