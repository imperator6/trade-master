package com.rwe.cpd.config;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableIntegration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.rwe.platform.*", "com.rwe.cpd.*"})
@EnableJpaRepositories(basePackages = {"com.rwe.platform.*", "com.rwe.cpd.*"})
@EntityScan(basePackages = {"com.rwe.platform.db.*", "com.rwe.cpd.db.*" })
public class CpdConfig {

    @Bean
    public PublishSubscribeChannel priceChannel() {
        return MessageChannels.publishSubscribe().get();
    }


    @Bean
    CouchDbInstance couchDbInstance(@Value("${couchDB.host}") String host,
                                   @Value("${couchDB.port}") Integer port,
                                   @Value("${couchDB.user}") String user,@Value("${couchDB.password}") String password) throws Exception {


        HttpClient httpClient = new StdHttpClient.Builder()
                .url("http://"+ host + ":" + port)
                .username(user)
                .password(password)
                .build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

        return dbInstance;
    }


    @Bean
    CouchDbConnector orderBookStorage(CouchDbInstance couchDbInstance) throws Exception {
        CouchDbConnector client = new StdCouchDbConnector("cpd_orderbook", couchDbInstance);
        return client;
    }

    @Bean
    CouchDbConnector configStorage(CouchDbInstance couchDbInstance) throws Exception {
        CouchDbConnector client = new StdCouchDbConnector("cpd_config", couchDbInstance);
        return client;
    }
}
