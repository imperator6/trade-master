package tradingmaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowRegistration;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import tradingmaster.exchange.ExchangeService;
import tradingmaster.model.IExchangeAdapter;
import tradingmaster.model.IMarket;
import tradingmaster.model.ITrade;
import tradingmaster.model.TradeBatch;

import java.util.stream.Collectors;

@Service
public class MaketWatcherService {

     @Autowired
     MessageChannel tradeChannel;

     @Autowired
     IntegrationFlowContext integrationFlowContext;

     @Autowired
     ExchangeService exchangeService;

     @Autowired
     TradeIdService tradeIdService;

     public String createMarketWatcher(final IMarket market, String exchangeName, long interval) {
          IExchangeAdapter exchange = exchangeService.getExchangyByName(exchangeName);
          return createMarketWatcher(market, exchange, interval);
     }

     public String createMarketWatcher(final IMarket market, final IExchangeAdapter exchange, long interval) {


          // new message source
          MessageSource<TradeBatch> tradeMessageSource = new AbstractMessageSource<TradeBatch>() {

               public String getComponentType() {
                    return "inbound-channel-adapter";
               }

               @Override
               protected TradeBatch doReceive() {
                    return exchange.getTrades(null, null, market);
               }
          };

          // periodic call the source and forwad to the trade channel
          IntegrationFlow myFlow = IntegrationFlows.from(tradeMessageSource, c ->
                  c.poller(Pollers.fixedRate(interval)))
                  .transform(s -> filterNewTrades((TradeBatch) s))
                  .transform(s -> filterZeroQuantityTrades((TradeBatch) s))
                  .channel(tradeChannel)
                  .get();

          // register and start the flow
          IntegrationFlowContext.IntegrationFlowRegistrationBuilder b = integrationFlowContext.registration(myFlow);
          IntegrationFlowRegistration r = b.autoStartup(true).register();


          // return id for removal  -->  integrationFlowContext.remove(id);
          return r.getId();
     }

     TradeBatch filterNewTrades(TradeBatch all) {

          Long maxTradeId = tradeIdService.getMaxTradeId(all.getExchange(), all.getMarket());
          ITrade nexMaxTrade =  all.getTrades()
                  .stream()
                  .max((t1, t2) -> Long.compare(Long.parseLong(t1.getExtId()), Long.parseLong(t2.getExtId())))
                  .orElse(null);

          all.setTrades(
           all.getTrades().stream().filter( t -> Long.parseLong(t.getExtId()) > maxTradeId  ).collect(Collectors.toList())
          );

          if(nexMaxTrade != null)
               tradeIdService.setMaxTradeId(all.getExchange(), all.getMarket(),Long.parseLong(nexMaxTrade.getExtId()) );

          return all;
     }

     TradeBatch filterZeroQuantityTrades(TradeBatch all) {
          all.setTrades(
                  all.getTrades().stream().filter( t -> t.getQuantity() > 0).collect(Collectors.toList())
          );
          return all;
     }


}
