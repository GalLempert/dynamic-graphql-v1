package com.example.dynamicgraphql.pipeline.primitives;

import com.example.dynamicgraphql.persistence.MongoPersistenceService;
import com.example.dynamicgraphql.pipeline.PipelineExecutionContext;
import com.example.dynamicgraphql.pipeline.PrimitiveOperation;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WriteDocumentOperation implements PrimitiveOperation {

    private final MongoPersistenceService persistenceService;

    public WriteDocumentOperation(MongoPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public String name() {
        return "write_doc";
    }

    @Override
    public void execute(PipelineExecutionContext context, Map<String, Object> arguments) {
        persistenceService.upsert(context.document());
    }
}
