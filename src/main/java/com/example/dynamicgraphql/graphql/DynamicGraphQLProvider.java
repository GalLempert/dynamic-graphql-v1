package com.example.dynamicgraphql.graphql;

import com.example.dynamicgraphql.scalars.DynamicDateTimeScalar;
import com.example.dynamicgraphql.scalars.DynamicGeoScalar;
import com.example.dynamicgraphql.scalars.DynamicJsonScalar;
import com.example.dynamicgraphql.schema.SchemaRegistry;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsRuntimeWiring;
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry;
import graphql.language.TypeDefinitionRegistry;
import graphql.schema.idl.RuntimeWiring;

@DgsComponent
public class DynamicGraphQLProvider {

    private final SchemaRegistry schemaRegistry;

    public DynamicGraphQLProvider(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    @DgsTypeDefinitionRegistry
    public TypeDefinitionRegistry registry() {
        return schemaRegistry.snapshot().registry();
    }

    @DgsRuntimeWiring
    public RuntimeWiring.Builder runtimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(DynamicDateTimeScalar.INSTANCE)
                .scalar(DynamicGeoScalar.INSTANCE)
                .scalar(DynamicJsonScalar.INSTANCE);
    }
}
