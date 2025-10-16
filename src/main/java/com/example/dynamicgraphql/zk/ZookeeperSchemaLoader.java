package com.example.dynamicgraphql.zk;

import com.example.dynamicgraphql.config.EngineProperties;
import com.example.dynamicgraphql.schema.SchemaRegistry;
import com.example.dynamicgraphql.schema.SchemaRegistry.SchemaBundle;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperSchemaLoader {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperSchemaLoader.class);

    private final EngineProperties properties;
    private final ZookeeperClient client;
    private final SchemaRegistry schemaRegistry;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "zk-schema-refresh");
        thread.setDaemon(true);
        return thread;
    });

    public ZookeeperSchemaLoader(EngineProperties properties, ZookeeperClient client, SchemaRegistry schemaRegistry) {
        this.properties = properties;
        this.client = client;
        this.schemaRegistry = schemaRegistry;
    }

    @PostConstruct
    void start() {
        Duration interval = properties.getZookeeper().getRefreshInterval();
        long seconds = Math.max(1, interval.toSeconds());
        executor.scheduleAtFixedRate(this::safeRefresh, 0, seconds, TimeUnit.SECONDS);
        client.watch(properties.getZookeeper().getSchemaRoot(), this::triggerRefresh);
    }

    public void refresh() {
        safeRefresh();
    }

    private void triggerRefresh() {
        executor.execute(this::safeRefresh);
    }

    private void safeRefresh() {
        try {
            refreshInternal();
        } catch (Exception ex) {
            log.error("Schema refresh failed", ex);
        }
    }

    private void refreshInternal() {
        String root = properties.getZookeeper().getSchemaRoot();
        List<String> children = client.list(root);
        List<String> fragments = children.stream()
                .map(child -> root + "/" + child)
                .map(client::getData)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .toList();
        schemaRegistry.refresh(new SchemaBundle(fragments, Instant.now()));
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }
}
