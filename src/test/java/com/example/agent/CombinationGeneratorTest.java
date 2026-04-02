package com.example.agent;

import com.example.agent.generator.CombinationGenerator;
import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FieldOption;
import com.example.agent.model.FormSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CombinationGeneratorTest {

    @Test
    void shouldGenerateCombinationsForSelectFields() {
        FieldDescriptor country = new FieldDescriptor("country", "Country", "select", true);
        country.setOptions(List.of(
            new FieldOption("us", "USA"),
            new FieldOption("de", "Germany")
                                  ));

        FieldDescriptor email = new FieldDescriptor("email", "Email", "email", true);

        FormSchema schema = new FormSchema();
        schema.setFields(List.of(country, email));

        CombinationGenerator generator = new CombinationGenerator();
        List<Map<String, Object>> combinations = generator.generate(schema, 10);

        assertFalse(combinations.isEmpty());
    }
}
