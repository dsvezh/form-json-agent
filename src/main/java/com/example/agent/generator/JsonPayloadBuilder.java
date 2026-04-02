package com.example.agent.generator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Собирает nested JSON из плоского набора значений.
 * Пример:
 * country -> payload.customer.country
 * amount  -> payload.payment.amount
 */
public class JsonPayloadBuilder {

    public Map<String, Object> build(Map<String, Object> flatValues, Map<String, String> mappingRules) {
        Map<String, Object> root = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : flatValues.entrySet()) {
            String fieldKey = entry.getKey();
            Object value = entry.getValue();
            String targetPath = mappingRules.getOrDefault(fieldKey, "payload." + fieldKey);

            putByPath(root, targetPath, value);
        }

        return root;
    }

    /**
     * Кладет значение по пути вида a.b.c.
     * Если промежуточных объектов нет, они создаются автоматически.
     */
    @SuppressWarnings("unchecked")
    private void putByPath(Map<String, Object> root, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object nested = current.get(part);

            if (!(nested instanceof Map<?, ?>)) {
                Map<String, Object> created = new LinkedHashMap<>();
                current.put(part, created);
                current = created;
            } else {
                current = (Map<String, Object>) nested;
            }
        }

        current.put(parts[parts.length - 1], value);
    }
}
