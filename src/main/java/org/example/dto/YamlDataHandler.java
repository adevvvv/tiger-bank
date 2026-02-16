package org.example.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Singleton;

@Singleton
public class YamlDataHandler extends AbstractDataHandler {

    private final ObjectMapper objectMapper;

    public YamlDataHandler() {
        // Создаем ObjectMapper с YAMLFactory
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    protected String getFileExtension() {
        return ".yaml";
    }
}