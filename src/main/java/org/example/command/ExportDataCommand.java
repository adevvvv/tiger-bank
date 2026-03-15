package org.example.command;

import org.example.dto.DataExporter;
import org.example.dto.DataHandlerFactory;
import org.example.service.AccountManager;
import org.example.service.CategoryManager;
import org.example.service.OperationManager;

import java.util.Scanner;

public class ExportDataCommand implements Command {
    private final DataHandlerFactory dataHandlerFactory;
    private final AccountManager accountManager;
    private final CategoryManager categoryManager;
    private final OperationManager operationManager;
    private final Scanner scanner;

    public ExportDataCommand(DataHandlerFactory dataHandlerFactory,
                             AccountManager accountManager,
                             CategoryManager categoryManager,
                             OperationManager operationManager,
                             Scanner scanner) {
        this.dataHandlerFactory = dataHandlerFactory;
        this.accountManager = accountManager;
        this.categoryManager = categoryManager;
        this.operationManager = operationManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Экспорт данных ---");
        System.out.println("Поддерживаемые форматы: .json, .yaml, .yml, .csv");
        System.out.print("Введите имя файла для экспорта: ");
        String filename = scanner.nextLine();

        try {
            DataExporter exporter = dataHandlerFactory.getExporter(filename);
            exporter.export(
                    accountManager.getAllAccounts(),
                    categoryManager.getAllCategories(),
                    operationManager.getAllOperations(),
                    filename
            );
            System.out.println("✅ Экспорт в " + filename + " завершен успешно!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Ошибка при экспорте: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Экспорт данных";
    }
}