package tradingmaster.exchange.mininghamster

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.context.IntegrationFlowContext
import org.springframework.integration.dsl.context.IntegrationFlowRegistration
import org.springframework.integration.dsl.core.Pollers
import org.springframework.integration.endpoint.AbstractMessageSource
import org.springframework.stereotype.Service
import tradingmaster.exchange.mininghamster.MiningHamster
import tradingmaster.exchange.mininghamster.model.HamsterSignal

import javax.annotation.PostConstruct

@Service
@Commons
class HamsterWatcherService {


     @Autowired
     MiningHamster miningHamster

     @Autowired
     PublishSubscribeChannel hamsterSignalChannel

     @Autowired
     IntegrationFlowContext integrationFlowContext

     Long intervalMillis

     @Autowired
     HamsterWatcherService(@Value('${mininghamster.pollInterval}') Long intervalMillis) {
          this.intervalMillis =  intervalMillis
     }


     void startHamsterWatcher() {

          log.info("Creating a Hamster Watcher")

          // new message source
          MessageSource<List<HamsterSignal>> hamsterSource = new AbstractMessageSource<List<HamsterSignal>>() {


               String getComponentType() {
                    return "inbound-channel-adapter"
               }

               @Override
               protected synchronized List<HamsterSignal> doReceive() {
                    return miningHamster.getLatestSignals()
               }
          }

          // periodic call the source and forward to the trade channel
          IntegrationFlow myFlow = IntegrationFlows.from(hamsterSource, {c ->
                  c.poller(Pollers.fixedRate(intervalMillis)) })
                  .channel(hamsterSignalChannel)
                  .get()

          // register and start the flow
          IntegrationFlowContext.IntegrationFlowRegistrationBuilder b = integrationFlowContext.registration(myFlow)
          IntegrationFlowRegistration r = b.autoStartup(true).register()
     }




}
