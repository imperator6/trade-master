package tradingmaster.exchange.mininghamster

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Component
import tradingmaster.db.SignalRepository
import tradingmaster.db.entity.Signal
import tradingmaster.exchange.mininghamster.model.HamsterSignal


import javax.annotation.PostConstruct


@Component
@Commons
class HamsterSignalPublisher implements MessageHandler {

    @Autowired
    HamsterSignalRepository store

    @Autowired
    SignalRepository signalRepository

    @Autowired
    PublishSubscribeChannel hamsterSignalChannel

    @Autowired
    PublishSubscribeChannel signalChannel


    Date applicationStartDate = new Date()

    @PostConstruct
    void init() {
        hamsterSignalChannel.subscribe(this)
    }

    @Override
    void handleMessage(Message<?> message) throws MessagingException {

        List<HamsterSignal> signals = message.getPayload()

        Date maxDate = store.findMaxSignalDate()

        signals.each {

            if(it.exchange == null) {
                log.error("Invalid Hamster Singnal received! $it")
            }

            if(maxDate == null || it.signalDate > maxDate) {


                // now we have the id - let's publich a signals
               if(it.getSignalDate() > this.applicationStartDate && !hasASimilarSignalAlredyPublished(it)) {
                   publishHamsterSignal(it)
               }

                log.debug("Saving a new hamster signal $it")
                store.save(it)
            }
        }
    }

    boolean hasASimilarSignalAlredyPublished(HamsterSignal s) {

        // check if we had raised an signals for this market in the last 24 hours
        Date date = s.signalDate
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.HOUR, -24)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        date = cal.getTime()

        List<HamsterSignal> signals = store.findByExchangeAndMarketAndSignalDateAfter(s.getExchange(), s.market, date )

        if(signals != null && signals.size() > 0) {
            log.warn("A similar signals has already been published! This signal will be skipped. ${s}")
            return true
        }

        return false
    }

    private publishHamsterSignal(HamsterSignal hs) {

        log.info("Publishing a new hamster signal!")
        // create a new trading signal

        // parse e.G. BTC-FUN
        String asset = hs.getMarket().split("-")[1]

        Signal s = new Signal()

        s.setAsset(asset)
        s.setSignalDate(new Date())
        s.setPrice(hs.getLastprice())
        s.setBuySell("buy")
        s.setExtSignalId( hs.getId() )
        s.setExchange( hs.getExchange() )

        s.setTriggerName("Mining Hamster")
        s.setTriggerValues("signalmode=${hs.signalmode}")

        // save the signal
        signalRepository.save(s)

        signalChannel.send( MessageBuilder.withPayload(s).build() )
    }
}
