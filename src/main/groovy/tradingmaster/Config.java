package tradingmaster;

import com.zaxxer.hikari.HikariDataSource;
import groovy.sql.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.channel.MessageChannels;
import tradingmaster.core.CandelBuilder;
import tradingmaster.core.TradeWriter;
import tradingmaster.db.couchdb.CouchDBClient;
import tradingmaster.db.couchdb.CouchDBTradeStore;
import tradingmaster.db.ITradeStore;
import tradingmaster.db.mariadb.MariaTradeStore;
import tradingmaster.exchange.bittrex.BittrexApi11;

import javax.sql.DataSource;

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
    public TradeWriter tradeWriter() {
        TradeWriter tw = new TradeWriter();
        tradeChannel().subscribe(tw);
        return tw;
    }

    @Bean
    public CandelBuilder candelBuilder() {
        CandelBuilder cb = new CandelBuilder();
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
    @ConfigurationProperties("mariaDB")
    public HikariDataSource dataSource() {
        return (HikariDataSource) DataSourceBuilder.create()
                .type(HikariDataSource.class).build();
    }



}
