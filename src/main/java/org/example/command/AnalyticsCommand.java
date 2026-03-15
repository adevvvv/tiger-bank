package org.example.command;

import org.example.model.Category;
import org.example.service.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;

public class AnalyticsCommand implements Command {
    private final AnalyticsService analyticsService;
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public AnalyticsCommand(AnalyticsService analyticsService, Scanner scanner) {
        this.analyticsService = analyticsService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Аналитика ---");
        System.out.println("Формат даты: ДД.ММ.ГГГГ (например, 13.03.2026)");

        LocalDate start = null;
        LocalDate end = null;

        try {
            System.out.print("Введите начальную дату: ");
            String startStr = scanner.nextLine();
            start = LocalDate.parse(startStr, DATE_FORMATTER);

            System.out.print("Введите конечную дату: ");
            String endStr = scanner.nextLine();
            end = LocalDate.parse(endStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Неверный формат даты. Используйте ДД.ММ.ГГГГ");
            return;
        }

        if (start.isAfter(end)) {
            System.out.println("❌ Начальная дата не может быть позже конечной");
            return;
        }

        BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(start, end);
        System.out.println("Разница доходов и расходов: " + difference + " руб.");

        System.out.println("\nДоходы по категориям:");
        Map<Category, BigDecimal> incomeByCategory = analyticsService.groupIncomeByCategory(start, end);
        if (incomeByCategory.isEmpty()) {
            System.out.println("  Нет доходов за указанный период");
        } else {
            incomeByCategory.forEach((cat, amount) ->
                    System.out.println("• " + cat.getName() + ": " + amount + " руб."));
        }

        System.out.println("\nРасходы по категориям:");
        Map<Category, BigDecimal> expenseByCategory = analyticsService.groupExpenseByCategory(start, end);
        if (expenseByCategory.isEmpty()) {
            System.out.println("  Нет расходов за указанный период");
        } else {
            expenseByCategory.forEach((cat, amount) ->
                    System.out.println("• " + cat.getName() + ": " + amount + " руб."));
        }

        System.out.println("\nОбщая сумма доходов: " + analyticsService.getTotalIncome(start, end) + " руб.");
        System.out.println("Общая сумма расходов: " + analyticsService.getTotalExpense(start, end) + " руб.");
    }

    @Override
    public String getDescription() {
        return "Аналитика";
    }
}