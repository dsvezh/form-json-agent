package com.example.agent.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AgentConfig {
    private final String url;
    private final String formSelector;
    private final boolean headless;
    private final int maxCases;
    private final String outputDir;

    public AgentConfig(String url, String formSelector, boolean headless, int maxCases, String outputDir) {
        this.url = url;
        this.formSelector = formSelector;
        this.headless = headless;
        this.maxCases = maxCases;
        this.outputDir = outputDir;
    }

    public static AgentConfig load() {
        Properties properties = new Properties();

        try (InputStream input = AgentConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        return new AgentConfig(
            properties.getProperty("agent.url"),
            properties.getProperty("agent.formSelector", "form"),
            Boolean.parseBoolean(properties.getProperty("agent.headless", "true")),
            Integer.parseInt(properties.getProperty("agent.maxCases", "50")),
            properties.getProperty("agent.outputDir", "outputs")
        );
    }

    public String getUrl() {
        return url;
    }

    public String getFormSelector() {
        return formSelector;
    }

    public boolean isHeadless() {
        return headless;
    }

    public int getMaxCases() {
        return maxCases;
    }

    public String getOutputDir() {
        return outputDir;
    }
}
