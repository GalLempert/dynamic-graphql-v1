package com.example.dynamicgraphql.zk;

import com.example.dynamicgraphql.config.EngineProperties;
import com.example.dynamicgraphql.pipeline.PipelineDefinition;
import com.example.dynamicgraphql.pipeline.PipelinePhase;
import com.example.dynamicgraphql.pipeline.PipelineStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperPipelineRepository {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperPipelineRepository.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EngineProperties properties;
    private final ZookeeperClient client;

    public ZookeeperPipelineRepository(EngineProperties properties, ZookeeperClient client) {
        this.properties = properties;
        this.client = client;
    }

    public List<PipelineDefinition> load() {
        List<PipelineDefinition> pipelines = new ArrayList<>();
        String root = properties.getZookeeper().getPipelineRoot();
        for (String child : client.list(root)) {
            String path = root + "/" + child;
            try {
                byte[] bytes = client.getData(path);
                JsonNode node = OBJECT_MAPPER.readTree(new String(bytes, StandardCharsets.UTF_8));
                PipelinePhase phase = PipelinePhase.fromConfig(node.path("phase").asText("ON_PERSIST"));
                List<PipelineStep> steps = new ArrayList<>();
                JsonNode stepsNode = node.path("steps");
                if (stepsNode.isArray()) {
                    stepsNode.forEach(stepNode -> {
                        String name = stepNode.path("name").asText();
                        JsonNode args = stepNode.path("args");
                        steps.add(new PipelineStep(name, OBJECT_MAPPER.convertValue(args, java.util.Map.class)));
                    });
                }
                pipelines.add(new PipelineDefinition(child, phase, steps));
            } catch (Exception ex) {
                log.error("Failed to parse pipeline {}", path, ex);
            }
        }
        return pipelines;
    }

    public void watch(Runnable onChange) {
        client.watch(properties.getZookeeper().getPipelineRoot(), onChange);
    }
}
