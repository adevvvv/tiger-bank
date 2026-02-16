package org.example.dto;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class JsonDataHandler implements DataExporter, DataImporter {

    private final ObjectMapper objectMapper;

    @Inject
    public JsonDataHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void export(List<BankAccount> accounts,
                       List<Category> categories,
                       List<Operation> operations,
                       String filePath) {
        try {
            // Конвертируем в DTO
            List<DataContainer.AccountDto> accountDtos = accounts.stream()
                    .map(DataContainer.AccountDto::new)
                    .collect(Collectors.toList());

            List<DataContainer.CategoryDto> categoryDtos = categories.stream()
                    .map(DataContainer.CategoryDto::new)
                    .collect(Collectors.toList());

            List<DataContainer.OperationDto> operationDtos = operations.stream()
                    .map(DataContainer.OperationDto::new)
                    .collect(Collectors.toList());

            DataContainer container = new DataContainer(accountDtos, categoryDtos, operationDtos);

            // Записываем в файл
            objectMapper.writeValue(Paths.get(filePath).toFile(), container);

            System.out.println("✅ Данные успешно экспортированы в файл: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при экспорте данных в JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public ImportResult importData(String filePath) {
        ImportResult result = new ImportResult();

        try {
            File file = Paths.get(filePath).toFile();
            if (!file.exists()) {
                result.addError("Файл не найден: " + filePath);
                return result;
            }

            DataContainer container = objectMapper.readValue(file, DataContainer.class);

            // Конвертируем обратно в доменные объекты
            if (container.accounts() != null) {
                container.accounts().forEach(dto -> {
                    try {
                        BankAccount account = new BankAccount(
                                dto.getId(),
                                dto.getName(),
                                dto.getBalance()
                        );
                        result.addAccount(account);
                    } catch (Exception e) {
                        result.addError("Ошибка при импорте счета: " + e.getMessage());
                    }
                });
            }

            if (container.categories() != null) {
                container.categories().forEach(dto -> {
                    try {
                        Category category = new Category(
                                dto.getId(),
                                dto.getType(),
                                dto.getName()
                        );
                        result.addCategory(category);
                    } catch (Exception e) {
                        result.addError("Ошибка при импорте категории: " + e.getMessage());
                    }
                });
            }

            if (container.operations() != null) {
                container.operations().forEach(dto -> {
                    try {
                        Operation operation = new Operation(
                                dto.getId(),
                                dto.getType(),
                                dto.getBankAccountId(),
                                dto.getAmount(),
                                dto.getDate(),
                                dto.getDescription(),
                                dto.getCategoryId()
                        );
                        result.addOperation(operation);
                    } catch (Exception e) {
                        result.addError("Ошибка при импорте операции: " + e.getMessage());
                    }
                });
            }

            System.out.println("✅ Данные успешно импортированы из файла: " + filePath);

        } catch (IOException e) {
            result.addError("Ошибка при чтении файла: " + e.getMessage());
        }

        return result;
    }
}