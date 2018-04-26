package tradingmaster.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tradingmaster.core.CandleAggregator;
import tradingmaster.core.CandleBuilder;
import tradingmaster.core.CandleWriter;
import tradingmaster.core.TradeWriter;
import tradingmaster.exchange.bittrex.BittrexApi11;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@EnableIntegration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.rwe.platform.*", "tradingmaster.*"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.rwe.platform.*", "tradingmaster.*"})
@EntityScan(basePackages = {"com.rwe.platform.db.*", "tradingmaster.db.*" , "tradingmaster.exchange.mininghamster.model"})
public class Config {

    @Autowired
    private Environment env;


    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC")); // let's use UTC by default
    }


    @Bean
    TaskExecutor backtestTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(false);
        return pool;
    }

    @Bean
    TaskExecutor candleImportTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    TaskExecutor signalExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(50);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    TaskExecutor orderTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(25);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    TaskExecutor positionTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(25);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }


    @Bean
    BittrexApi11 bittrexApi11() {
        return new BittrexApi11(env.getProperty("bittrex.key"), env.getProperty("bittrex.secret"), 1, 15);
    }


    @Bean
    public PublishSubscribeChannel tradeChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel candelChannel1Minute() {
        return MessageChannels.publishSubscribe().get();
    }

    // only candles > server startTime
    @Bean
    public PublishSubscribeChannel lastRecentCandelChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel mixedCandelSizesChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel signalChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel executedSignalChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel backtestChannel() { return MessageChannels.publishSubscribe().get(); }

    @Bean
    public PublishSubscribeChannel positionUpdateChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel fxDollarChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel hamsterSignalChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    CandleAggregator candelAggregator5Minutes(PublishSubscribeChannel mixedCandelSizesChannel) {
        CandleAggregator ca = new CandleAggregator(5, mixedCandelSizesChannel);
        lastRecentCandelChannel().subscribe(ca);
        return ca;
    }

    @Bean
    CandleAggregator candelAggregator15Minutes(PublishSubscribeChannel mixedCandelSizesChannel) {
        CandleAggregator ca = new CandleAggregator(15, mixedCandelSizesChannel);
        lastRecentCandelChannel().subscribe(ca);
        return ca;
    }

    @Bean
    CandleAggregator candelAggregator30Minutes(PublishSubscribeChannel mixedCandelSizesChannel) {
        CandleAggregator ca = new CandleAggregator(30, mixedCandelSizesChannel);
        lastRecentCandelChannel().subscribe(ca);
        return ca;
    }


    @Bean
    public TradeWriter tradeWriter() {
        TradeWriter tw = new TradeWriter();
        // TODO: add property to active trade writing
        if(false)
           tradeChannel().subscribe(tw);
        return tw;
    }

    @Bean
    public CandleWriter candleWriter() {
        CandleWriter cw = new CandleWriter();
        return cw;
    }

    @Bean
    public CandleBuilder candelBuilder() {
        CandleBuilder cb = new CandleBuilder();
        tradeChannel().subscribe(cb);
        return cb;
    }

    /*
    @Bean
    CouchDBClient couchDBClient() {
        return new CouchDBClient(
                env.getProperty("couchDB.host"),
                Integer.parseInt(env.getProperty("couchDB.port")),
                env.getProperty("couchDB.user"),
                env.getProperty("couchDB.password"));
    }

    */


}
