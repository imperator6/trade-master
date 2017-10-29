package tradingmaster;

import groovy.util.logging.Commons;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import tradingmaster.core.CandelBuilder;
import tradingmaster.core.TradeWriter;
import tradingmaster.exchange.bittrex.Bittrex;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tradingmaster.exchange.IExchangeAdapter;
import tradingmaster.exchange.bittrex.BittrexTrade;
import tradingmaster.model.CrypoMarket;
import tradingmaster.model.ITrade;

import java.util.List;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        IExchangeAdapter bittrex = ctx.getBean(Bittrex.class);

        PublishSubscribeChannel tradeChannel = ctx.getBean("tradeChannel", PublishSubscribeChannel.class);

        tradeChannel.subscribe(new CandelBuilder());

        tradeChannel.subscribe(new TradeWriter());




       // System.exit(0);


    }


}
