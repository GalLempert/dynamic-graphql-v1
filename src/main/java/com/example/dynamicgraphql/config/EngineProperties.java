package com.example.dynamicgraphql.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "engine")
public class EngineProperties {

    private final ZookeeperProperties zookeeper = new ZookeeperProperties();
    private final PersistenceProperties persistence = new PersistenceProperties();
    private final KafkaProperties kafka = new KafkaProperties();
    private final SchemaProperties schema = new SchemaProperties();
    private final PluginProperties plugin = new PluginProperties();

    public ZookeeperProperties getZookeeper() {
        return zookeeper;
    }

    public PersistenceProperties getPersistence() {
        return persistence;
    }

    public KafkaProperties getKafka() {
        return kafka;
    }

    public SchemaProperties getSchema() {
        return schema;
    }

    public PluginProperties getPlugin() {
        return plugin;
    }

    public static class ZookeeperProperties {
        private String connectionString = "localhost:2181";
        private Duration refreshInterval = Duration.ofSeconds(30);
        private String schemaRoot = "/dynamic/graphql/schema";
        private String pipelineRoot = "/dynamic/graphql/pipelines";

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public Duration getRefreshInterval() {
            return refreshInterval;
        }

        public void setRefreshInterval(Duration refreshInterval) {
            this.refreshInterval = refreshInterval;
        }

        public String getSchemaRoot() {
            return schemaRoot;
        }

        public void setSchemaRoot(String schemaRoot) {
            this.schemaRoot = schemaRoot;
        }

        public String getPipelineRoot() {
            return pipelineRoot;
        }

        public void setPipelineRoot(String pipelineRoot) {
            this.pipelineRoot = pipelineRoot;
        }
    }

    public static class PersistenceProperties {
        private String connectionString = "mongodb://localhost:27017";
        private String database = "dynamic";
        private String collection = "documents";
        private Duration commandTimeout = Duration.ofSeconds(5);

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getCollection() {
            return collection;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }

        public Duration getCommandTimeout() {
            return commandTimeout;
        }

        public void setCommandTimeout(Duration commandTimeout) {
            this.commandTimeout = commandTimeout;
        }
    }

    public static class KafkaProperties {
        private String topic = "dynamic-graphql-cdc";
        private List<String> bootstrapServers = List.of("localhost:9092");
        private java.util.Map<String, Object> producerOverrides = new java.util.HashMap<>();

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public List<String> getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(List<String> bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public java.util.Map<String, Object> getProducerOverrides() {
            return producerOverrides;
        }

        public void setProducerOverrides(java.util.Map<String, Object> producerOverrides) {
            this.producerOverrides.clear();
            if (producerOverrides != null) {
                this.producerOverrides.putAll(producerOverrides);
            }
        }
    }

    public static class SchemaProperties {
        private String coreSchemaLocation = "classpath:schema/core.graphqls";
        private String jsonSchemaRoot = "/dynamic/graphql/json-schema";

        public String getCoreSchemaLocation() {
            return coreSchemaLocation;
        }

        public void setCoreSchemaLocation(String coreSchemaLocation) {
            this.coreSchemaLocation = coreSchemaLocation;
        }

        public String getJsonSchemaRoot() {
            return jsonSchemaRoot;
        }

        public void setJsonSchemaRoot(String jsonSchemaRoot) {
            this.jsonSchemaRoot = jsonSchemaRoot;
        }
    }

    public static class PluginProperties {
        private String pluginsRoot = "/plugins";
        private boolean enableSignatureVerification = true;

        public String getPluginsRoot() {
            return pluginsRoot;
        }

        public void setPluginsRoot(String pluginsRoot) {
            this.pluginsRoot = pluginsRoot;
        }

        public boolean isEnableSignatureVerification() {
            return enableSignatureVerification;
        }

        public void setEnableSignatureVerification(boolean enableSignatureVerification) {
            this.enableSignatureVerification = enableSignatureVerification;
        }
    }
}
