package com.example.dynamicgraphql.plugin;

import com.example.dynamicgraphql.pipeline.PipelineExecutionContext;
import com.example.dynamicgraphql.pipeline.PipelinePhase;
import java.util.Set;

public interface Plugin {

    String id();

    Set<PipelinePhase> phases();

    PluginResult apply(PipelinePhase phase, PipelineExecutionContext context, PluginContext pluginContext);
}
