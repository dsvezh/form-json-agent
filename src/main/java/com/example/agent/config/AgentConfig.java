package com.example.agent.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Хранит настройки приложения.
 * Этот класс читает application.properties и превращает его в обычный Java-объект,
 * чтобы дальше по коду не работать напрямую со строковыми ключами.
 */
public class AgentConfig {
    private final String url;
    private final String formSelector;
    private final boolean headless;
    private final int maxCases;
    private final String outputDir;
    private final boolean scanCustomDropdowns;
    private final boolean captureNetwork;
    private final String networkUrlContains;
    private final String mappingFile;

    public AgentConfig(String url,
                       String formSelector,
                       boolean headless,
                       int maxCases,
                       String outputDir,
                       boolean scanCustomDropdowns,
                       boolean captureNetwork,
                       String networkUrlContains,
                       String mappingFile) {
        this.url = url;
        this.formSelector = formSelector;
        this.headless = headless;
        this.maxCases = maxCases;
        this.outputDir = outputDir;
        this.scanCustomDropdowns = scanCustomDropdowns;
        this.captureNetwork = captureNetwork;
        this.networkUrlContains = networkUrlContains;
        this.mappingFile = mappingFile;
    }

    /**
     * Загружает настройки из resources/application.properties.
     */
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
            properties.getProperty("agent.outputDir", "outputs"),
            Boolean.parseBoolean(properties.getProperty("agent.scanCustomDropdowns", "true")),
            Boolean.parseBoolean(properties.getProperty("agent.captureNetwork", "true")),
            properties.getProperty("agent.networkUrlContains", "").trim(),
            properties.getProperty("agent.mappingFile", "src/main/resources/mapping-rules.json")
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

    public boolean isScanCustomDropdowns() {
        return scanCustomDropdowns;
    }

    public boolean isCaptureNetwork() {
        return captureNetwork;
    }

    public String getNetworkUrlContains() {
        return networkUrlContains;
    }

    public String getMappingFile() {
        return mappingFile;
    }
}
