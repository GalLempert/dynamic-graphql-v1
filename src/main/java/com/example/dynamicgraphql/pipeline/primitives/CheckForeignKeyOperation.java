package com.example.dynamicgraphql.pipeline.primitives;

import com.example.dynamicgraphql.persistence.ForeignKeyPolicyService;
import com.example.dynamicgraphql.pipeline.PipelineExecutionContext;
import com.example.dynamicgraphql.pipeline.PrimitiveOperation;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CheckForeignKeyOperation implements PrimitiveOperation {

    private final ForeignKeyPolicyService foreignKeyPolicyService;

    public CheckForeignKeyOperation(ForeignKeyPolicyService foreignKeyPolicyService) {
        this.foreignKeyPolicyService = foreignKeyPolicyService;
    }

    @Override
    public String name() {
        return "check_fk";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(PipelineExecutionContext context, Map<String, Object> arguments) {
        String collection = (String) arguments.getOrDefault("collection", "");
        Map<String, Object> filter = (Map<String, Object>) arguments.getOrDefault("filter", Map.of());
        if (!foreignKeyPolicyService.exists(collection, filter)) {
            throw new IllegalStateException("Foreign key not satisfied for collection " + collection);
        }
    }
}
