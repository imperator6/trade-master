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

    }

}
