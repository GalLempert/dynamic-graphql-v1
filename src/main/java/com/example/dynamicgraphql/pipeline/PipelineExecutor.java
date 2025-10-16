package com.example.dynamicgraphql.pipeline;

import com.example.dynamicgraphql.context.RequestContext;
import com.example.dynamicgraphql.context.RequestContextHolder;
import com.example.dynamicgraphql.plugin.Plugin;
import com.example.dynamicgraphql.plugin.PluginContext;
import com.example.dynamicgraphql.plugin.PluginLoader;
import com.example.dynamicgraphql.plugin.PluginResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PipelineExecutor {

    private static final Logger log = LoggerFactory.getLogger(PipelineExecutor.class);

    private final Map<String, PrimitiveOperation> primitivesByName;
    private final PluginLoader pluginLoader;
    private final PluginContext pluginContext;
    private final MeterRegistry meterRegistry;

    public PipelineExecutor(List<PrimitiveOperation> primitives, PluginLoader pluginLoader, PluginContext pluginContext,
            MeterRegistry meterRegistry) {
        this.primitivesByName = primitives.stream().collect(java.util.stream.Collectors.toMap(PrimitiveOperation::name, it -> it));
        this.pluginLoader = pluginLoader;
        this.pluginContext = pluginContext;
        this.meterRegistry = meterRegistry;
    }

    public void execute(PipelineDefinition pipeline, Map<String, Object> document) {
        RequestContext requestContext = RequestContextHolder.get().orElse(new RequestContext("system", RequestContext.TimeFormat.ISO8601, RequestContext.GeoFormat.GEOJSON, java.util.Locale.getDefault()));
        PipelineExecutionContext context = new PipelineExecutionContext(document, requestContext);
        log.info("Executing pipeline {} phase {}", pipeline.name(), pipeline.phase());
        for (PipelineStep step : pipeline.steps()) {
            PrimitiveOperation primitive = primitivesByName.get(step.name());
            if (primitive != null) {
                Timer.Sample sample = Timer.start(meterRegistry);
                try {
                    primitive.execute(context, step.arguments());
                } finally {
                    sample.stop(Timer.builder("pipeline_primitive_seconds")
                            .tag("pipeline", pipeline.name())
                            .tag("phase", pipeline.phase().name())
                            .tag("primitive", step.name())
                            .register(meterRegistry));
                }
            } else {
                log.warn("Unknown primitive {} in pipeline {}", step.name(), pipeline.name());
                meterRegistry.counter("pipeline_unknown_primitives_total",
                                "pipeline", pipeline.name(),
                                "phase", pipeline.phase().name(),
                                "primitive", step.name())
                        .increment();
            }
        }
        Collection<Plugin> plugins = pluginLoader.loadPlugins(pipeline.phase());
        for (Plugin plugin : plugins) {
            PluginResult result;
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                result = plugin.apply(pipeline.phase(), context, pluginContext);
            } catch (RuntimeException ex) {
                meterRegistry.counter("plugin_invocation_failures_total",
                                "pipeline", pipeline.name(),
                                "phase", pipeline.phase().name(),
                                "plugin", plugin.id())
                        .increment();
                throw ex;
            } finally {
                sample.stop(Timer.builder("plugin_invocation_seconds")
                        .tag("pipeline", pipeline.name())
                        .tag("phase", pipeline.phase().name())
                        .tag("plugin", plugin.id())
                        .register(meterRegistry));
            }
            if (!result.success()) {
                meterRegistry.counter("plugin_invocation_failures_total",
                                "pipeline", pipeline.name(),
                                "phase", pipeline.phase().name(),
                                "plugin", plugin.id())
                        .increment();
                throw new IllegalStateException("Plugin " + plugin.id() + " failed: " + result.message());
            }
        }
    }
}
