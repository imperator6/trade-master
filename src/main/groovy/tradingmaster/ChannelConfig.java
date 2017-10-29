package tradingmaster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.endpoint.MethodInvokingMessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tradingmaster.exchange.bittrex.Bittrex;
import tradingmaster.model.CrypoMarket;
import tradingmaster.model.IMarket;
import tradingmaster.model.ITrade;

import java.util.List;

@Configuration
@EnableIntegration
public class ChannelConfig {

    @Autowired
    Bittrex api;


    @Bean
    public IMarket getMarket() {
        return new CrypoMarket("USDT", "NEO");
    }

    @Bean
    public MessageSource<?> tradeMessageSource() {
       return new AbstractMessageSource<List<ITrade>>() {

           public String getComponentType() {
               return "inbound-channel-adapter";
           }

           @Override
           protected List<ITrade> doReceive() {
               return api.getTrades(null, null, getMarket());
           }
       };
    }


    @Bean
    public MessageChannel tradeChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public IntegrationFlow tradeFlow() {
        return IntegrationFlows.from(this.tradeMessageSource(), c ->
                c.poller(Pollers.fixedRate(10000)))
                .channel(this.tradeChannel())
                //.publishSubscribeChannel(c -> c.subscribe( s -> s.handle( m -> { System.out.println(m.getPayload());})))
                .get();
    }









}
