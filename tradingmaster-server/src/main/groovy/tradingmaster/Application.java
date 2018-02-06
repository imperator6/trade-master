package tradingmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tradingmaster.db.MarketWatcherRepository;
import tradingmaster.exchange.mininghamster.HamsterWatcherService;
import tradingmaster.model.CryptoMarket;
import tradingmaster.service.MarketWatcherService;
import tradingmaster.service.TradeBotManager;


@SpringBootApplication
public class Application {


    public static void main(String[] args) throws Exception {

        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        // start all Bots
        TradeBotManager botManager = ctx.getBean(TradeBotManager.class);
        botManager.startBots();

        // start market watcher
        MarketWatcherService maketWatcherService = ctx.getBean(MarketWatcherService.class);

        MarketWatcherRepository marketWatcherRepository = ctx.getBean(MarketWatcherRepository.class);

        marketWatcherRepository.findByActive(true).stream().forEach( w -> {

            CryptoMarket market = new CryptoMarket(w.getExchange(), w.getMarket());
            w.setActive(false);
            w.setIntegrationFlowId(null);
            marketWatcherRepository.save(w);

            maketWatcherService.createMarketWatcher( market, w.getIntervalMillis() );

        });


        // start Hamster Watcher
        HamsterWatcherService hamsterWatcherService = ctx.getBean(HamsterWatcherService.class);
        hamsterWatcherService.startHamsterWatcher();


    }


}
