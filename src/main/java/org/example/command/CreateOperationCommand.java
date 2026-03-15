package org.example.command;

import org.example.model.Operation;
import org.example.service.OperationManager;
import org.example.service.AccountManager;
import org.example.service.CategoryManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class CreateOperationCommand implements Command {
    private final OperationManager operationManager;
    private final AccountManager accountManager;
    private final CategoryManager categoryManager;
    private final Scanner scanner;

    public CreateOperationCommand(OperationManager operationManager,
                                  AccountManager accountManager,
                                  CategoryManager categoryManager,
                                  Scanner scanner) {
        this.operationManager = operationManager;
        this.accountManager = accountManager;
        this.categoryManager = categoryManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        try {
            System.out.print("Введите тип операции (INCOME/EXPENSE): ");
            String typeStr = scanner.nextLine().toUpperCase();
            Operation.Type type = Operation.Type.valueOf(typeStr);

            System.out.println("\n--- Доступные счета ---");
            accountManager.getAllAccounts().forEach(acc ->
                    System.out.println("• " + acc.getId() + " | " + acc.getName() + " | " + acc.getBalance() + " руб."));

            System.out.print("Введите ID счета: ");
            UUID accountId = UUID.fromString(scanner.nextLine());

            System.out.print("Введите сумму: ");
            BigDecimal amount = scanner.nextBigDecimal();
            scanner.nextLine();

            System.out.print("Введите дату (ГГГГ-ММ-ДД) или Enter для текущей даты: ");
            String dateStr = scanner.nextLine();
            LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

            System.out.print("Введите описание: ");
            String description = scanner.nextLine();

            System.out.println("\n--- Доступные категории ---");
            categoryManager.getAllCategories().forEach(cat ->
                    System.out.println("• " + cat.getId() + " | " + cat.getName() + " | " + cat.getType()));

            System.out.print("Введите ID категории: ");
            UUID categoryId = UUID.fromString(scanner.nextLine());

            Operation operation = operationManager.createOperation(
                    type, accountId, amount, date, description, categoryId
            );
            System.out.println("✅ Операция создана (ID: " + operation.getId() + ")");

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Создание операции";
    }
}