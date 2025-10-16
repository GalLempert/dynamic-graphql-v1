package com.example.dynamicgraphql.persistence;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ForeignKeyPolicyService {

    private static final Logger log = LoggerFactory.getLogger(ForeignKeyPolicyService.class);

    public boolean exists(String collection, Map<String, Object> filter) {
        log.debug("Checking FK in {} with filter {}", collection, filter);
        return true;
    }
}
