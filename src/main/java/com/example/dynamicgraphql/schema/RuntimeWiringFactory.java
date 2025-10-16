package com.example.dynamicgraphql.schema;

import com.example.dynamicgraphql.scalars.DynamicDateTimeScalar;
import com.example.dynamicgraphql.scalars.DynamicGeoScalar;
import com.example.dynamicgraphql.scalars.DynamicJsonScalar;
import graphql.schema.idl.RuntimeWiring;

public final class RuntimeWiringFactory {

    private RuntimeWiringFactory() {
    }

    public static RuntimeWiring defaultWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(DynamicDateTimeScalar.INSTANCE)
                .scalar(DynamicGeoScalar.INSTANCE)
                .scalar(DynamicJsonScalar.INSTANCE)
                .build();
    }
}
