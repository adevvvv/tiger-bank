package org.example.service;

import com.google.inject.Inject;
import org.example.model.Category;
import org.example.repository.CategoryRepository;
import org.example.repository.OperationRepository;

import java.util.List;
import java.util.UUID;

public class CategoryManager {

    private final CategoryRepository categoryRepository;
    private final OperationRepository operationRepository;

    @Inject
    public CategoryManager(CategoryRepository categoryRepository,
                           OperationRepository operationRepository) {
        this.categoryRepository = categoryRepository;
        this.operationRepository = operationRepository;
    }

    public Category createCategory(String name, Category.Type type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Category type cannot be null");
        }

        Category category = new Category(
                UUID.randomUUID(),
                type,
                name.trim()
        );

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getCategoriesByType(Category.Type type) {
        return categoryRepository.findByType(type);
    }

    public Category updateCategoryName(UUID id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        category.setName(newName.trim());
        categoryRepository.update(category);
        return category;
    }

    public void deleteCategory(UUID id) {
        if (!categoryRepository.exists(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }

        // Проверяем, есть ли операции с этой категорией
        var operations = operationRepository.findByCategoryId(id);
        if (!operations.isEmpty()) {
            // В реальном приложении здесь может быть логика переназначения категорий
            // или запрет на удаление. Пока просто предупреждаем, но удаляем.
            System.out.println("⚠️ Внимание: удаляется категория, используемая в " +
                    operations.size() + " операциях");
        }

        categoryRepository.delete(id);
    }
}