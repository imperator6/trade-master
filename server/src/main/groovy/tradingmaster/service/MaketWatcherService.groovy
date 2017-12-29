package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.context.IntegrationFlowContext
import org.springframework.integration.dsl.context.IntegrationFlowRegistration
import org.springframework.integration.dsl.core.Pollers
import org.springframework.integration.endpoint.AbstractMessageSource
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Service
import tradingmaster.exchange.ExchangeService
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.TradeBatch

@Service
@Commons
class MaketWatcherService {

     @Autowired
     MessageChannel tradeChannel

     @Autowired
     IntegrationFlowContext integrationFlowContext

     @Autowired
     ExchangeService exchangeService

     @Autowired
     TradeIdService tradeIdService

     MaketWatcherService() {
          log.info("New MaketWatcherService!")
     }

     String createMarketWatcher(final CryptoMarket market, long interval) {
          IExchangeAdapter exchange = exchangeService.getExchangyByName(market.getExchange())
          return createMarketWatcher(market, exchange, interval)
     }

     String createMarketWatcher(final CryptoMarket market, final IExchangeAdapter exchange, long interval) {


          // new message source
          MessageSource<TradeBatch> tradeMessageSource = new AbstractMessageSource<TradeBatch>() {

               boolean first = true

               String getComponentType() {
                    return "inbound-channel-adapter"
               }

               @Override
               protected synchronized TradeBatch doReceive() {

                    TradeBatch batch = exchange.getTrades(null, null, market)

                    if(first) {
                         // TODO: merge with existing trades from db if needed
                         first = false
                    }

                    return batch
               }
          }


          // periodic call the source and forward to the trade channel
          IntegrationFlow myFlow = IntegrationFlows.from(tradeMessageSource, {c ->
                  c.poller(Pollers.fixedRate(interval)) })
                  //.transform({s -> filterNewTrades((TradeBatch) s)})
                   // .transform( new AbstractTransformer() {
                   //      Object doTransform(Message<?> message) {
                   //           return filterNewTrades(message.getPayload())
                   //      }
                   // })
                  .transform(this, "filterNewTrades")
                  .transform(this, "filterZeroQuantityTrades")
                  .channel(tradeChannel)
                  .get()

          // register and start the flow
          IntegrationFlowContext.IntegrationFlowRegistrationBuilder b = integrationFlowContext.registration(myFlow)
          IntegrationFlowRegistration r = b.autoStartup(true).register()


          // return id for removal  -->  integrationFlowContext.remove(id)
          return r.getId()
     }

     TradeBatch filterNewTrades(TradeBatch all) {

          if(all && all.trades && !all.trades.isEmpty()) {

               def maxTradeId = tradeIdService.getMaxTradeId(all.getMarket())

               //log.info("maxTradeId: $maxTradeId  trades befor: ${all.trades.size()}")

               def newMaxTradeId = all.trades.max { (it.extId as Long) }?.extId as Long

               //log.info("newMaxTradeId: $newMaxTradeId")

               all.trades = all.trades.findAll { (it.extId as Long) > maxTradeId }

               //log.info("trades after: ${all.trades.size()}")

               tradeIdService.setMaxTradeId(all.getMarket(), newMaxTradeId)
          }

          return all
     }

     TradeBatch filterZeroQuantityTrades(TradeBatch all) {

          all.trades = all.trades.findAll { it.quantity > 0.0 }

          return all
     }


}
