package com.example.dynamicgraphql.service;

import com.example.dynamicgraphql.pipeline.PipelineCatalog;
import com.example.dynamicgraphql.pipeline.PipelineExecutor;
import com.example.dynamicgraphql.pipeline.PipelinePhase;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DocumentMutationService {

    private final PipelineCatalog pipelineCatalog;
    private final PipelineExecutor pipelineExecutor;

    public DocumentMutationService(PipelineCatalog pipelineCatalog, PipelineExecutor pipelineExecutor) {
        this.pipelineCatalog = pipelineCatalog;
        this.pipelineExecutor = pipelineExecutor;
    }

    public void persist(Map<String, Object> document) {
        pipelineCatalog.pipelinesFor(PipelinePhase.ON_PERSIST)
                .forEach(pipeline -> pipelineExecutor.execute(pipeline, document));
    }

    public void update(Map<String, Object> document) {
        pipelineCatalog.pipelinesFor(PipelinePhase.ON_UPDATE)
                .forEach(pipeline -> pipelineExecutor.execute(pipeline, document));
    }
}
