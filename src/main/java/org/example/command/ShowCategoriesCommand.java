package org.example.command;

import org.example.service.CategoryManager;

public class ShowCategoriesCommand implements Command {
    private final CategoryManager categoryManager;

    public ShowCategoriesCommand(CategoryManager categoryManager) {
        this.categoryManager = categoryManager;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Категории ---");
        var categories = categoryManager.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("Категории отсутствуют");
        } else {
            categories.forEach(cat ->
                    System.out.println("• " + cat.getId() + " | " + cat.getName() + " | " + cat.getType()));
        }
    }

    @Override
    public String getDescription() {
        return "Просмотр категорий";
    }
}