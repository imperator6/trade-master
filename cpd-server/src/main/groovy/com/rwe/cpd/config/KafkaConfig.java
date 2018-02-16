package com.rwe.cpd.config;

import com.rwe.cpd.kafka.MapDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
class KafkaConfig {

    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value("${kafka.groupId}")
    private String groupId;


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        // list of host:port pairs used for establishing the initial connections to the Kafka cluster
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // allows a pool of processes to divide the work of consuming and processing records
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // automatically reset the offset to the earliest offset
        //props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    @Bean
    public DefaultKafkaConsumerFactory consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
                new MapDeserializer());
    }


    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Map>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConcurrency(1); // only ONE Thread
/*
        factory.setRecordFilterStrategy(new RecordFilterStrategy<String, Map>() {
            @Override
            public boolean filter(ConsumerRecord<String, Map> consumerRecord) {
                return !"POWER".equalsIgnoreCase((String) consumerRecord.value().get("S_Commodity"));
            }
        });
*/
        //factory.setBatchListener(true); // receive all objects from a poll --> List<KafkaPrice>

        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

