package com.example.dynamicgraphql.zk;

import com.example.dynamicgraphql.config.EngineProperties;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CuratorZookeeperClient implements ZookeeperClient {

    private static final Logger log = LoggerFactory.getLogger(CuratorZookeeperClient.class);

    private final CuratorFramework curator;
    private final Map<String, CuratorCache> caches = new ConcurrentHashMap<>();
    private final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "zk-client-callbacks");
        thread.setDaemon(true);
        return thread;
    });

    public CuratorZookeeperClient(EngineProperties properties) {
        String connectionString = properties.getZookeeper().getConnectionString();
        this.curator = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        this.curator.start();
    }

    @Override
    public List<String> list(String path) {
        try {
            return curator.getChildren().forPath(path);
        } catch (Exception e) {
            log.warn("Unable to list children for path {}", path, e);
            return List.of();
        }
    }

    @Override
    public byte[] getData(String path) {
        try {
            return curator.getData().forPath(path);
        } catch (Exception e) {
            log.warn("Unable to fetch data for path {}", path, e);
            return new byte[0];
        }
    }

    @Override
    public void watch(String path, Runnable callback) {
        caches.computeIfAbsent(path, key -> {
            CuratorCache cache = CuratorCache.build(curator, key);
            CuratorCacheListener listener = CuratorCacheListener.builder()
                    .forAll((type, oldData, data) -> callbackExecutor.execute(callback))
                    .build();
            cache.listenable().addListener(listener);
            cache.start();
            return cache;
        });
    }

    @PreDestroy
    public void close() {
        caches.values().forEach(cache -> {
            try {
                cache.close();
            } catch (Exception ex) {
                log.warn("Error closing Curator cache", ex);
            }
        });
        caches.clear();
        callbackExecutor.shutdownNow();
        curator.close();
    }
}
