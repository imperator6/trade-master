package tradingmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tradingmaster.model.CryptoMarket;
import tradingmaster.service.MaketWatcherService;


@SpringBootApplication
public class Application {


    public static void main(String[] args) {


        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        MaketWatcherService maketWatcherService = ctx.getBean(MaketWatcherService.class);

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "NEO"), 10000 );

        maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "ETH"),  10000 );



       /* PublishSubscribeChannel tradeChannel = ctx.getBean("tradeChannel", PublishSubscribeChannel.class);

        tradeChannel.subscribe(new CandleBuilder());

        tradeChannel.subscribe(new TradeWriter()); */




       // System.exit(0);


    }


}
