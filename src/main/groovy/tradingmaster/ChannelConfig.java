package tradingmaster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.channel.MessageChannels;
import tradingmaster.core.TradeWriter;

@Configuration
@EnableIntegration
public class ChannelConfig {


    @Bean
    public PublishSubscribeChannel tradeChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public TradeWriter tradeWriter() {
        TradeWriter tw = new TradeWriter();
        tradeChannel().subscribe(tw);
        return tw;
    }


}
