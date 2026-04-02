package com.example.agent;

import com.example.agent.browser.BrowserSession;
import com.example.agent.browser.FormScanner;
import com.example.agent.model.FormSchema;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

            FormScanner scanner = new FormScanner();
            FormSchema schema = scanner.scan(page, "#test-form");

            assertNotNull(schema);
            assertFalse(schema.getFields().isEmpty());
        }
    }
}
