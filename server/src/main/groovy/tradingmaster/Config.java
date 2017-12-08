package tradingmaster;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.channel.MessageChannels;
import tradingmaster.core.CandleAggregator;
import tradingmaster.core.CandleBuilder;
import tradingmaster.core.TradeWriter;
import tradingmaster.db.couchdb.CouchDBClient;
import tradingmaster.db.mariadb.MariaStrategyStore;
import tradingmaster.db.mariadb.MariaTradeStore;
import tradingmaster.exchange.bittrex.BittrexApi11;
import tradingmaster.model.IStrategyStore;
import tradingmaster.model.ITradeStore;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@EnableIntegration
public class Config {

    @Autowired
    private Environment env;


    @Bean
    BittrexApi11 bittrexApi11() {
        return new BittrexApi11(env.getProperty("bittrex.apikey"), env.getProperty("bittrex.secret"), 1, 15);
    }


    @Bean
    public PublishSubscribeChannel tradeChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public PublishSubscribeChannel candelChannel1Minute() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    CandleAggregator candelAggregator5Minutes() {
        CandleAggregator ca = new CandleAggregator(5);
        candelChannel1Minute().subscribe(ca);
        return ca;
    }

    @Bean
    CandleAggregator candelAggregator15Minutes() {
        CandleAggregator ca = new CandleAggregator(15);
        candelChannel1Minute().subscribe(ca);
        return ca;
    }


    @Bean
    public TradeWriter tradeWriter() {
        TradeWriter tw = new TradeWriter();
        tradeChannel().subscribe(tw);
        return tw;
    }

    @Bean
    public CandleBuilder candelBuilder() {
        CandleBuilder cb = new CandleBuilder();
        tradeChannel().subscribe(cb);
        return cb;
    }


    @Bean
    CouchDBClient couchDBClient() {
        return new CouchDBClient(
                env.getProperty("couchDB.host"),
                Integer.parseInt(env.getProperty("couchDB.port")),
                env.getProperty("couchDB.user"),
                env.getProperty("couchDB.password"));
    }

    @Bean
    ITradeStore tradeStore() {
        return new MariaTradeStore();
    }

    @Bean
    IStrategyStore strategyStore() {
        return new MariaStrategyStore();
    }


    @Bean
    @ConfigurationProperties("mariaDB")
    public HikariDataSource dataSource() {
        return (HikariDataSource) DataSourceBuilder.create()
                .type(HikariDataSource.class).build();
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC")); // let's use UTC by default
    }



}
