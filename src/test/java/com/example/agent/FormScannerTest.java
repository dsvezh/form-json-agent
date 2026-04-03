package com.example.agent;

import com.example.agent.browser.BrowserSession;
import com.example.agent.browser.FormScanner;
import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FormSchema;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет, что сканер формы умеет читать базовые поля и кастомные dropdown.
 */
public class FormScannerTest {

    @Test
    void shouldScanSimpleHtmlForm() {
        try (BrowserSession session = new BrowserSession(true)) {
            Page page = session.page();
            page.setContent("""
                <html>
                  <body>
                    <form id="test-form">
                      <label for="country">Country</label>
                      <select id="country" name="country" required>
                        <option value="us">USA</option>
                        <option value="de">Germany</option>
                      </select>
                      <label for="email">Email</label>
                      <input id="email" name="email" type="email" />
                    </form>
                  </body>
                </html>
            """);

            FormSchema schema = new FormScanner().scan(page, "#test-form", false, false, 5, java.util.Set.of());

            assertNotNull(schema);
            assertEquals(2, schema.getFields().size());

            Optional<FieldDescriptor> country = schema.getFields().stream()
                    .filter(field -> field.getKey().equals("country"))
                    .findFirst();

            assertTrue(country.isPresent());
            assertEquals("Country", country.get().getLabel());
            assertEquals(2, country.get().getOptions().size());
        }
    }

    @Test
    void shouldScanCustomDropdown() {
        try (BrowserSession session = new BrowserSession(true)) {
            Page page = session.page();
            page.setContent("""
                <html>
                  <body>
                    <form id="test-form">
                      <div>
                        <label for="city-combobox">City</label>
                        <button id="city-combobox" role="combobox" aria-expanded="false" aria-controls="city-listbox" aria-label="City">
                          Select city
                        </button>
                        <ul id="city-listbox" role="listbox" style="display:block;">
                          <li role="option" data-value="ekb">Yekaterinburg</li>
                          <li role="option" data-value="msk">Moscow</li>
                        </ul>
                      </div>
                    </form>
                  </body>
                </html>
            """);

            FormSchema schema = new FormScanner().scan(page, "#test-form", true, false, 5, java.util.Set.of());

            Optional<FieldDescriptor> city = schema.getFields().stream()
                    .filter(field -> field.getKey().equals("city_combobox"))
                    .findFirst();

            assertTrue(city.isPresent());
            assertTrue(city.get().isCustomDropdown());
            assertFalse(city.get().getOptions().isEmpty());
        }
    }

    @Test
    void shouldScanNestedFieldsInsideContainers() {
        try (BrowserSession session = new BrowserSession(true)) {
            Page page = session.page();
            page.setContent("""
                <html>
                  <body>
                    <form id="test-form">
                      <div class="section">
                        <div class="row">
                          <input name="parent.child.name" type="text" />
                        </div>
                        <div class="row">
                          <textarea name="parent.child.description"></textarea>
                        </div>
                        <div class="row">
                          <div role="textbox" aria-label="Nested note"></div>
                        </div>
                      </div>
                    </form>
                  </body>
                </html>
            """);

            FormSchema schema = new FormScanner().scan(page, "#test-form", false, false, 5, java.util.Set.of());

            assertEquals(3, schema.getFields().size());
            assertTrue(schema.getFields().stream().anyMatch(field -> field.getKey().equals("parent.child.name")));
            assertTrue(schema.getFields().stream().anyMatch(field -> field.getKey().equals("parent.child.description")));
            assertTrue(schema.getFields().stream().anyMatch(field -> field.getLabel().equals("Nested note")));
        }
    }
}
