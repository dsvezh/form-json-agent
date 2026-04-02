package com.example.agent.generator;

import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FieldOption;
import com.example.agent.model.FormSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CombinationGenerator {

    public List<Map<String, Object>> generate(FormSchema schema, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        backtrack(schema.getFields(), 0, new LinkedHashMap<>(), results, limit);
        return results;
    }

    private void backtrack(List<FieldDescriptor> fields,
                           int index,
                           Map<String, Object> current,
                           List<Map<String, Object>> results,
                           int limit) {
        if (results.size() >= limit) {
            return;
        }

        if (index == fields.size()) {
            results.add(new LinkedHashMap<>(current));
            return;
        }

        FieldDescriptor field = fields.get(index);

        if ("select".equals(field.getType()) && !field.getOptions().isEmpty()) {
            for (FieldOption option : field.getOptions()) {
                current.put(field.getKey(), option.value());
                backtrack(fields, index + 1, current, results, limit);
                current.remove(field.getKey());

                if (results.size() >= limit) {
                    return;
                }
            }
            return;
        }

        current.put(field.getKey(), sampleValue(field));
        backtrack(fields, index + 1, current, results, limit);
        current.remove(field.getKey());
    }

    private Object sampleValue(FieldDescriptor field) {
        return switch (field.getType()) {
            case "number" -> 100;
            case "email" -> "test@example.com";
            case "checkbox" -> true;
            case "textarea" -> "sample text";
            case "date" -> "2026-04-02";
            default -> "sample";
        };
    }
}
