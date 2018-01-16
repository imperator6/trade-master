package tradingmaster.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tradingmaster.db.mariadb.MariaCandleStore;
import tradingmaster.db.mariadb.MariaStrategyStore;
import tradingmaster.db.mariadb.MariaTradeStore;
import tradingmaster.model.ICandleStore;
import tradingmaster.model.ITradeStore;

@Configuration
public class MariaDbConfig {

    @Bean
    @ConfigurationProperties("mariaDB")
    public HikariDataSource dataSource() {
        return (HikariDataSource) DataSourceBuilder.create()
                .type(HikariDataSource.class).build();
    }

    @Bean
    ITradeStore tradeStore() {
        return new MariaTradeStore();
    }

    @Bean
    ICandleStore candleStore() { return new MariaCandleStore(); }

    @Bean
    MariaStrategyStore strategyStore() {
        return new MariaStrategyStore();
    }
}
