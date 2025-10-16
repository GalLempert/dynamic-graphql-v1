package com.example.dynamicgraphql.scalars;

import com.example.dynamicgraphql.context.RequestContext;
import com.example.dynamicgraphql.context.RequestContext.TimeFormat;
import com.example.dynamicgraphql.context.RequestContextHolder;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class DynamicDateTimeScalar {

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("DateTime scalar supporting ISO8601, epoch milliseconds, and epoch seconds")
            .coercing(new Coercing<Instant, Object>() {
                @Override
                public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    Instant instant = convertToInstant(dataFetcherResult);
                    TimeFormat format = RequestContextHolder.get()
                            .map(RequestContext::getTimeFormat)
                            .orElse(TimeFormat.ISO8601);
                    return switch (format) {
                        case EPOCH_SECONDS -> instant.getEpochSecond();
                        case EPOCH_MILLIS -> instant.toEpochMilli();
                        case ISO8601 -> DateTimeFormatter.ISO_INSTANT.format(instant);
                    };
                }

                @Override
                public Instant parseValue(Object input) throws CoercingParseValueException {
                    return convertToInstant(input);
                }

                @Override
                public Instant parseLiteral(Value<?> input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue stringValue) {
                        return convertToInstant(stringValue.getValue());
                    }
                    if (input instanceof IntValue intValue) {
                        return Instant.ofEpochSecond(intValue.getValue().longValue());
                    }
                    if (input instanceof FloatValue floatValue) {
                        return Instant.ofEpochMilli(floatValue.getValue().longValue());
                    }
                    throw new CoercingParseLiteralException("Unsupported literal for DateTime");
                }

                private Instant convertToInstant(Object value) {
                    if (value instanceof Instant instant) {
                        return instant;
                    }
                    if (value instanceof Number number) {
                        long numeric = number.longValue();
                        if (Math.abs(numeric) < 3_000_000_000L) {
                            return Instant.ofEpochSecond(numeric);
                        }
                        return Instant.ofEpochMilli(numeric);
                    }
                    if (value instanceof String string) {
                        try {
                            return Instant.parse(string);
                        } catch (Exception ex) {
                            try {
                                long epoch = Long.parseLong(string);
                                return convertToInstant(epoch);
                            } catch (NumberFormatException ignored) {
                                throw new CoercingParseValueException("Unable to parse DateTime value");
                            }
                        }
                    }
                    throw new CoercingSerializeException("Unsupported DateTime representation: " + value);
                }
            })
            .build();

    private DynamicDateTimeScalar() {
    }
}
