package com.example.dynamicgraphql.plugin;

import com.example.dynamicgraphql.cdc.ChangeDataCapturePublisher;
import com.example.dynamicgraphql.persistence.MongoPersistenceService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PluginContext {

    private final MongoPersistenceService persistenceService;
    private final ChangeDataCapturePublisher cdcPublisher;

    public PluginContext(MongoPersistenceService persistenceService, ChangeDataCapturePublisher cdcPublisher) {
        this.persistenceService = persistenceService;
        this.cdcPublisher = cdcPublisher;
    }

    public void writeDocument(Map<String, Object> document) {
        persistenceService.upsert(document);
    }

    public void publishEvent(String key, Map<String, Object> payload) {
        cdcPublisher.publish(key, payload);
    }
}
