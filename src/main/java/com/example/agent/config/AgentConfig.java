package com.example.agent.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

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
    private final boolean scanDependencies;
    private final int maxDependencyPairs;
    private final Set<String> dependencyAllowedFields;
    private final String networkUrlContains;
    private final String mappingFile;

    public AgentConfig(String url,
                       String formSelector,
                       boolean headless,
                       int maxCases,
                       String outputDir,
                       boolean scanCustomDropdowns,
                       boolean captureNetwork,
                       boolean scanDependencies,
                       int maxDependencyPairs,
                       Set<String> dependencyAllowedFields,
                       String networkUrlContains,
                       String mappingFile) {
        this.url = url;
        this.formSelector = formSelector;
        this.headless = headless;
        this.maxCases = maxCases;
        this.outputDir = outputDir;
        this.scanCustomDropdowns = scanCustomDropdowns;
        this.captureNetwork = captureNetwork;
        this.scanDependencies = scanDependencies;
        this.maxDependencyPairs = maxDependencyPairs;
        this.dependencyAllowedFields = dependencyAllowedFields;
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
            Boolean.parseBoolean(properties.getProperty("agent.scanDependencies", "false")),
            Integer.parseInt(properties.getProperty("agent.maxDependencyPairs", "5")),
            parseCsvSet(properties.getProperty("agent.dependencyAllowedFields", "")),
            properties.getProperty("agent.networkUrlContains", "").trim(),
            properties.getProperty("agent.mappingFile", "src/main/resources/mapping-rules.json")
        );
    }

    private static Set<String> parseCsvSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(value.split(","))
                     .map(String::trim)
                     .filter(item -> !item.isBlank())
                     .collect(LinkedHashSet::new, Set::add, Set::addAll);
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

    public boolean isScanDependencies() {
        return scanDependencies;
    }

    public int getMaxDependencyPairs() {
        return maxDependencyPairs;
    }

    public Set<String> getDependencyAllowedFields() {
        return dependencyAllowedFields;
    }

    public String getNetworkUrlContains() {
        return networkUrlContains;
    }

    public String getMappingFile() {
        return mappingFile;
    }
}
