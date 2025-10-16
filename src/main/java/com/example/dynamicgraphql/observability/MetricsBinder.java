package com.example.dynamicgraphql.observability;

import com.example.dynamicgraphql.pipeline.PipelineCatalog;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MetricsBinder {

    private final MeterRegistry meterRegistry;
    private final PipelineCatalog pipelineCatalog;

    public MetricsBinder(MeterRegistry meterRegistry, PipelineCatalog pipelineCatalog) {
        this.meterRegistry = meterRegistry;
        this.pipelineCatalog = pipelineCatalog;
    }

    @PostConstruct
    public void bind() {
        meterRegistry.gauge("pipeline_catalog_last_refresh_seconds", pipelineCatalog,
                catalog -> (double) (System.currentTimeMillis() - catalog.getLastRefresh().toEpochMilli()) / 1000.0);
    }
}
