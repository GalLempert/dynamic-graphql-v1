package com.example.dynamicgraphql.pipeline;

public enum PipelinePhase {
    ON_PERSIST,
    ON_UPDATE,
    ON_READ,
    ON_CRON;

    public static PipelinePhase fromConfig(String value) {
        return PipelinePhase.valueOf(value.trim().toUpperCase());
    }
}
