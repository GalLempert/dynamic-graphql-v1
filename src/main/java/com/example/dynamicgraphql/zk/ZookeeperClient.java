package com.example.dynamicgraphql.zk;

import java.util.List;

public interface ZookeeperClient {

    List<String> list(String path);

    byte[] getData(String path);

    void watch(String path, Runnable callback);
}
