package tradingmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import tradingmaster.db.MarketWatcherRepository;
import tradingmaster.model.CryptoMarket;
import tradingmaster.service.MaketWatcherService;


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


    }


}
