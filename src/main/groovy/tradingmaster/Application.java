package tradingmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tradingmaster.model.CrypoMarket;
import tradingmaster.service.MaketWatcherService;


@SpringBootApplication
public class Application {


    public static void main(String[] args) {


        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        MaketWatcherService maketWatcherService = ctx.getBean(MaketWatcherService.class);


        maketWatcherService.createMarketWatcher(new CrypoMarket("USDT", "NEO"), "Bittrex", 10000 );

        //maketWatcherService.createMarketWatcher(new CrypoMarket("USDT", "ETH"), "Bittrex", 10000 );


       /* PublishSubscribeChannel tradeChannel = ctx.getBean("tradeChannel", PublishSubscribeChannel.class);

        tradeChannel.subscribe(new CandelBuilder());

        tradeChannel.subscribe(new TradeWriter()); */




       // System.exit(0);


    }


}
