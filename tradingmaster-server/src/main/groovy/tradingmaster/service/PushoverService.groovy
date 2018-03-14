package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.TradeBot
import tradingmaster.pushover.Pushover
import tradingmaster.util.NumberHelper

@Service
@Commons
class PushoverService {

    @Autowired
    Pushover pushover

    @Value('${pushover.homepage}')
    String homepage

    @Async
    void send(TradeBot bot, String title, Position pos) {
        try {

            String msg = posToString(bot, pos)
            send(bot, title, msg )

        } catch(all) {
            log.error("Pushover Error!", all)
        }
    }


    @Async
    void send(TradeBot bot, String title, String message) {
        try {

            pushover.sendMessage(title, message, homepage , "TradeMaster")

        } catch(all) {
            log.error("Pushover Error!", all)
        }
    }

    String posToStringForBuy(TradeBot bot, Position pos) {

        String msg = """
buy: ${NumberHelper.formatNumber(pos.buyRate)}
quantity: ${NumberHelper.formatNumber(pos.amount)}
<i>${bot.exchange}</i>
"""

        return msg.toString()

    }

    String posToStringForSell(TradeBot bot, Position pos) {

        String color = "green"
        if(pos.result && pos.result < 0) {
            color = "red"
        }

        def lastRate = pos.sellRate
        if(lastRate == null) lastRate = pos.lastKnowRate

        String msg = """<b><font color="$color">${NumberHelper.formatNumber(pos.result)}%</font></b>
age: ${pos.age}
buy: ${NumberHelper.formatNumber(pos.buyRate)}
sell: ${NumberHelper.formatNumber(lastRate)}
diff: ${NumberHelper.formatNumber(pos.sellRate - pos.buyRate)}
<i>${bot.exchange}</i>
"""

        return msg.toString()
    }

    String posToString(TradeBot bot, Position pos) {

        String color = "green"
        if(pos.result && pos.result < 0) {
            color = "red"
        }

        def lastRate = pos.sellRate
        if(lastRate == null) lastRate = pos.lastKnowRate

        String msg = """<b><font color="$color">${NumberHelper.formatNumber(pos.result)}%</font></b>
age: ${pos.age}
buy: ${NumberHelper.formatNumber(pos.buyRate)}
last: ${NumberHelper.formatNumber(lastRate)}
<i>${bot.exchange}</i>
"""

        return msg.toString()

    }



}
