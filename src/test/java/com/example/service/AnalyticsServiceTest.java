package com.example.service;

import org.example.model.Category;
import org.example.model.Operation;
import org.example.repository.CategoryRepository;
import org.example.repository.OperationRepository;
import org.example.service.AnalyticsService;
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
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты AnalyticsService")
class AnalyticsServiceTest {

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private LocalDate startDate;
    private LocalDate endDate;
    private UUID accountId;
    private Category foodCategory;
    private Category salaryCategory;
    private Category transportCategory;
    private Category entertainmentCategory;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);
        accountId = UUID.randomUUID();

        foodCategory = new Category(UUID.randomUUID(), Category.Type.EXPENSE, "Food");
        salaryCategory = new Category(UUID.randomUUID(), Category.Type.INCOME, "Salary");
        transportCategory = new Category(UUID.randomUUID(), Category.Type.EXPENSE, "Transport");
        entertainmentCategory = new Category(UUID.randomUUID(), Category.Type.EXPENSE, "Entertainment");
    }

    private Operation createOperation(Operation.Type type, BigDecimal amount, UUID categoryId) {
        return new Operation(
                UUID.randomUUID(),
                type,
                accountId,
                amount,
                LocalDate.now(),
                "Test operation",
                categoryId
        );
    }

    @Nested
    @DisplayName("Тесты расчета разницы доходов и расходов")
    class CalculateDifferenceTests {

        @Test
        @DisplayName("Расчет разницы при наличии доходов и расходов должен возвращать корректное значение")
        void calculateDifference_MixedOperations_ShouldReturnCorrectDifference() {
            // Arrange
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.INCOME, new BigDecimal("5000.00"), salaryCategory.getId()),
                    createOperation(Operation.Type.INCOME, new BigDecimal("1000.00"), salaryCategory.getId()),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodCategory.getId()),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("500.00"), transportCategory.getId())
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("3500.00"), difference);
        }

        @Test
        @DisplayName("Расчет разницы при отсутствии операций должен возвращать ноль")
        void calculateDifference_NoOperations_ShouldReturnZero() {
            // Arrange
            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

            // Act
            BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(startDate, endDate);

            // Assert
            assertEquals(BigDecimal.ZERO, difference);
        }

        @Test
        @DisplayName("Расчет разницы только с доходами должен возвращать сумму доходов")
        void calculateDifference_OnlyIncome_ShouldReturnTotalIncome() {
            // Arrange
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.INCOME, new BigDecimal("5000.00"), salaryCategory.getId()),
                    createOperation(Operation.Type.INCOME, new BigDecimal("3000.00"), salaryCategory.getId())
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("8000.00"), difference);
        }

        @Test
        @DisplayName("Расчет разницы только с расходами должен возвращать отрицательную сумму")
        void calculateDifference_OnlyExpense_ShouldReturnNegativeTotal() {
            // Arrange
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodCategory.getId()),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1500.00"), foodCategory.getId())
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("-3500.00"), difference);
        }
    }

    @Nested
    @DisplayName("Тесты группировки по категориям")
    class GroupByCategoryTests {

        @Test
        @DisplayName("Группировка расходов по категориям должна возвращать корректные суммы")
        void groupExpenseByCategory_ShouldReturnCorrectSums() {
            // Arrange
            UUID foodId = foodCategory.getId();
            UUID transportId = transportCategory.getId();

            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("500.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("300.00"), transportId),
                    createOperation(Operation.Type.INCOME, new BigDecimal("5000.00"), salaryCategory.getId()) // Should be filtered out
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);
            when(categoryRepository.findById(foodId)).thenReturn(Optional.of(foodCategory));
            when(categoryRepository.findById(transportId)).thenReturn(Optional.of(transportCategory));

            // Act
            Map<Category, BigDecimal> result = analyticsService.groupExpenseByCategory(startDate, endDate);

            // Assert
            assertEquals(2, result.size());
            assertEquals(new BigDecimal("1500.00"), result.get(foodCategory));
            assertEquals(new BigDecimal("300.00"), result.get(transportCategory));
        }

        @Test
        @DisplayName("Группировка доходов по категориям должна возвращать корректные суммы")
        void groupIncomeByCategory_ShouldReturnCorrectSums() {
            // Arrange
            UUID salaryId = salaryCategory.getId();

            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.INCOME, new BigDecimal("50000.00"), salaryId),
                    createOperation(Operation.Type.INCOME, new BigDecimal("10000.00"), salaryId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodCategory.getId()) // Should be filtered out
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);
            when(categoryRepository.findById(salaryId)).thenReturn(Optional.of(salaryCategory));

            // Act
            Map<Category, BigDecimal> result = analyticsService.groupIncomeByCategory(startDate, endDate);

            // Assert
            assertEquals(1, result.size());
            assertEquals(new BigDecimal("60000.00"), result.get(salaryCategory));
        }

        @Test
        @DisplayName("Группировка при отсутствии операций должна возвращать пустую карту")
        void groupByCategory_NoOperations_ShouldReturnEmptyMap() {
            // Arrange
            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

            // Act
            Map<Category, BigDecimal> result = analyticsService.groupExpenseByCategory(startDate, endDate);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Группировка при отсутствии категорий должна возвращать пустую карту")
        void groupByCategory_CategoriesNotFound_ShouldReturnEmptyMap() {
            // Arrange
            UUID foodId = foodCategory.getId();
            UUID transportId = transportCategory.getId();

            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("500.00"), transportId)
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);
            when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // Act
            Map<Category, BigDecimal> result = analyticsService.groupExpenseByCategory(startDate, endDate);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тесты получения топ категорий")
    class TopCategoriesTests {

        @Test
        @DisplayName("Получение топ категорий должно возвращать отсортированный список")
        void getTopExpenseCategories_ShouldReturnSortedList() {
            // Arrange
            UUID foodId = foodCategory.getId();
            UUID transportId = transportCategory.getId();
            UUID entertainmentId = entertainmentCategory.getId();

            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("5000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("3000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), transportId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("4000.00"), entertainmentId)
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);
            when(categoryRepository.findById(foodId)).thenReturn(Optional.of(foodCategory));
            when(categoryRepository.findById(transportId)).thenReturn(Optional.of(transportCategory));
            when(categoryRepository.findById(entertainmentId)).thenReturn(Optional.of(entertainmentCategory));

            // Act
            List<Map.Entry<Category, BigDecimal>> top2 =
                    analyticsService.getTopExpenseCategories(startDate, endDate, 2);

            // Assert
            assertEquals(2, top2.size());
            assertEquals(foodCategory, top2.get(0).getKey());
            assertEquals(new BigDecimal("8000.00"), top2.get(0).getValue());
            assertEquals(entertainmentCategory, top2.get(1).getKey());
            assertEquals(new BigDecimal("4000.00"), top2.get(1).getValue());
        }

        @Test
        @DisplayName("Получение топ категорий с лимитом больше чем категорий должно возвращать все категории")
        void getTopExpenseCategories_LimitGreaterThanCategories_ShouldReturnAll() {
            // Arrange
            UUID foodId = foodCategory.getId();
            UUID transportId = transportCategory.getId();

            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("5000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), transportId)
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);
            when(categoryRepository.findById(foodId)).thenReturn(Optional.of(foodCategory));
            when(categoryRepository.findById(transportId)).thenReturn(Optional.of(transportCategory));

            // Act
            List<Map.Entry<Category, BigDecimal>> top5 =
                    analyticsService.getTopExpenseCategories(startDate, endDate, 5);

            // Assert
            assertEquals(2, top5.size());
            assertEquals(foodCategory, top5.get(0).getKey());
            assertEquals(new BigDecimal("5000.00"), top5.get(0).getValue());
            assertEquals(transportCategory, top5.get(1).getKey());
            assertEquals(new BigDecimal("2000.00"), top5.get(1).getValue());
        }

        @Test
        @DisplayName("Получение топ категорий при отсутствии операций должно возвращать пустой список")
        void getTopExpenseCategories_NoOperations_ShouldReturnEmptyList() {
            // Arrange
            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

            // Act
            List<Map.Entry<Category, BigDecimal>> result =
                    analyticsService.getTopExpenseCategories(startDate, endDate, 3);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тесты расчета среднего чека")
    class AverageAmountTests {

        @Test
        @DisplayName("Расчет среднего чека по категории должен возвращать корректное значение")
        void getAverageAmountByCategory_ShouldReturnCorrectAverage() {
            // Arrange
            UUID foodId = foodCategory.getId();
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("3000.00"), foodId)
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal average = analyticsService.getAverageAmountByCategory(foodId, startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("2000.00"), average);
        }

        @Test
        @DisplayName("Расчет среднего чека при отсутствии операций должен возвращать ноль")
        void getAverageAmountByCategory_NoOperations_ShouldReturnZero() {
            // Arrange
            UUID foodId = foodCategory.getId();
            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

            // Act
            BigDecimal average = analyticsService.getAverageAmountByCategory(foodId, startDate, endDate);

            // Assert
            assertEquals(BigDecimal.ZERO, average);
        }

        @Test
        @DisplayName("Расчет среднего чека с одной операцией должен возвращать сумму операции")
        void getAverageAmountByCategory_SingleOperation_ShouldReturnAmount() {
            // Arrange
            UUID foodId = foodCategory.getId();
            List<Operation> operations = Collections.singletonList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1500.00"), foodId)
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal average = analyticsService.getAverageAmountByCategory(foodId, startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("1500.00"), average);
        }

        @Test
        @DisplayName("Расчет среднего чека с нецелым средним должен корректно округляться")
        void getAverageAmountByCategory_NonIntegerAverage_ShouldRoundCorrectly() {
            // Arrange
            UUID foodId = foodCategory.getId();
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("3000.00"), foodId),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("500.00"), foodId)
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal average = analyticsService.getAverageAmountByCategory(foodId, startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("1625.00"), average); // (1000+2000+3000+500)/4 = 1625
        }
    }

    @Nested
    @DisplayName("Тесты получения общих сумм")
    class TotalSumsTests {

        @Test
        @DisplayName("Получение общей суммы доходов должно возвращать корректное значение")
        void getTotalIncome_ShouldReturnCorrectSum() {
            // Arrange
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.INCOME, new BigDecimal("5000.00"), salaryCategory.getId()),
                    createOperation(Operation.Type.INCOME, new BigDecimal("3000.00"), salaryCategory.getId()),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodCategory.getId())
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal totalIncome = analyticsService.getTotalIncome(startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("8000.00"), totalIncome);
        }

        @Test
        @DisplayName("Получение общей суммы расходов должно возвращать корректное значение")
        void getTotalExpense_ShouldReturnCorrectSum() {
            // Arrange
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodCategory.getId()),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1500.00"), transportCategory.getId()),
                    createOperation(Operation.Type.INCOME, new BigDecimal("5000.00"), salaryCategory.getId())
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal totalExpense = analyticsService.getTotalExpense(startDate, endDate);

            // Assert
            assertEquals(new BigDecimal("3500.00"), totalExpense);
        }

        @Test
        @DisplayName("Получение общей суммы доходов при отсутствии доходов должно возвращать ноль")
        void getTotalIncome_NoIncome_ShouldReturnZero() {
            // Arrange
            List<Operation> operations = Arrays.asList(
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("2000.00"), foodCategory.getId()),
                    createOperation(Operation.Type.EXPENSE, new BigDecimal("1500.00"), transportCategory.getId())
            );

            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(operations);

            // Act
            BigDecimal totalIncome = analyticsService.getTotalIncome(startDate, endDate);

            // Assert
            assertEquals(BigDecimal.ZERO, totalIncome);
        }
    }

    @Nested
    @DisplayName("Тесты получения динамики по месяцам")
    class MonthlyDynamicsTests {

        @Test
        @DisplayName("Получение динамики по месяцам должно возвращать корректные данные")
        void getMonthlyDynamics_ShouldReturnCorrectData() {
            // Arrange
            LocalDate janDate = LocalDate.of(2024, 1, 15);
            LocalDate febDate = LocalDate.of(2024, 2, 15);

            Operation janIncome = new Operation(
                    UUID.randomUUID(), Operation.Type.INCOME, accountId,
                    new BigDecimal("50000.00"), janDate, "Salary", salaryCategory.getId()
            );

            Operation janExpense1 = new Operation(
                    UUID.randomUUID(), Operation.Type.EXPENSE, accountId,
                    new BigDecimal("5000.00"), janDate, "Food", foodCategory.getId()
            );

            Operation janExpense2 = new Operation(
                    UUID.randomUUID(), Operation.Type.EXPENSE, accountId,
                    new BigDecimal("3000.00"), janDate, "Transport", transportCategory.getId()
            );

            Operation febIncome = new Operation(
                    UUID.randomUUID(), Operation.Type.INCOME, accountId,
                    new BigDecimal("55000.00"), febDate, "Salary", salaryCategory.getId()
            );

            Operation febExpense = new Operation(
                    UUID.randomUUID(), Operation.Type.EXPENSE, accountId,
                    new BigDecimal("7000.00"), febDate, "Food", foodCategory.getId()
            );

            List<Operation> operations = Arrays.asList(
                    janIncome, janExpense1, janExpense2, febIncome, febExpense
            );

            when(operationRepository.findByDateRange(startDate, endDate.plusMonths(1))).thenReturn(operations);

            // Act
            Map<YearMonth, AnalyticsService.BalanceInfo> dynamics =
                    analyticsService.getMonthlyDynamics(startDate, endDate.plusMonths(1));

            // Assert
            assertEquals(2, dynamics.size());

            YearMonth jan2024 = YearMonth.of(2024, 1);
            YearMonth feb2024 = YearMonth.of(2024, 2);

            AnalyticsService.BalanceInfo janInfo = dynamics.get(jan2024);
            assertNotNull(janInfo);
            assertEquals(new BigDecimal("50000.00"), janInfo.getIncome());
            assertEquals(new BigDecimal("8000.00"), janInfo.getExpense());
            assertEquals(new BigDecimal("42000.00"), janInfo.getDifference());

            AnalyticsService.BalanceInfo febInfo = dynamics.get(feb2024);
            assertNotNull(febInfo);
            assertEquals(new BigDecimal("55000.00"), febInfo.getIncome());
            assertEquals(new BigDecimal("7000.00"), febInfo.getExpense());
            assertEquals(new BigDecimal("48000.00"), febInfo.getDifference());
        }

        @Test
        @DisplayName("Получение динамики по месяцам при отсутствии операций должно возвращать пустую карту")
        void getMonthlyDynamics_NoOperations_ShouldReturnEmptyMap() {
            // Arrange
            when(operationRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

            // Act
            Map<YearMonth, AnalyticsService.BalanceInfo> result =
                    analyticsService.getMonthlyDynamics(startDate, endDate);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тесты класса BalanceInfo")
    class BalanceInfoTests {

        @Test
        @DisplayName("Добавление дохода должно увеличивать income")
        void addIncome_ShouldIncreaseIncome() {
            // Arrange
            AnalyticsService.BalanceInfo info = new AnalyticsService.BalanceInfo();

            // Act
            info.addIncome(new BigDecimal("1000.00"));
            info.addIncome(new BigDecimal("500.00"));

            // Assert
            assertEquals(new BigDecimal("1500.00"), info.getIncome());
            assertEquals(BigDecimal.ZERO, info.getExpense());
            assertEquals(new BigDecimal("1500.00"), info.getDifference());
        }

        @Test
        @DisplayName("Добавление расхода должно увеличивать expense")
        void addExpense_ShouldIncreaseExpense() {
            // Arrange
            AnalyticsService.BalanceInfo info = new AnalyticsService.BalanceInfo();

            // Act
            info.addExpense(new BigDecimal("300.00"));
            info.addExpense(new BigDecimal("200.00"));

            // Assert
            assertEquals(BigDecimal.ZERO, info.getIncome());
            assertEquals(new BigDecimal("500.00"), info.getExpense());
            assertEquals(new BigDecimal("-500.00"), info.getDifference());
        }

        @Test
        @DisplayName("Метод toString должен возвращать корректное представление")
        void toString_ShouldReturnCorrectFormat() {
            // Arrange
            AnalyticsService.BalanceInfo info = new AnalyticsService.BalanceInfo();
            info.addIncome(new BigDecimal("1000.00"));
            info.addExpense(new BigDecimal("300.00"));

            // Act
            String result = info.toString();

            // Assert
            assertTrue(result.contains("Доход: 1000"));
            assertTrue(result.contains("Расход: 300"));
            assertTrue(result.contains("Разница: 700"));
        }
    }
}