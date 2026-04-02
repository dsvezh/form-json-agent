package com.example.agent.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Читает mapping-файл.
 * Формат простой: "имя_поля": "payload.customer.country".
 */
public class MappingRulesLoader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mapping rules from " + path, e);
        }
    }
}
