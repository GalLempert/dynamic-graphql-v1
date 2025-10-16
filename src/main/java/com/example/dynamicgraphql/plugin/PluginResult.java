package com.example.dynamicgraphql.plugin;

public record PluginResult(boolean success, String message) {

    public static PluginResult success(String message) {
        return new PluginResult(true, message);
    }

    public static PluginResult failure(String message) {
        return new PluginResult(false, message);
    }
}
