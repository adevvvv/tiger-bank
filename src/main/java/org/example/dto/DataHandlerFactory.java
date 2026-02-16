package org.example.dto;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DataHandlerFactory {

    private final JsonDataHandler jsonHandler;
    private final YamlDataHandler yamlHandler;
    private final CsvDataHandler csvHandler;

    @Inject
    public DataHandlerFactory(JsonDataHandler jsonHandler,
                              YamlDataHandler yamlHandler,
                              CsvDataHandler csvHandler) {
        this.jsonHandler = jsonHandler;
        this.yamlHandler = yamlHandler;
        this.csvHandler = csvHandler;
    }

    public DataExporter getExporter(String filePath) {
        return (DataExporter) getHandler(filePath);
    }

    public DataImporter getImporter(String filePath) {
        return (DataImporter) getHandler(filePath);
    }

    private Object getHandler(String filePath) {
        String lowerCasePath = filePath.toLowerCase();
        if (lowerCasePath.endsWith(".json")) {
            return jsonHandler;
        } else if (lowerCasePath.endsWith(".yaml") || lowerCasePath.endsWith(".yml")) {
            return yamlHandler;
        } else if (lowerCasePath.endsWith(".csv")) {
            return csvHandler;
        } else {
            throw new IllegalArgumentException("Неподдерживаемый формат файла. Используйте .json, .yaml или .csv");
        }
    }
}