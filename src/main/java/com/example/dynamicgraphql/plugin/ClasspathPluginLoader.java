package com.example.dynamicgraphql.plugin;

import com.example.dynamicgraphql.pipeline.PipelinePhase;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ClasspathPluginLoader implements PluginLoader {

    private final ApplicationContext applicationContext;

    public ClasspathPluginLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<Plugin> loadPlugins(PipelinePhase phase) {
        Map<String, Plugin> beans = applicationContext.getBeansOfType(Plugin.class);
        return beans.values().stream()
                .filter(plugin -> plugin.phases().contains(phase))
                .collect(Collectors.toList());
    }
}
