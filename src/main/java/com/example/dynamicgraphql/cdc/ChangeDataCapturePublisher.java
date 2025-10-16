package com.example.dynamicgraphql.cdc;

import com.example.dynamicgraphql.config.EngineProperties;
import java.util.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChangeDataCapturePublisher {

    private static final Logger log = LoggerFactory.getLogger(ChangeDataCapturePublisher.class);

    private final KafkaProducer<String, Map<String, Object>> producer;
    private final EngineProperties properties;

    public ChangeDataCapturePublisher(KafkaProducer<String, Map<String, Object>> producer, EngineProperties properties) {
        this.producer = producer;
        this.properties = properties;
    }

    public void publish(String key, Map<String, Object> payload) {
        producer.send(new ProducerRecord<>(properties.getKafka().getTopic(), key, payload));
        log.debug("CDC event for {} dispatched", key);
    }
}
