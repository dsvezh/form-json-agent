package com.example.agent.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Сохраняет любые Java-объекты в JSON-файлы.
 */
public class JsonFileWriter {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void write(Path path, Object data) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(path.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write json to " + path, e);
        }
    }
}
