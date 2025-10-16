package com.example.dynamicgraphql.plugin;

import com.example.dynamicgraphql.pipeline.PipelinePhase;

public record PluginDescriptor(
        String id,
        PipelinePhase phase,
        String type,
        String entry,
        String artifact,
        String version,
        String sha256,
        String endpoint) {}
