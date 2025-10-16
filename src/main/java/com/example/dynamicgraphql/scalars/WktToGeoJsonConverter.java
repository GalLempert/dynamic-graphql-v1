package com.example.dynamicgraphql.scalars;

import java.util.HashMap;
import java.util.Map;

final class WktToGeoJsonConverter {

    private WktToGeoJsonConverter() {
    }

    static Map<String, Object> toGeoJson(String wkt) {
        String trimmed = wkt.trim();
        int spaceIndex = trimmed.indexOf(' ');
        String type = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : "Point";
        String coordinateSection = spaceIndex > 0 ? trimmed.substring(spaceIndex + 1).trim() : trimmed;
        coordinateSection = coordinateSection.replace('(', '[').replace(')', ']');
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", capitalize(type));
        geoJson.put("coordinates", coordinateSection);
        return geoJson;
    }

    private static String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
    }
}
