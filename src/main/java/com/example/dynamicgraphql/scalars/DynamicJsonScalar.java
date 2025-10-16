package com.example.dynamicgraphql.scalars;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

public final class DynamicJsonScalar {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("JSON")
            .description("JSON scalar with transparent serialization")
            .coercing(new Coercing<Object, Object>() {
                @Override
                public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    return dataFetcherResult;
                }

                @Override
                public Object parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof String string) {
                        try {
                            return OBJECT_MAPPER.readTree(string);
                        } catch (JsonProcessingException e) {
                            throw new CoercingParseValueException("Unable to parse JSON", e);
                        }
                    }
                    return input;
                }

                @Override
                public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue stringValue) {
                        return parseValue(stringValue.getValue());
                    }
                    throw new CoercingParseLiteralException("JSON literal must be a string");
                }
            })
            .build();

    private DynamicJsonScalar() {
    }
}
