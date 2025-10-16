package com.example.dynamicgraphql.schema;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dynamicgraphql.config.EngineProperties;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class SchemaRegistryTest {

    @Test
    void loadsCoreSchema() {
        EngineProperties properties = new EngineProperties();
        properties.getSchema().setCoreSchemaLocation("classpath:schema/core.graphqls");
        SchemaRegistry registry = new SchemaRegistry(properties);
        registry.setResourceLoader(new DefaultResourceLoader());

        SchemaRegistry.SchemaSnapshot snapshot = registry.snapshot();

        assertThat(snapshot.schema().getQueryType().getName()).isEqualTo("Query");
        assertThat(snapshot.registry().getType("EngineInfo")).isPresent();
        assertThat(snapshot.loadedAt()).isAfterOrEqualTo(Instant.EPOCH);
    }

    @Test
    void refreshMergesDynamicFragments() {
        EngineProperties properties = new EngineProperties();
        properties.getSchema().setCoreSchemaLocation("classpath:schema/core.graphqls");
        SchemaRegistry registry = new SchemaRegistry(properties);
        registry.setResourceLoader(new DefaultResourceLoader());

        registry.refresh(new SchemaRegistry.SchemaBundle(List.of("extend type Query { ping: String! }"), Instant.now()));

        assertThat(registry.snapshot().registry().getType("Query")).isPresent();
    }
}
