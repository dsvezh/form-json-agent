package com.example.agent;

import com.example.agent.generator.JsonPayloadBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет, что плоские значения корректно превращаются во вложенный JSON.
 */
public class JsonPayloadBuilderTest {

    @Test
    void shouldBuildNestedPayloadUsingMappingRules() {
        JsonPayloadBuilder builder = new JsonPayloadBuilder();

        Map<String, Object> flatValues = Map.of(
                "country", "us",
                "amount", 100
        );

        Map<String, String> mapping = Map.of(
                "country", "payload.customer.country",
                "amount", "payload.payment.amount"
        );

        Map<String, Object> result = builder.build(flatValues, mapping);

        assertTrue(result.containsKey("payload"));

        Map<?, ?> payload = (Map<?, ?>) result.get("payload");
        Map<?, ?> customer = (Map<?, ?>) payload.get("customer");
        Map<?, ?> payment = (Map<?, ?>) payload.get("payment");

        assertEquals("us", customer.get("country"));
        assertEquals(100, payment.get("amount"));
    }
}
