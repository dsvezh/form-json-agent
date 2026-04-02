package com.example.agent.generator;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonPayloadBuilder {

    public Map<String, Object> build(Map<String, Object> flatValues) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("payload", new LinkedHashMap<>(flatValues));
        return root;
    }
}
