package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.ResourceLoader
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import tradingmaster.model.Candle
import tradingmaster.model.IMarket
import tradingmaster.model.IPortfolio
import tradingmaster.model.IStrategyScript
import tradingmaster.model.StrategyConfig

import javax.annotation.PostConstruct
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import java.nio.charset.Charset

@Service
@Commons
class StrategyRunnerService implements  MessageHandler {

    Map<String, StrategyConfig> strategyMap = [:]

    @Autowired
    ResourceLoader resourceLoader

    @Autowired
    PublishSubscribeChannel candelChannel1Minute

    @PostConstruct
    init() {
        candelChannel1Minute.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        Candle c = message.getPayload()


        Map strategies = new HashMap(this.strategyMap)

        def matchingStrategies = strategies.values().findAll { StrategyConfig s ->
            s.getMarket().equals( c.getMarket() )
        }

        log.info("Found ${matchingStrategies.size()} matching strategies!")



        matchingStrategies.each { StrategyConfig s ->

            s.scriptHandler.getBindings(ScriptContext.ENGINE_SCOPE).put("market", s.getMarket())

            Invocable invocable = (Invocable) s.scriptHandler

            Object result  = invocable.invokeFunction("fun1", "Peter Parker")
            System.out.println(result)
        }

    }


    String startStrategy(IMarket market, IPortfolio portfolio, IStrategyScript script) {

        log.info("Starting a new Strategy!")

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn")
        engine.eval(script.getScript())

        StrategyConfig strategy = new StrategyConfig(market, portfolio, script, engine)

        String id = "1"

        strategyMap.put(id, strategy)

    }


}
