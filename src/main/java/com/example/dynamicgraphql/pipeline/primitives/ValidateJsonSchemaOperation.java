package com.example.dynamicgraphql.pipeline.primitives;

import com.example.dynamicgraphql.pipeline.PipelineExecutionContext;
import com.example.dynamicgraphql.pipeline.PrimitiveOperation;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidateJsonSchemaOperation implements PrimitiveOperation {

    private static final Logger log = LoggerFactory.getLogger(ValidateJsonSchemaOperation.class);

    @Override
    public String name() {
        return "validate_json_schema";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(PipelineExecutionContext context, Map<String, Object> arguments) {
        Object required = arguments.getOrDefault("requiredFields", List.of());
        if (required instanceof List<?> requiredFields) {
            for (Object field : requiredFields) {
                if (field instanceof String fieldName) {
                    if (!context.document().containsKey(fieldName)) {
                        throw new IllegalStateException("Missing required field: " + fieldName);
                    }
                }
            }
        }
        log.debug("Validated document {} against lightweight schema", context.requestContext().getRequestId());
    }
}
