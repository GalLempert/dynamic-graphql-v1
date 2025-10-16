package com.example.dynamicgraphql.pipeline;

import com.example.dynamicgraphql.context.RequestContext;
import java.util.HashMap;
import java.util.Map;

public class PipelineExecutionContext {

    private final Map<String, Object> document;
    private final RequestContext requestContext;
    private final Map<String, Object> attributes = new HashMap<>();

    public PipelineExecutionContext(Map<String, Object> document, RequestContext requestContext) {
        this.document = document;
        this.requestContext = requestContext;
    }

    public Map<String, Object> document() {
        return document;
    }

    public RequestContext requestContext() {
        return requestContext;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }
}
