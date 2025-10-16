package com.example.dynamicgraphql.pipeline;

import com.example.dynamicgraphql.zk.ZookeeperPipelineRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PipelineCatalog {

    private static final Logger log = LoggerFactory.getLogger(PipelineCatalog.class);

    private final ZookeeperPipelineRepository repository;
    private final Map<PipelinePhase, List<PipelineDefinition>> pipelines = new ConcurrentHashMap<>();
    private final ExecutorService refreshExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "pipeline-catalog-refresh");
        thread.setDaemon(true);
        return thread;
    });
    private volatile Instant lastRefresh = Instant.EPOCH;

    public PipelineCatalog(ZookeeperPipelineRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void start() {
        safeRefresh();
        repository.watch(() -> refreshExecutor.execute(this::safeRefresh));
    }

    public void refresh() {
        safeRefresh();
    }

    private void safeRefresh() {
        try {
            performRefresh();
        } catch (Exception ex) {
            log.error("Failed to refresh pipeline catalog", ex);
        }
    }

    private void performRefresh() {
        List<PipelineDefinition> loaded = repository.load();
        Map<PipelinePhase, List<PipelineDefinition>> byPhase = loaded.stream()
                .collect(java.util.stream.Collectors.groupingBy(PipelineDefinition::phase));
        pipelines.clear();
        pipelines.putAll(byPhase);
        lastRefresh = Instant.now();
        log.info("Pipeline catalog refreshed with {} definitions", loaded.size());
    }

    public List<PipelineDefinition> pipelinesFor(PipelinePhase phase) {
        return pipelines.getOrDefault(phase, List.of());
    }

    public Instant getLastRefresh() {
        return lastRefresh;
    }

    @PreDestroy
    void stop() {
        refreshExecutor.shutdownNow();
    }
}
