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
import tradingmaster.db.MarketWatcherRepository
import tradingmaster.exchange.ExchangeService
import tradingmaster.model.CryptoMarket
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.MarketWatcher
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

     @Autowired
     MarketWatcherRepository marketWatcherRepository


     MaketWatcherService() {
          log.info("New MaketWatcherService!")
     }

     MarketWatcher createMarketWatcher(final CryptoMarket market, long interval) {

          MarketWatcher w = marketWatcherRepository.findByExchangeAndMarket(market.getExchange(), market.getName())

          if(w == null) {
               w  = marketWatcherRepository.save( new MarketWatcher(market.getExchange(), market.getName()))
          }

          w.setIntervalMillis(interval)

          IExchangeAdapter exchange = exchangeService.getExchangyByName(market.getExchange())

          return createMarketWatcher(w, market, exchange)
     }

     MarketWatcher stopMarketWatcher(Integer id) {

          log.info("Stopping MarketWatcher with id ${id}")

          MarketWatcher w = marketWatcherRepository.findOne(id)

          if(w != null && w.isActive()) {
               integrationFlowContext.remove( w.getIntegrationFlowId())
               w.setIntegrationFlowId(null)
               w.setActive(false)
          }

          return marketWatcherRepository.save(w)
     }

     MarketWatcher createMarketWatcher(final MarketWatcher w, final CryptoMarket market, final IExchangeAdapter exchange) {

          log.info("Creating a new MarketWatcher on exchange ${market.getExchange()} for market: $market")

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
                  c.poller(Pollers.fixedRate(w.getIntervalMillis())) })
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

          w.setActive(true)
          w.setStartDate(new Date())

          w.setIntegrationFlowId(r.getId())
          marketWatcherRepository.save(w)

          // return id for removal  -->  integrationFlowContext.remove(id)
          return w
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
