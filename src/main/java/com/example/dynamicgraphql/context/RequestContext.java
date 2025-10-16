package com.example.dynamicgraphql.context;

import java.util.Locale;
import java.util.Optional;

public class RequestContext {

    public enum TimeFormat {
        ISO8601,
        EPOCH_SECONDS,
        EPOCH_MILLIS
    }

    public enum GeoFormat {
        GEOJSON,
        WKT
    }

    private final String requestId;
    private final TimeFormat timeFormat;
    private final GeoFormat geoFormat;
    private final Locale locale;

    public RequestContext(String requestId, TimeFormat timeFormat, GeoFormat geoFormat, Locale locale) {
        this.requestId = requestId;
        this.timeFormat = timeFormat;
        this.geoFormat = geoFormat;
        this.locale = locale;
    }

    public String getRequestId() {
        return requestId;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public GeoFormat getGeoFormat() {
        return geoFormat;
    }

    public Locale getLocale() {
        return locale;
    }

    public static TimeFormat parseTimeFormat(String value) {
        if (value == null) {
            return TimeFormat.ISO8601;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "epoch_s" -> TimeFormat.EPOCH_SECONDS;
            case "epoch_ms" -> TimeFormat.EPOCH_MILLIS;
            default -> TimeFormat.ISO8601;
        };
    }

    public static GeoFormat parseGeoFormat(String value) {
        if (value == null) {
            return GeoFormat.GEOJSON;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "wkt" -> GeoFormat.WKT;
            default -> GeoFormat.GEOJSON;
        };
    }

    public static Optional<RequestContext> maybeFrom(Object context) {
        if (context instanceof RequestContext rc) {
            return Optional.of(rc);
        }
        return Optional.empty();
    }
}
