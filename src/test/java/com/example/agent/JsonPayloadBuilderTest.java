package com.example.agent;

import com.example.agent.generator.JsonPayloadBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonPayloadBuilderTest {

    @Test
    void shouldWrapFlatValuesIntoPayloadObject() {
        JsonPayloadBuilder builder = new JsonPayloadBuilder();

        Map<String, Object> flat = Map.of(
            "country", "us",
            "email", "test@example.com"
                                         );

        Map<String, Object> result = builder.build(flat);

        assertTrue(result.containsKey("payload"));
        Map<?, ?> payload = (Map<?, ?>) result.get("payload");
        assertEquals("us", payload.get("country"));
    }
}
