package org.example.command;

import org.example.model.Category;
import org.example.service.CategoryManager;

import java.util.Scanner;

public class CreateCategoryCommand implements Command {
    private final CategoryManager categoryManager;
    private final Scanner scanner;

    public CreateCategoryCommand(CategoryManager categoryManager, Scanner scanner) {
        this.categoryManager = categoryManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.print("Введите название категории: ");
        String name = scanner.nextLine();
        System.out.print("Введите тип (INCOME/EXPENSE): ");
        String typeStr = scanner.nextLine().toUpperCase();
        Category.Type type = Category.Type.valueOf(typeStr);

        Category category = categoryManager.createCategory(name, type);
        System.out.println("✅ Категория создана: " + category.getName() + " (ID: " + category.getId() + ")");
    }

    @Override
    public String getDescription() {
        return "Создание категории";
    }
}