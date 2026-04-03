package com.example.agent.generator;

import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FieldOption;
import com.example.agent.model.FormSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Генерирует сценарии заполнения формы.
 * Важная идея: мы не делаем "все со всем" без ограничений,
 * а пытаемся учитывать зависимые dropdown и лимит сценариев.
 */
public class CombinationGenerator {

    public List<Map<String, Object>> generate(FormSchema schema, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        backtrack(schema, 0, new LinkedHashMap<>(), results, limit);
        return results;
    }

    private void backtrack(FormSchema schema,
                           int index,
                           Map<String, Object> current,
                           List<Map<String, Object>> results,
                           int limit) {
        if (results.size() >= limit) {
            return;
        }

        List<FieldDescriptor> fields = schema.getFields();
        if (index == fields.size()) {
            results.add(new LinkedHashMap<>(current));
            return;
        }

        FieldDescriptor field = fields.get(index);

        if ("select".equals(field.getType()) && !field.getOptions().isEmpty()) {
            for (FieldOption option : resolveAvailableOptions(schema, field, current)) {
                current.put(field.getKey(), option.value());
                backtrack(schema, index + 1, current, results, limit);
                current.remove(field.getKey());

                if (results.size() >= limit) {
                    return;
                }
            }
            return;
        }

        // Для обычных полей берем безопасные тестовые значения.
        current.put(field.getKey(), sampleValue(field));
        backtrack(schema, index + 1, current, results, limit);
        current.remove(field.getKey());
    }

    private List<FieldOption> resolveAvailableOptions(FormSchema schema,
                                                      FieldDescriptor field,
                                                      Map<String, Object> current) {
        if (field.getDependsOn() == null || field.getDependsOn().isBlank()) {
            return field.getOptions();
        }

        Object parentValue = current.get(field.getDependsOn());
        if (parentValue == null) {
            return field.getOptions();
        }

        List<FieldOption> dependentOptions = schema.getDependencySnapshots().stream()
                .filter(snapshot -> snapshot.getChildField().equals(field.getKey()))
                .filter(snapshot -> snapshot.getParentField().equals(field.getDependsOn()))
                .filter(snapshot -> snapshot.getParentValue().equals(parentValue.toString()))
                .flatMap(snapshot -> snapshot.getOptions().stream())
                .distinct()
                .toList();

        return dependentOptions.isEmpty() ? field.getOptions() : dependentOptions;
    }

    private Object sampleValue(FieldDescriptor field) {
        return switch (field.getType()) {
            case "number", "range" -> 100;
            case "email" -> "test@example.com";
            case "checkbox" -> true;
            case "textarea" -> "Sample text for generated payload";
            case "date" -> "2026-04-03";
            case "tel" -> "+79001234567";
            default -> "sample";
        };
    }
}
