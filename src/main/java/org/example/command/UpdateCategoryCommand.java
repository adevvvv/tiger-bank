package org.example.command;

import org.example.model.Category;
import org.example.service.CategoryManager;

import java.util.Scanner;
import java.util.UUID;

public class UpdateCategoryCommand implements Command {
    private final CategoryManager categoryManager;
    private final Scanner scanner;

    public UpdateCategoryCommand(CategoryManager categoryManager, Scanner scanner) {
        this.categoryManager = categoryManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Доступные категории ---");
        categoryManager.getAllCategories().forEach(cat ->
                System.out.println("• " + cat.getId() + " | " + cat.getName() + " | " + cat.getType()));

        System.out.print("Введите ID категории для обновления: ");
        UUID id = UUID.fromString(scanner.nextLine());
        System.out.print("Введите новое название: ");
        String newName = scanner.nextLine();

        Category updated = categoryManager.updateCategoryName(id, newName);
        System.out.println("✅ Категория обновлена: " + updated.getName());
    }

    @Override
    public String getDescription() {
        return "Обновление категории";
    }
}