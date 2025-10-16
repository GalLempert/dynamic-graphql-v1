package com.example.dynamicgraphql.graphql;

import com.example.dynamicgraphql.context.RequestContextHolder;
import com.example.dynamicgraphql.schema.SchemaRegistry;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.info.BuildProperties;

@DgsComponent
public class EngineInfoDataFetcher {

    private final SchemaRegistry schemaRegistry;
    private final Optional<BuildProperties> buildProperties;

    public EngineInfoDataFetcher(SchemaRegistry schemaRegistry, Optional<BuildProperties> buildProperties) {
        this.schemaRegistry = schemaRegistry;
        this.buildProperties = buildProperties;
    }

    @DgsQuery
    public Map<String, Object> engineInfo() {
        String requestId = RequestContextHolder.get().map(ctx -> ctx.getRequestId()).orElse("system");
        String name = buildProperties.map(BuildProperties::getName).orElse("dynamic-graphql-engine");
        String version = buildProperties.map(BuildProperties::getVersion).orElse("dev");
        return Map.of(
                "name", name,
                "version", version,
                "schemaVersion", schemaRegistry.snapshot().loadedAt().toString(),
                "requestId", requestId);
    }
}
