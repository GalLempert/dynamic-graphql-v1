package com.example.dynamicgraphql.plugin;

import com.example.dynamicgraphql.pipeline.PipelinePhase;
import java.util.Collection;

public interface PluginLoader {

    Collection<Plugin> loadPlugins(PipelinePhase phase);
}
