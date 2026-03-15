package org.example.console;

import com.google.inject.Inject;
import org.example.command.*;
import org.example.dto.DataHandlerFactory;
import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;
import org.example.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public class ConsoleApp {

    private final AccountManager accountManager;
    private final CategoryManager categoryManager;
    private final OperationManager operationManager;
    private final AnalyticsService analyticsService;
    private final DataHandlerFactory dataHandlerFactory;
    private final StatisticsService statisticsService;
    private final Scanner scanner;
    private final CommandInvoker commandInvoker;

    @Inject
    public ConsoleApp(
            AccountManager accountManager,
            CategoryManager categoryManager,
            OperationManager operationManager,
            AnalyticsService analyticsService,
            DataHandlerFactory dataHandlerFactory,
            StatisticsService statisticsService) {
        this.accountManager = accountManager;
        this.categoryManager = categoryManager;
        this.operationManager = operationManager;
        this.analyticsService = analyticsService;
        this.dataHandlerFactory = dataHandlerFactory;
        this.statisticsService = statisticsService;
        this.scanner = new Scanner(System.in);
        this.commandInvoker = new CommandInvoker(statisticsService);

        initializeCommands();
    }

    private void initializeCommands() {
        commandInvoker.registerCommand(1, new ShowAccountsCommand(accountManager));
        commandInvoker.registerCommand(2, new ShowCategoriesCommand(categoryManager));
        commandInvoker.registerCommand(3, new ShowOperationsCommand(operationManager));
        commandInvoker.registerCommand(4, new CreateAccountCommand(accountManager, scanner));
        commandInvoker.registerCommand(5, new CreateCategoryCommand(categoryManager, scanner));
        commandInvoker.registerCommand(6, new CreateOperationCommand(operationManager, accountManager, categoryManager, scanner));
        commandInvoker.registerCommand(7, new AnalyticsCommand(analyticsService, scanner));
        commandInvoker.registerCommand(8, new ExportDataCommand(dataHandlerFactory, accountManager, categoryManager, operationManager, scanner));
        commandInvoker.registerCommand(9, new ImportDataCommand(dataHandlerFactory, scanner));
        commandInvoker.registerCommand(10, new UpdateAccountCommand(accountManager, scanner));
        commandInvoker.registerCommand(11, new UpdateCategoryCommand(categoryManager, scanner));
        commandInvoker.registerCommand(12, new DeleteAccountCommand(accountManager, operationManager, scanner));
        commandInvoker.registerCommand(13, new DeleteCategoryCommand(categoryManager, operationManager, scanner));
        commandInvoker.registerCommand(14, new DeleteOperationCommand(operationManager, scanner));
        commandInvoker.registerCommand(15, new ShowStatisticsCommand(statisticsService));
    }

    public void start() {
        System.out.println("\n🐯 Добро пожаловать в ТигрБанк! 🐯");
        System.out.println("Модуль учета финансов\n");

        demonstrateFullFunctionality();

        interactiveMenu();
    }

    private void demonstrateFullFunctionality() {
        System.out.println("=== ПОЛНАЯ ДЕМОНСТРАЦИЯ ФУНКЦИОНАЛА (без интерактива) ===\n");

        // Создаем тестовые данные напрямую через менеджеры, а не через команды с вводом
        try {
            // 1. Создаем счет
            BankAccount account = accountManager.createAccount("Основной счет", new BigDecimal("50000.00"));
            BankAccount savingsAccount = accountManager.createAccount("Накопительный счет", new BigDecimal("10000.00"));
            System.out.println("✅ Созданы тестовые счета");

            // 2. Создаем категории
            Category salary = categoryManager.createCategory("Зарплата", Category.Type.INCOME);
            Category products = categoryManager.createCategory("Продукты", Category.Type.EXPENSE);
            Category cafe = categoryManager.createCategory("Кафе", Category.Type.EXPENSE);
            System.out.println("✅ Созданы тестовые категории");

            // 3. Создаем операции
            LocalDate now = LocalDate.now();
            operationManager.createOperation(Operation.Type.INCOME, account.getId(),
                    new BigDecimal("75000.00"), now.minusDays(5), "Зарплата", salary.getId());
            operationManager.createOperation(Operation.Type.EXPENSE, account.getId(),
                    new BigDecimal("3500.00"), now.minusDays(3), "Продукты", products.getId());
            operationManager.createOperation(Operation.Type.EXPENSE, account.getId(),
                    new BigDecimal("1200.00"), now.minusDays(2), "Кофе", cafe.getId());
            System.out.println("✅ Созданы тестовые операции");

            // 4. Показываем результаты (неинтерактивные команды)
            commandInvoker.executeCommand(1); // Просмотр счетов
            commandInvoker.executeCommand(2); // Просмотр категорий
            commandInvoker.executeCommand(3); // Просмотр операций

            // 5. Аналитика с тестовыми датами
            System.out.println("\n--- Тестовая аналитика ---");
            LocalDate startOfMonth = now.withDayOfMonth(1);
            BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(startOfMonth, now);
            System.out.println("Разница доходов и расходов: " + difference + " руб.");

            System.out.println("Доходы по категориям:");
            analyticsService.groupIncomeByCategory(startOfMonth, now)
                    .forEach((cat, amount) -> System.out.println("  • " + cat.getName() + ": " + amount + " руб."));

            // 6. Экспорт (создаем файлы в текущей директории)
            String jsonFile = "tigerbank_test.json";
            String yamlFile = "tigerbank_test.yaml";
            String csvFile = "tigerbank_test.csv";

            dataHandlerFactory.getExporter(jsonFile).export(
                    accountManager.getAllAccounts(),
                    categoryManager.getAllCategories(),
                    operationManager.getAllOperations(),
                    jsonFile);
            dataHandlerFactory.getExporter(yamlFile).export(
                    accountManager.getAllAccounts(),
                    categoryManager.getAllCategories(),
                    operationManager.getAllOperations(),
                    yamlFile);
            dataHandlerFactory.getExporter(csvFile).export(
                    accountManager.getAllAccounts(),
                    categoryManager.getAllCategories(),
                    operationManager.getAllOperations(),
                    csvFile);
            System.out.println("✅ Выполнен тестовый экспорт в JSON/YAML/CSV");

            // 7. Статистика
            commandInvoker.executeCommand(15); // Просмотр статистики

        } catch (Exception e) {
            System.out.println("❌ Ошибка в демонстрации: " + e.getMessage());
        }

        System.out.println("\n=== ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===\n");
        System.out.println("Нажмите Enter для перехода в интерактивный режим...");
        scanner.nextLine();
    }

    private void interactiveMenu() {
        while (true) {
            commandInvoker.printMenu();

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine(); // очищаем буфер
                System.out.println("❌ Пожалуйста, введите число");
                continue;
            }

            if (choice == 0) {
                System.out.println("\n=== ИТОГОВАЯ СТАТИСТИКА ===");
                statisticsService.printStatistics();
                System.out.println("\nДо свидания!");
                return;
            }

            commandInvoker.executeCommand(choice);
        }
    }
}