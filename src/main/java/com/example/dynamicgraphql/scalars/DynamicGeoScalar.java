package com.example.dynamicgraphql.scalars;

import com.example.dynamicgraphql.context.RequestContext;
import com.example.dynamicgraphql.context.RequestContext.GeoFormat;
import com.example.dynamicgraphql.context.RequestContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.util.Map;

public final class DynamicGeoScalar {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("Geo")
            .description("Geo scalar supporting GeoJSON and WKT representations")
            .coercing(new Coercing<Object, Object>() {
                @Override
                public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    GeoFormat format = RequestContextHolder.get()
                            .map(RequestContext::getGeoFormat)
                            .orElse(GeoFormat.GEOJSON);
                    if (format == GeoFormat.WKT) {
                        return GeoJsonToWktConverter.toWkt(dataFetcherResult);
                    }
                    try {
                        return OBJECT_MAPPER.writeValueAsString(dataFetcherResult);
                    } catch (JsonProcessingException e) {
                        throw new CoercingSerializeException("Unable to serialize GeoJSON", e);
                    }
                }

                @Override
                public Object parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof String string) {
                        return parseString(string);
                    }
                    if (input instanceof Map<?, ?> map) {
                        return map;
                    }
                    throw new CoercingParseValueException("Unsupported Geo value: " + input);
                }

                @Override
                public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue stringValue) {
                        return parseString(stringValue.getValue());
                    }
                    throw new CoercingParseLiteralException("Unsupported literal for Geo");
                }

                private Object parseString(String value) {
                    try {
                        if (value.trim().toUpperCase().startsWith("POINT") || value.contains("(") && value.contains(")")) {
                            return WktToGeoJsonConverter.toGeoJson(value);
                        }
                        return OBJECT_MAPPER.readValue(value, Map.class);
                    } catch (Exception ex) {
                        throw new CoercingParseValueException("Unable to parse Geo value", ex);
                    }
                }
            })
            .build();

    private DynamicGeoScalar() {
    }
}
