package com.example.agent;

import com.example.agent.generator.CombinationGenerator;
import com.example.agent.model.FieldDependencySnapshot;
import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FieldOption;
import com.example.agent.model.FormSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет, что генератор учитывает зависимые dropdown.
 */
public class CombinationGeneratorTest {

    @Test
    void shouldGenerateCombinationsForDependentSelectFields() {
        FieldDescriptor country = new FieldDescriptor("country", "Country", "select", true);
        country.setOptions(List.of(
                new FieldOption("us", "USA"),
                new FieldOption("de", "Germany")
        ));

        FieldDescriptor state = new FieldDescriptor("state", "State", "select", true);
        state.setDependsOn("country");
        state.setOptions(List.of(
                new FieldOption("ca", "California"),
                new FieldOption("bw", "Baden-Wurttemberg")
        ));

        FieldDependencySnapshot usSnapshot = new FieldDependencySnapshot();
        usSnapshot.setParentField("country");
        usSnapshot.setParentValue("us");
        usSnapshot.setChildField("state");
        usSnapshot.setOptions(List.of(new FieldOption("ca", "California")));

        FieldDependencySnapshot deSnapshot = new FieldDependencySnapshot();
        deSnapshot.setParentField("country");
        deSnapshot.setParentValue("de");
        deSnapshot.setChildField("state");
        deSnapshot.setOptions(List.of(new FieldOption("bw", "Baden-Wurttemberg")));

        FormSchema schema = new FormSchema();
        schema.setFields(List.of(country, state));
        schema.setDependencySnapshots(List.of(usSnapshot, deSnapshot));

        List<Map<String, Object>> combinations = new CombinationGenerator().generate(schema, 10);

        assertEquals(2, combinations.size());
        assertTrue(combinations.stream().anyMatch(item -> "us".equals(item.get("country")) && "ca".equals(item.get("state"))));
        assertTrue(combinations.stream().anyMatch(item -> "de".equals(item.get("country")) && "bw".equals(item.get("state"))));
    }

    @Test
    void shouldGenerateTextValuesForNonSelectFields() {
        FieldDescriptor email = new FieldDescriptor("email", "Email", "email", true);

        FormSchema schema = new FormSchema();
        schema.setFields(List.of(email));

        List<Map<String, Object>> combinations = new CombinationGenerator().generate(schema, 10);

        assertFalse(combinations.isEmpty());
        assertEquals("test@example.com", combinations.get(0).get("email"));
    }
}
