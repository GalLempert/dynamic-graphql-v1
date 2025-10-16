package com.example.dynamicgraphql.pipeline;

import java.util.List;

public record PipelineDefinition(String name, PipelinePhase phase, List<PipelineStep> steps) {}
