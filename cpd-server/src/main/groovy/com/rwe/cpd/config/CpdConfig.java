package com.rwe.cpd.config;

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



}
