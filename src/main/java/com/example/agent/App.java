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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Главная точка входа.
 * Порядок работы:
 * 1. загружаем конфиг,
 * 2. открываем браузер и страницу,
 * 3. сканируем форму,
 * 4. при необходимости слушаем сеть,
 * 5. строим комбинации,
 * 6. собираем итоговые JSON,
 * 7. сохраняем артефакты в outputs.
 */
public class App {
    public static void main(String[] args) {
        AgentConfig config = AgentConfig.load();

        try (BrowserSession browserSession = new BrowserSession(config.isHeadless())) {
            NetworkInterceptor interceptor = new NetworkInterceptor();
            if (config.isCaptureNetwork()) {
                interceptor.attach(browserSession.page(), config.getNetworkUrlContains());
            }

            browserSession.page().navigate(config.getUrl());
            browserSession.page().waitForLoadState();

            FormScanner scanner = new FormScanner();
            FormSchema schema = scanner.scan(
                    browserSession.page(),
                    config.getFormSelector(),
                    config.isScanCustomDropdowns(),
                    config.isScanDependencies(),
                    config.getMaxDependencyPairs(),
                    config.getDependencyAllowedFields()
            );

            CombinationGenerator generator = new CombinationGenerator();
            List<Map<String, Object>> combinations = generator.generate(schema, config.getMaxCases());

            MappingRulesLoader mappingRulesLoader = new MappingRulesLoader();
            Map<String, String> mappingRules = mappingRulesLoader.load(config.getMappingFile());

            List<NetworkCapture> networkCaptures = config.isCaptureNetwork()
                    ? interceptor.getRelevantCaptures(config.getNetworkUrlContains())
                    : List.of();

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

            JsonFileWriter writer = new JsonFileWriter();
            Path outputDir = Path.of(config.getOutputDir());

            writer.write(outputDir.resolve("form-schema.json"), schema);
            writer.write(outputDir.resolve("mapping-rules.json"), mappingRules);
            writer.write(outputDir.resolve("network-captures.json"), networkCaptures);
            writer.write(outputDir.resolve("generated-payloads.json"), payloads);
        }
    }
}
