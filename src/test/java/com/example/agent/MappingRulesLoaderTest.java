package com.example.agent;

import com.example.agent.generator.MappingRulesLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Проверяет чтение mapping-файла из resources.
 */
public class MappingRulesLoaderTest {

    @Test
    void shouldLoadMappingRulesFromJsonFile() {
        Map<String, String> mapping = new MappingRulesLoader().load("src/main/resources/mapping-rules.json");

        assertFalse(mapping.isEmpty());
        assertEquals("payload.customer.country", mapping.get("country"));
    }
}
