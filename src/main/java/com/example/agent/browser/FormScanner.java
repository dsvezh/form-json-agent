package com.example.agent.browser;

import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FieldOption;
import com.example.agent.model.FormSchema;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

public class FormScanner {

    public FormSchema scan(Page page, String formSelector) {
        Locator form = page.locator(formSelector);
        form.waitFor();

        FormSchema schema = new FormSchema();
        schema.setUrl(page.url());

        List<FieldDescriptor> fields = new ArrayList<>();

        for (ElementHandle element : form.locator("input, select, textarea").elementHandles()) {
            String tag = stringValue(element.evaluate("el => el.tagName.toLowerCase()"));
            String name = attr(element, "name");
            String id = attr(element, "id");
            String key = firstNonBlank(name, id, "unnamed_field");
            String type = "input".equals(tag) ? firstNonBlank(attr(element, "type"), "text") : tag;
            boolean required = element.getAttribute("required") != null;
            String label = resolveLabel(page, id, name);

            FieldDescriptor descriptor = new FieldDescriptor(
                key,
                label,
                normalizeType(tag, type),
                required
            );

            if ("select".equals(tag)) {
                List<FieldOption> options = new ArrayList<>();
                for (ElementHandle option : element.querySelectorAll("option")) {
                    String value = firstNonBlank(option.getAttribute("value"), option.textContent());
                    String optionLabel = firstNonBlank(option.textContent(), value);

                    if (!optionLabel.isBlank()) {
                        options.add(new FieldOption(value.trim(), optionLabel.trim()));
                    }
                }
                descriptor.setOptions(options);
            }

            fields.add(descriptor);
        }

        schema.setFields(fields);
        return schema;
    }

    private String resolveLabel(Page page, String id, String name) {
        if (id != null && !id.isBlank()) {
            Locator label = page.locator("label[for='" + id + "']");
            if (label.count() > 0) {
                return label.first().innerText().trim();
            }
        }
        return firstNonBlank(name, "unknown");
    }

    private String attr(ElementHandle element, String name) {
        String value = element.getAttribute(name);
        return value == null ? "" : value.trim();
    }

    private String normalizeType(String tag, String type) {
        if ("select".equals(tag)) {
            return "select";
        }
        if ("textarea".equals(tag)) {
            return "textarea";
        }
        return type == null || type.isBlank() ? "text" : type.toLowerCase();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }
}
