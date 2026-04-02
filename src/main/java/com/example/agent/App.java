package com.example.agent;

import com.example.agent.browser.BrowserSession;
import com.example.agent.browser.FormScanner;
import com.example.agent.browser.NetworkInterceptor;
import com.example.agent.config.AgentConfig;
import com.example.agent.generator.CombinationGenerator;
import com.example.agent.generator.JsonPayloadBuilder;
import com.example.agent.generator.MappingRulesLoader;
import com.example.agent.io.JsonFileWriter;
import com.example.agent.model.FormSchema;
import com.example.agent.model.GeneratedPayload;
import com.example.agent.model.NetworkCapture;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Главная точка входа.
 * Здесь удобно держать понятные логи, чтобы было видно,
 * на каком именно шаге приложение сейчас находится.
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Step 1: loading config");
        AgentConfig config = AgentConfig.load();

        System.out.println("Config loaded");
        System.out.println("URL: " + config.getUrl());
        System.out.println("Form selector: " + config.getFormSelector());
        System.out.println("Headless: " + config.isHeadless());
        System.out.println("Max cases: " + config.getMaxCases());
        System.out.println("Output dir: " + config.getOutputDir());

        try (BrowserSession browserSession = new BrowserSession(config.isHeadless())) {
            System.out.println("Step 2: browser started");

            Page page = browserSession.page();

            // Задаем таймауты на уровне страницы, чтобы приложение не ждало бесконечно.
            page.setDefaultTimeout(10_000);
            page.setDefaultNavigationTimeout(20_000);

            NetworkInterceptor interceptor = new NetworkInterceptor();
            if (config.isCaptureNetwork()) {
                System.out.println("Step 3: attaching network interceptor");
                interceptor.attach(page, config.getNetworkUrlContains());
            }

            System.out.println("Step 4: opening page");
            System.out.println("Navigating to: " + config.getUrl());

            page.navigate(config.getUrl());

            // DOMCONTENTLOADED обычно надежнее для отладки, чем более "тяжелые" состояния загрузки.
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            System.out.println("Page opened successfully");
            System.out.println("Actual URL after navigation: " + page.url());

            System.out.println("Step 5: scanning form");
            FormScanner scanner = new FormScanner();
            FormSchema schema = scanner.scan(
                page,
                config.getFormSelector(),
                config.isScanCustomDropdowns()
                                            );

            System.out.println("Form scan completed");
            System.out.println("Fields found: " + schema.getFields().size());
            System.out.println("Dependency snapshots found: " + schema.getDependencySnapshots().size());

            System.out.println("Step 6: generating combinations");
            CombinationGenerator generator = new CombinationGenerator();
            List<Map<String, Object>> combinations = generator.generate(schema, config.getMaxCases());
            System.out.println("Generated combinations: " + combinations.size());

            System.out.println("Step 7: loading mapping rules");
            MappingRulesLoader mappingRulesLoader = new MappingRulesLoader();
            Map<String, String> mappingRules = mappingRulesLoader.load(config.getMappingFile());
            System.out.println("Mapping rules loaded: " + mappingRules.size());

            System.out.println("Step 8: collecting captured network requests");
            List<NetworkCapture> networkCaptures = config.isCaptureNetwork()
                                                       ? interceptor.getRelevantCaptures(config.getNetworkUrlContains())
                                                       : List.of();
            System.out.println("Relevant network captures: " + networkCaptures.size());

            System.out.println("Step 9: building payloads");
            JsonPayloadBuilder payloadBuilder = new JsonPayloadBuilder();
            List<GeneratedPayload> payloads = new ArrayList<>();

            for (int i = 0; i < combinations.size(); i++) {
                Map<String, Object> flatValues = combinations.get(i);
                Map<String, Object> payload = payloadBuilder.build(flatValues, mappingRules);

                NetworkCapture capture = i < networkCaptures.size() ? networkCaptures.get(i) : null;

                payloads.add(new GeneratedPayload(
                    flatValues,
                    payload,
                    capture == null ? null : capture.getUrl(),
                    capture == null ? null : capture.getPostData()
                ));
            }

            System.out.println("Built payloads: " + payloads.size());

            System.out.println("Step 10: writing output files");
            JsonFileWriter writer = new JsonFileWriter();
            Path outputDir = Path.of(config.getOutputDir());

            writer.write(outputDir.resolve("form-schema.json"), schema);
            writer.write(outputDir.resolve("mapping-rules.json"), mappingRules);
            writer.write(outputDir.resolve("network-captures.json"), networkCaptures);
            writer.write(outputDir.resolve("generated-payloads.json"), payloads);

            System.out.println("Done");
            System.out.println("Files saved to: " + outputDir.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Application failed");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
