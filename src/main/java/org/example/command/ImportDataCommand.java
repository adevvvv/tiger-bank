package org.example.command;

import org.example.dto.DataImporter;
import org.example.dto.DataHandlerFactory;
import org.example.dto.ImportResult;

import java.util.Scanner;

public class ImportDataCommand implements Command {
    private final DataHandlerFactory dataHandlerFactory;
    private final Scanner scanner;

    public ImportDataCommand(DataHandlerFactory dataHandlerFactory, Scanner scanner) {
        this.dataHandlerFactory = dataHandlerFactory;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Импорт данных ---");
        System.out.println("Поддерживаемые форматы: .json, .yaml, .yml, .csv");
        System.out.print("Введите имя файла для импорта: ");
        String filename = scanner.nextLine();

        try {
            DataImporter importer = dataHandlerFactory.getImporter(filename);
            ImportResult result = importer.importData(filename);
            System.out.println("Результат импорта: " + result);

            if (!result.hasErrors()) {
                System.out.println("✅ Импорт из " + filename + " завершен успешно!");
            } else {
                System.out.println("⚠️ Импорт завершен с ошибками:");
                result.getErrors().forEach(err -> System.out.println("  • " + err));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Ошибка при импорте: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Импорт данных";
    }
}