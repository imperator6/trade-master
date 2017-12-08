package tradingmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import tradingmaster.model.CryptoMarket;
import tradingmaster.model.PaperPortfolio;
import tradingmaster.model.Strategy;
import tradingmaster.service.MaketWatcherService;
import tradingmaster.service.StrategyRunnerService;

import java.nio.charset.Charset;


@SpringBootApplication
public class Application {


    public static void main(String[] args) throws Exception {


        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        MaketWatcherService maketWatcherService = ctx.getBean(MaketWatcherService.class);

        StrategyRunnerService strategyRunnerService = ctx.getBean(StrategyRunnerService.class);

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "NEO"), 10000 );

       // maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "ETH"),  10000 );

        maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "BTC"),  10000 );


        String script = StreamUtils.copyToString( new ClassPathResource("/strategy/dema.js").getInputStream(), Charset.defaultCharset() );


        strategyRunnerService.startStrategy(new CryptoMarket("Bittrex","USDT", "BTC"), new PaperPortfolio(), new Strategy( script, "javascript"));






       /* PublishSubscribeChannel tradeChannel = ctx.getBean("tradeChannel", PublishSubscribeChannel.class);

        tradeChannel.subscribe(new CandleBuilder());

        tradeChannel.subscribe(new TradeWriter()); */




       // System.exit(0);


    }


}
