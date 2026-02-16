package com.example.service;

import org.example.model.Category;
import org.example.model.Operation;
import org.example.repository.BankAccountRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.OperationRepository;
import org.example.service.AccountManager;
import org.example.service.OperationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты OperationManager")
class OperationManagerTest {

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private OperationManager operationManager;

    private UUID accountId;
    private UUID categoryId;
    private Category testCategory;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        testCategory = new Category(categoryId, Category.Type.EXPENSE, "Food");
        testDate = LocalDate.now();
    }

    @Nested
    @DisplayName("Тесты создания операции")
    class CreateOperationTests {

        @Test
        @DisplayName("Создание операции расхода с валидными данными должно возвращать созданную операцию")
        void createOperation_Expense_ValidData_ShouldReturnOperation() {
            // Arrange
            Operation.Type type = Operation.Type.EXPENSE;
            BigDecimal amount = new BigDecimal("100.00");
            String description = "Lunch";

            when(accountRepository.exists(accountId)).thenReturn(true);
            when(categoryRepository.exists(categoryId)).thenReturn(true);
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(operationRepository.save(any(Operation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(accountManager).updateBalance(eq(accountId), any(BigDecimal.class));

            // Act
            Operation operation = operationManager.createOperation(
                    type, accountId, amount, testDate, description, categoryId
            );

            // Assert
            assertNotNull(operation);
            assertNotNull(operation.getId());
            assertEquals(type, operation.getType());
            assertEquals(accountId, operation.getBankAccountId());
            assertEquals(amount, operation.getAmount());
            assertEquals(testDate, operation.getDate());
            assertEquals(description, operation.getDescription());
            assertEquals(categoryId, operation.getCategoryId());

            verify(accountManager, times(1)).updateBalance(eq(accountId), eq(amount.negate()));
            verify(operationRepository, times(1)).save(any(Operation.class));
        }

        @Test
        @DisplayName("Создание операции дохода с валидными данными должно возвращать созданную операцию")
        void createOperation_Income_ValidData_ShouldReturnOperation() {
            // Arrange
            Operation.Type type = Operation.Type.INCOME;
            BigDecimal amount = new BigDecimal("1000.00");
            String description = "Salary";
            Category incomeCategory = new Category(UUID.randomUUID(), Category.Type.INCOME, "Salary");
            UUID incomeCategoryId = incomeCategory.getId();

            when(accountRepository.exists(accountId)).thenReturn(true);
            when(categoryRepository.exists(incomeCategoryId)).thenReturn(true);
            when(categoryRepository.findById(incomeCategoryId)).thenReturn(Optional.of(incomeCategory));
            when(operationRepository.save(any(Operation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(accountManager).updateBalance(eq(accountId), any(BigDecimal.class));

            // Act
            Operation operation = operationManager.createOperation(
                    type, accountId, amount, testDate, description, incomeCategoryId
            );

            // Assert
            assertNotNull(operation);
            assertEquals(type, operation.getType());
            verify(accountManager, times(1)).updateBalance(eq(accountId), eq(amount));
            verify(operationRepository, times(1)).save(any(Operation.class));
        }

        @Test
        @DisplayName("Создание операции с несуществующим счетом должно выбрасывать исключение")
        void createOperation_NonExistingAccount_ShouldThrowException() {
            // Arrange
            when(accountRepository.exists(accountId)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> operationManager.createOperation(
                            Operation.Type.EXPENSE, accountId, new BigDecimal("100.00"),
                            testDate, "Test", categoryId
                    )
            );

            assertEquals("Bank account not found", exception.getMessage());
            verify(operationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание операции с несуществующей категорией должно выбрасывать исключение")
        void createOperation_NonExistingCategory_ShouldThrowException() {
            // Arrange
            when(accountRepository.exists(accountId)).thenReturn(true);
            when(categoryRepository.exists(categoryId)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> operationManager.createOperation(
                            Operation.Type.EXPENSE, accountId, new BigDecimal("100.00"),
                            testDate, "Test", categoryId
                    )
            );

            assertEquals("Category not found", exception.getMessage());
            verify(operationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание операции с отрицательной суммой должно выбрасывать исключение")
        void createOperation_NegativeAmount_ShouldThrowException() {
            // Arrange
            BigDecimal amount = new BigDecimal("-100.00");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> operationManager.createOperation(
                            Operation.Type.EXPENSE, accountId, amount,
                            testDate, "Test", categoryId
                    )
            );

            assertEquals("Amount must be positive", exception.getMessage());
            verify(operationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание операции с несоответствием типа категории должно выбрасывать исключение")
        void createOperation_CategoryTypeMismatch_ShouldThrowException() {
            // Arrange
            Category incomeCategory = new Category(categoryId, Category.Type.INCOME, "Salary");

            when(accountRepository.exists(accountId)).thenReturn(true);
            when(categoryRepository.exists(categoryId)).thenReturn(true);
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(incomeCategory));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> operationManager.createOperation(
                            Operation.Type.EXPENSE, accountId, new BigDecimal("100.00"),
                            testDate, "Test", categoryId
                    )
            );

            assertEquals("Category type does not match operation type", exception.getMessage());
            verify(operationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Тесты удаления операции")
    class DeleteOperationTests {

        @Test
        @DisplayName("Удаление операции должно откатывать баланс и удалять операцию")
        void deleteOperation_ShouldRollbackBalanceAndDelete() {
            // Arrange
            UUID operationId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");
            Operation operation = new Operation(
                    operationId, Operation.Type.EXPENSE, accountId, amount,
                    testDate, "Test", categoryId
            );

            when(operationRepository.findById(operationId)).thenReturn(Optional.of(operation));
            doNothing().when(accountManager).updateBalance(eq(accountId), eq(amount));
            doNothing().when(operationRepository).delete(operationId);

            // Act
            operationManager.deleteOperation(operationId);

            // Assert
            verify(accountManager, times(1)).updateBalance(accountId, amount);
            verify(operationRepository, times(1)).delete(operationId);
        }

        @Test
        @DisplayName("Удаление несуществующей операции должно выбрасывать исключение")
        void deleteOperation_NonExisting_ShouldThrowException() {
            // Arrange
            UUID operationId = UUID.randomUUID();
            when(operationRepository.findById(operationId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> operationManager.deleteOperation(operationId)
            );

            assertTrue(exception.getMessage().contains("Operation not found"));
            verify(operationRepository, never()).delete(any());
        }
    }
}