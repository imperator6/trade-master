package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tradingmaster.db.entity.TradeBot
import tradingmaster.pushover.Pushover

@Service
@Commons
class PushoverService {

    @Autowired
    Pushover pushover

    @Value('${pushover.homepage}')
    String homepage


    void send(TradeBot bot, String title, String message) {
        try {

            pushover.sendMessage(title, message, homepage , "TradeMaster")

        } catch(all) {
            log.error("Pushover Error!", all)
        }
    }


}
