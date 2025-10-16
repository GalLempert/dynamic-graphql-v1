package com.example.dynamicgraphql.pipeline;

import java.util.Map;

public record PipelineStep(String name, Map<String, Object> arguments) {}
