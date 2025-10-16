package com.example.dynamicgraphql.scalars;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

final class GeoJsonToWktConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GeoJsonToWktConverter() {
    }

    @SuppressWarnings("unchecked")
    static String toWkt(Object value) {
        try {
            Map<String, Object> geoJson;
            if (value instanceof Map<?, ?> map) {
                geoJson = (Map<String, Object>) map;
            } else if (value instanceof String string) {
                geoJson = OBJECT_MAPPER.readValue(string, Map.class);
            } else {
                throw new IllegalArgumentException("Unsupported GeoJSON representation");
            }
            String type = (String) geoJson.getOrDefault("type", "Point");
            Object coordinates = geoJson.get("coordinates");
            return type.toUpperCase() + " " + OBJECT_MAPPER.writeValueAsString(coordinates);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to convert GeoJSON to WKT", ex);
        }
    }
}
