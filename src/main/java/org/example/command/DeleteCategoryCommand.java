package org.example.command;

import org.example.service.CategoryManager;
import org.example.service.OperationManager;

import java.util.Scanner;
import java.util.UUID;

public class DeleteCategoryCommand implements Command {
    private final CategoryManager categoryManager;
    private final OperationManager operationManager;
    private final Scanner scanner;

    public DeleteCategoryCommand(CategoryManager categoryManager,
                                 OperationManager operationManager,
                                 Scanner scanner) {
        this.categoryManager = categoryManager;
        this.operationManager = operationManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Доступные категории ---");
        categoryManager.getAllCategories().forEach(cat ->
                System.out.println("• " + cat.getId() + " | " + cat.getName() + " | " + cat.getType()));

        System.out.print("Введите ID категории для удаления: ");
        UUID id = UUID.fromString(scanner.nextLine());

        // Проверяем, есть ли операции с этой категорией
        long operationsCount = operationManager.getOperationsByCategoryId(id).size();
        if (operationsCount > 0) {
            System.out.print("⚠️ С категорией связано " + operationsCount + " операций. Удалить категорию? (y/n): ");
            String confirm = scanner.nextLine();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("❌ Удаление отменено");
                return;
            }
        }

        categoryManager.deleteCategory(id);
        System.out.println("✅ Категория удалена");
    }

    @Override
    public String getDescription() {
        return "Удаление категории";
    }
}