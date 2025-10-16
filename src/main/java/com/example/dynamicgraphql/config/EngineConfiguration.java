package com.example.dynamicgraphql.config;

import com.example.dynamicgraphql.config.EngineProperties.KafkaProperties;
import com.example.dynamicgraphql.schema.SchemaRegistry;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.time.Duration;
import java.util.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableConfigurationProperties(EngineProperties.class)
public class EngineConfiguration {

    @Bean
    public SchemaRegistry schemaRegistry(EngineProperties properties, ResourceLoader resourceLoader) {
        SchemaRegistry registry = new SchemaRegistry(properties);
        registry.setResourceLoader(resourceLoader);
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoClient mongoClient(EngineProperties properties) {
        EngineProperties.PersistenceProperties persistence = properties.getPersistence();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(persistence.getConnectionString()))
                .applyToSocketSettings(builder -> {
                    Duration timeout = persistence.getCommandTimeout();
                    builder.connectTimeout((int) timeout.toMillis());
                    builder.readTimeout((int) timeout.toMillis());
                })
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaProducer<String, Map<String, Object>> kafkaProducer(KafkaProperties properties) {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", properties.getBootstrapServers()));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        if (!properties.getProducerOverrides().isEmpty()) {
            config.putAll(properties.getProducerOverrides());
        }
        return new KafkaProducer<>(config);
    }
}
