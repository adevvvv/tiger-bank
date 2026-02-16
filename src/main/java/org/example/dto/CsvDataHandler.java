package org.example.dto;

import com.google.inject.Singleton;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Singleton
public class CsvDataHandler implements DataExporter, DataImporter {

    @Override
    public void export(List<BankAccount> accounts,
                       List<Category> categories,
                       List<Operation> operations,
                       String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {

            // Заголовки
            writer.writeNext(new String[]{"TYPE", "ID", "NAME", "BALANCE", "CATEGORY_TYPE", "AMOUNT",
                    "DATE", "DESCRIPTION", "ACCOUNT_ID", "CATEGORY_ID"});

            // Экспорт счетов
            for (BankAccount account : accounts) {
                writer.writeNext(new String[]{
                        "ACCOUNT",
                        account.getId().toString(),
                        account.getName(),
                        account.getBalance().toString(),
                        "", "", "", "", "", ""
                });
            }

            // Экспорт категорий
            for (Category category : categories) {
                writer.writeNext(new String[]{
                        "CATEGORY",
                        category.getId().toString(),
                        category.getName(),
                        "",
                        category.getType().toString(),
                        "", "", "", "", ""
                });
            }

            // Экспорт операций
            for (Operation operation : operations) {
                String operationType = operation.getType() == Operation.Type.INCOME ? "INCOME" : "EXPENSE";
                writer.writeNext(new String[]{
                        "OPERATION",
                        operation.getId().toString(),
                        "",
                        "",
                        operationType,
                        operation.getAmount().toString(),
                        operation.getDate().toString(),
                        operation.getDescription(),
                        operation.getBankAccountId().toString(),
                        operation.getCategoryId().toString()
                });
            }

            System.out.println("✅ Данные успешно экспортированы в CSV файл: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при экспорте данных в CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public ImportResult importData(String filePath) {
        ImportResult result = new ImportResult();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> lines = reader.readAll();

            // Пропускаем заголовок
            boolean firstLine = true;

            for (String[] line : lines) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                try {
                    String type = line[0];

                    switch (type) {
                        case "ACCOUNT":
                            BankAccount account = new BankAccount(
                                    UUID.fromString(line[1]),
                                    line[2],
                                    new BigDecimal(line[3])
                            );
                            result.addAccount(account);
                            break;

                        case "CATEGORY":
                            Category category = new Category(
                                    UUID.fromString(line[1]),
                                    Category.Type.valueOf(line[4]),
                                    line[2]
                            );
                            result.addCategory(category);
                            break;

                        case "OPERATION":
                            Operation.Type opType = Operation.Type.valueOf(line[4]);
                            Operation operation = new Operation(
                                    UUID.fromString(line[1]),
                                    opType,
                                    UUID.fromString(line[8]),
                                    new BigDecimal(line[5]),
                                    LocalDate.parse(line[6]),
                                    line[7],
                                    UUID.fromString(line[9])
                            );
                            result.addOperation(operation);
                            break;
                    }
                } catch (Exception e) {
                    result.addError("Ошибка при импорте строки: " + Arrays.toString(line) + " - " + e.getMessage());
                }
            }

            System.out.println("✅ Данные успешно импортированы из CSV файла: " + filePath);

        } catch (IOException | CsvException e) {
            result.addError("Ошибка при чтении CSV файла: " + e.getMessage());
        }

        return result;
    }
}