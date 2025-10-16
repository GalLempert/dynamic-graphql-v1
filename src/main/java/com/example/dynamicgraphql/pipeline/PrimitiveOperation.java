package com.example.dynamicgraphql.pipeline;

import java.util.Map;

public interface PrimitiveOperation {

    String name();

    void execute(PipelineExecutionContext context, Map<String, Object> arguments);
}
