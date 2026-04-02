package com.example.agent;

import com.example.agent.browser.BrowserSession;
import com.example.agent.browser.FormScanner;
import com.example.agent.config.AgentConfig;
import com.example.agent.generator.CombinationGenerator;
import com.example.agent.generator.JsonPayloadBuilder;
import com.example.agent.io.JsonFileWriter;
import com.example.agent.model.FormSchema;
import com.example.agent.model.GeneratedPayload;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        AgentConfig config = AgentConfig.load();

        try (BrowserSession browserSession = new BrowserSession(config.isHeadless())) {
            browserSession.page().navigate(config.getUrl());
            browserSession.page().waitForLoadState();

            FormScanner scanner = new FormScanner();
            FormSchema schema = scanner.scan(browserSession.page(), config.getFormSelector());

            CombinationGenerator generator = new CombinationGenerator();
            List<Map<String, Object>> combinations = generator.generate(schema, config.getMaxCases());

            JsonPayloadBuilder payloadBuilder = new JsonPayloadBuilder();
            List<GeneratedPayload> payloads = new ArrayList<>();

            for (Map<String, Object> combination : combinations) {
                payloads.add(new GeneratedPayload(
                    combination,
                    payloadBuilder.build(combination)
                ));
            }

            JsonFileWriter writer = new JsonFileWriter();
            writer.write(Path.of(config.getOutputDir(), "form-schema.json"), schema);
            writer.write(Path.of(config.getOutputDir(), "generated-payloads.json"), payloads);
        }
    }
}
