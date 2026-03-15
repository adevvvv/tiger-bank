package org.example.service;

import com.google.inject.Inject;
import org.example.model.Category;
import org.example.model.Operation;
import org.example.repository.CategoryRepository;
import org.example.repository.OperationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {

    private final OperationRepository operationRepository;
    private final CategoryRepository categoryRepository;

    @Inject
    public AnalyticsService(OperationRepository operationRepository,
                            CategoryRepository categoryRepository) {
        this.operationRepository = operationRepository;
        this.categoryRepository = categoryRepository;
    }

    public BigDecimal calculateIncomeExpenseDifference(LocalDate start, LocalDate end) {
        List<Operation> operations = operationRepository.findByDateRange(start, end);

        BigDecimal totalIncome = operations.stream()
                .filter(op -> op.getType() == Operation.Type.INCOME)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = operations.stream()
                .filter(op -> op.getType() == Operation.Type.EXPENSE)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.subtract(totalExpense);
    }

    public Map<Category, BigDecimal> groupIncomeByCategory(LocalDate start, LocalDate end) {
        return groupOperationsByCategory(start, end, Operation.Type.INCOME);
    }

    public Map<Category, BigDecimal> groupExpenseByCategory(LocalDate start, LocalDate end) {
        return groupOperationsByCategory(start, end, Operation.Type.EXPENSE);
    }

    private Map<Category, BigDecimal> groupOperationsByCategory(LocalDate start, LocalDate end,
                                                                Operation.Type type) {
        List<Operation> operations = operationRepository.findByDateRange(start, end).stream()
                .filter(op -> op.getType() == type)
                .toList();

        Map<Category, BigDecimal> result = new HashMap<>();

        for (Operation op : operations) {
            categoryRepository.findById(op.getCategoryId()).ifPresent(category ->
                    result.merge(category, op.getAmount(), BigDecimal::add));
        }

        return result;
    }

    public BigDecimal getTotalIncome(LocalDate start, LocalDate end) {
        return operationRepository.findByDateRange(start, end).stream()
                .filter(op -> op.getType() == Operation.Type.INCOME)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense(LocalDate start, LocalDate end) {
        return operationRepository.findByDateRange(start, end).stream()
                .filter(op -> op.getType() == Operation.Type.EXPENSE)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Map.Entry<Category, BigDecimal>> getTopExpenseCategories(LocalDate start,
                                                                         LocalDate end,
                                                                         int limit) {
        return groupExpenseByCategory(start, end).entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public BigDecimal getAverageAmountByCategory(UUID categoryId, LocalDate start, LocalDate end) {
        List<Operation> operations = operationRepository.findByDateRange(start, end).stream()
                .filter(op -> categoryId.equals(op.getCategoryId()))
                .toList();

        if (operations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = operations.stream()
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(operations.size()), 2, RoundingMode.HALF_UP);
    }

    public Map<YearMonth, BalanceInfo> getMonthlyDynamics(LocalDate start, LocalDate end) {
        List<Operation> operations = operationRepository.findByDateRange(start, end);

        Map<YearMonth, BalanceInfo> dynamics = new TreeMap<>();

        for (Operation op : operations) {
            YearMonth yearMonth = YearMonth.from(op.getDate());
            BalanceInfo info = dynamics.computeIfAbsent(yearMonth, k -> new BalanceInfo());

            if (op.getType() == Operation.Type.INCOME) {
                info.addIncome(op.getAmount());
            } else {
                info.addExpense(op.getAmount());
            }
        }

        return dynamics;
    }

    public static class BalanceInfo {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;

        public void addIncome(BigDecimal amount) {
            income = income.add(amount);
        }

        public void addExpense(BigDecimal amount) {
            expense = expense.add(amount);
        }

        public BigDecimal getIncome() { return income; }
        public BigDecimal getExpense() { return expense; }
        public BigDecimal getDifference() { return income.subtract(expense); }

        @Override
        public String toString() {
            return String.format("Доход: %s, Расход: %s, Разница: %s",
                    income, expense, getDifference());
        }
    }
}