package com.example.dynamicgraphql.schema;

import com.example.dynamicgraphql.config.EngineProperties;
import graphql.language.TypeDefinitionRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

public class SchemaRegistry {

    private static final Logger log = LoggerFactory.getLogger(SchemaRegistry.class);

    private final EngineProperties properties;
    private final SchemaParser parser = new SchemaParser();
    private final SchemaGenerator generator = new SchemaGenerator();
    private final AtomicReference<SchemaSnapshot> current = new AtomicReference<>();
    private ResourceLoader resourceLoader;

    public SchemaRegistry(EngineProperties properties) {
        this.properties = properties;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        refresh(new SchemaBundle(List.of(), null));
    }

    public SchemaSnapshot snapshot() {
        SchemaSnapshot snapshot = current.get();
        if (snapshot == null) {
            refresh(new SchemaBundle(List.of(), null));
            snapshot = current.get();
        }
        return snapshot;
    }

    public synchronized void refresh(SchemaBundle bundle) {
        try {
            List<String> sdlFragments = new ArrayList<>();
            sdlFragments.add(loadCoreSchema());
            sdlFragments.addAll(bundle.fragments());

            TypeDefinitionRegistry mergedRegistry = new TypeDefinitionRegistry();
            sdlFragments.stream()
                    .map(parser::parse)
                    .forEach(mergedRegistry::merge);

            GraphQLSchema schema = generator.makeExecutableSchema(mergedRegistry, RuntimeWiringFactory.defaultWiring());

            Instant loadedAt = bundle.version() == null ? Instant.now() : bundle.version();
            SchemaSnapshot snapshot = new SchemaSnapshot(schema, mergedRegistry, loadedAt);
            current.set(snapshot);
            log.info("GraphQL schema refreshed at {} with {} fragments", snapshot.loadedAt(), sdlFragments.size());
        } catch (Exception ex) {
            log.error("Failed to refresh schema", ex);
            throw new SchemaRegistryException("Unable to refresh schema", ex);
        }
    }

    private String loadCoreSchema() throws IOException {
        String location = properties.getSchema().getCoreSchemaLocation();
        Resource resource = resourceLoader.getResource(location);
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    public record SchemaBundle(List<String> fragments, Instant version) {}

    public record SchemaSnapshot(GraphQLSchema schema, TypeDefinitionRegistry registry, Instant loadedAt) {}

    public static class SchemaRegistryException extends RuntimeException {
        public SchemaRegistryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
