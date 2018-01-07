package tradingmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tradingmaster.db.MarketWatcherRepository;
import tradingmaster.model.CryptoMarket;
import tradingmaster.service.MaketWatcherService;
import tradingmaster.service.StrategyRunnerService;


@SpringBootApplication
public class Application {


    public static void main(String[] args) throws Exception {


        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        MaketWatcherService maketWatcherService = ctx.getBean(MaketWatcherService.class);

        //StrategyRunnerService strategyRunnerService = ctx.getBean(StrategyRunnerService.class);

        MarketWatcherRepository marketWatcherRepository = ctx.getBean(MarketWatcherRepository.class);

        marketWatcherRepository.findByActive(true).stream().forEach( w -> {

            CryptoMarket market = new CryptoMarket(w.getExchange(), w.getMarket());
            w.setActive(false);
            w.setIntegrationFlowId(null);

            maketWatcherService.createMarketWatcher( market, w.getIntervalMillis() );

        });

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "NEO"), 10000 );

      //  maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "ETH"),  10000 );

       // maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","BTC", "ETH"),  10000 );

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Gdax","USD", "BTC"),  10000 );

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Gdax","USD", "ETH"),  10000 );

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Gdax","BTC", "ETH"),  10000 );

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "BTC"),  10000 );

        //maketWatcherService.createMarketWatcher(new CryptoMarket("Bittrex","USDT", "LTC"),  10000 );






       /* PublishSubscribeChannel tradeChannel = ctx.getBean("tradeChannel", PublishSubscribeChannel.class);

        tradeChannel.subscribe(new CandleBuilder());

        tradeChannel.subscribe(new TradeWriter()); */




       // System.exit(0);


    }


}
