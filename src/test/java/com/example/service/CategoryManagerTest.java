package com.example.service;

import org.example.model.Category;
import org.example.repository.CategoryRepository;
import org.example.service.CategoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты CategoryManager")
class CategoryManagerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryManager categoryManager;

    private UUID testId;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCategory = new Category(testId, Category.Type.EXPENSE, "Test Category");
    }

    @Nested
    @DisplayName("Тесты создания категории")
    class CreateCategoryTests {

        @Test
        @DisplayName("Создание категории с валидными данными должно возвращать созданную категорию")
        void createCategory_ValidData_ShouldReturnCategory() {
            // Arrange
            String name = "Food";
            Category.Type type = Category.Type.EXPENSE;

            when(categoryRepository.save(any(Category.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category category = categoryManager.createCategory(name, type);

            // Assert
            assertNotNull(category);
            assertNotNull(category.getId());
            assertEquals(name, category.getName());
            assertEquals(type, category.getType());
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Создание категории с пустым именем должно выбрасывать исключение")
        void createCategory_EmptyName_ShouldThrowException() {
            // Arrange
            String name = "   ";
            Category.Type type = Category.Type.EXPENSE;

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryManager.createCategory(name, type)
            );

            assertEquals("Category name cannot be empty", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание категории с null типом должно выбрасывать исключение")
        void createCategory_NullType_ShouldThrowException() {
            // Arrange
            String name = "Food";
            Category.Type type = null;

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryManager.createCategory(name, type)
            );

            assertEquals("Category type cannot be null", exception.getMessage());
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Тесты получения категорий по типу")
    class GetCategoriesByTypeTests {

        @Test
        @DisplayName("Получение категорий по типу должно возвращать отфильтрованный список")
        void getCategoriesByType_ShouldReturnFilteredList() {
            // Arrange
            List<Category> expenseCategories = Arrays.asList(
                    testCategory,
                    new Category(UUID.randomUUID(), Category.Type.EXPENSE, "Transport")
            );

            when(categoryRepository.findByType(Category.Type.EXPENSE)).thenReturn(expenseCategories);

            // Act
            List<Category> result = categoryManager.getCategoriesByType(Category.Type.EXPENSE);

            // Assert
            assertEquals(expenseCategories.size(), result.size());
            assertTrue(result.stream().allMatch(c -> c.getType() == Category.Type.EXPENSE));
            verify(categoryRepository, times(1)).findByType(Category.Type.EXPENSE);
        }
    }

    @Nested
    @DisplayName("Тесты обновления категории")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Обновление имени существующей категории должно возвращать обновленную категорию")
        void updateCategoryName_ExistingId_ShouldReturnUpdatedCategory() {
            // Arrange
            String newName = "Updated Category Name";
            when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
            doNothing().when(categoryRepository).update(any(Category.class));

            // Act
            Category updated = categoryManager.updateCategoryName(testId, newName);

            // Assert
            assertEquals(newName, updated.getName());
            assertEquals(testCategory.getType(), updated.getType());
            verify(categoryRepository, times(1)).findById(testId);
            verify(categoryRepository, times(1)).update(any(Category.class));
        }

        @Test
        @DisplayName("Обновление имени с пустым именем должно выбрасывать исключение")
        void updateCategoryName_EmptyName_ShouldThrowException() {
            // Arrange
            String newName = "   ";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> categoryManager.updateCategoryName(testId, newName)
            );

            assertEquals("Category name cannot be empty", exception.getMessage());
            verify(categoryRepository, never()).findById(any());
            verify(categoryRepository, never()).update(any());
        }
    }
}