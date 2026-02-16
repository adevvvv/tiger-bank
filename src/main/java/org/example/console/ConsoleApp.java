package org.example.console;


import com.google.inject.Inject;
import org.example.dto.DataExporter;
import org.example.dto.DataImporter;
import org.example.dto.ImportResult;
import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;
import org.example.service.AccountManager;
import org.example.service.AnalyticsService;
import org.example.service.CategoryManager;
import org.example.service.OperationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;


public class ConsoleApp {

    private final AccountManager accountManager;
    private final CategoryManager categoryManager;
    private final OperationManager operationManager;
    private final AnalyticsService analyticsService;
    private final DataExporter dataExporter;
    private final DataImporter dataImporter;
    private final Scanner scanner;

    @Inject
    public ConsoleApp(
            AccountManager accountManager,
            CategoryManager categoryManager,
            OperationManager operationManager,
            AnalyticsService analyticsService,
            DataExporter dataExporter,
            DataImporter dataImporter) {
        this.accountManager = accountManager;
        this.categoryManager = categoryManager;
        this.operationManager = operationManager;
        this.analyticsService = analyticsService;
        this.dataExporter = dataExporter;
        this.dataImporter = dataImporter;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n🐯 Добро пожаловать в ТигрБанк! 🐯");
        System.out.println("Модуль учета финансов\n");

        demonstrateFullFunctionality();

        interactiveMenu();
    }

    private void demonstrateFullFunctionality() {
        System.out.println("=== ПОЛНАЯ ДЕМОНСТРАЦИЯ ФУНКЦИОНАЛА ===\n");

        System.out.println("1. СОЗДАНИЕ СЧЕТОВ:");
        BankAccount mainAccount = accountManager.createAccount("Основной счет", new BigDecimal("50000.00"));
        BankAccount savingsAccount = accountManager.createAccount("Накопительный счет", new BigDecimal("10000.00"));
        BankAccount cashAccount = accountManager.createAccount("Наличные", new BigDecimal("5000.00"));
        printAccounts();

        System.out.println("\n2. СОЗДАНИЕ КАТЕГОРИЙ:");
        Category salary = categoryManager.createCategory("Зарплата", Category.Type.INCOME);
        Category cashback = categoryManager.createCategory("Кэшбэк", Category.Type.INCOME);
        Category cafe = categoryManager.createCategory("Кафе", Category.Type.EXPENSE);
        Category products = categoryManager.createCategory("Продукты", Category.Type.EXPENSE);
        Category transport = categoryManager.createCategory("Транспорт", Category.Type.EXPENSE);
        Category health = categoryManager.createCategory("Здоровье", Category.Type.EXPENSE);
        Category entertainment = categoryManager.createCategory("Развлечения", Category.Type.EXPENSE);
        printCategories();

        System.out.println("\n3. СОЗДАНИЕ ОПЕРАЦИЙ:");

        LocalDate now = LocalDate.now();

        // Доходы
        operationManager.createOperation(Operation.Type.INCOME, mainAccount.getId(),
                new BigDecimal("75000.00"), now.minusDays(5), "Зарплата за месяц", salary.getId());
        operationManager.createOperation(Operation.Type.INCOME, mainAccount.getId(),
                new BigDecimal("1200.00"), now.minusDays(3), "Кэшбэк по карте", cashback.getId());
        operationManager.createOperation(Operation.Type.INCOME, savingsAccount.getId(),
                new BigDecimal("3500.00"), now.minusDays(10), "Проценты по вкладу", cashback.getId());

        // Расходы
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("3500.00"), now.minusDays(7), "Продукты в супермаркете", products.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("850.00"), now.minusDays(6), "Кофе и завтрак", cafe.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, cashAccount.getId(),
                new BigDecimal("1500.00"), now.minusDays(4), "Обед в ресторане", cafe.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("3000.00"), now.minusDays(3), "Аптека", health.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("1000.00"), now.minusDays(2), "Такси", transport.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("2500.00"), now.minusDays(1), "Кино и ужин", entertainment.getId());

        // Операции за прошлый месяц
        LocalDate lastMonth = now.minusMonths(1);
        operationManager.createOperation(Operation.Type.INCOME, mainAccount.getId(),
                new BigDecimal("75000.00"), lastMonth.withDayOfMonth(10), "Зарплата", salary.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("12000.00"), lastMonth.withDayOfMonth(15), "Отпуск", entertainment.getId());
        operationManager.createOperation(Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("4500.00"), lastMonth.withDayOfMonth(20), "Продукты", products.getId());

        printOperations();

        System.out.println("\n4. АНАЛИТИКА:");

        LocalDate startOfMonth = now.withDayOfMonth(1);

        BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(startOfMonth, now);
        System.out.println("   Разница доходов и расходов за текущий месяц: " + difference + " руб.");

        System.out.println("\n   Доходы по категориям:");
        analyticsService.groupIncomeByCategory(startOfMonth, now)
                .forEach((cat, amount) ->
                        System.out.println("   • " + cat.getName() + ": " + amount + " руб."));

        System.out.println("\n   Расходы по категориям:");
        analyticsService.groupExpenseByCategory(startOfMonth, now)
                .forEach((cat, amount) ->
                        System.out.println("   • " + cat.getName() + ": " + amount + " руб."));

        System.out.println("\n   Топ-3 категорий расходов:");
        analyticsService.getTopExpenseCategories(startOfMonth, now, 3)
                .forEach(entry ->
                        System.out.println("   • " + entry.getKey().getName() + ": " + entry.getValue() + " руб."));

        System.out.println("\n   Динамика по месяцам:");
        Map<YearMonth, AnalyticsService.BalanceInfo> dynamics =
                analyticsService.getMonthlyDynamics(lastMonth.minusMonths(2), now);
        dynamics.forEach((month, info) ->
                System.out.println("   • " + month + ": " + info));

        System.out.println("\n5. ЭКСПОРТ ДАННЫХ В JSON:");
        String exportFile = "tigerbank_export.json";
        dataExporter.export(
                accountManager.getAllAccounts(),
                categoryManager.getAllCategories(),
                operationManager.getAllOperations(),
                exportFile
        );

        System.out.println("\n6. ИМПОРТ ДАННЫХ ИЗ JSON:");
        ImportResult importResult = dataImporter.importData(exportFile);
        System.out.println("   " + importResult);

        System.out.println("\n7. ДЕМОНСТРАЦИЯ CRUD ОПЕРАЦИЙ:");

        System.out.println("   Обновление названия счета:");
        BankAccount updatedAccount = accountManager.updateAccountName(mainAccount.getId(), "Основной счет (обновлен)");
        System.out.println("   ✓ Новое название: " + updatedAccount.getName());

        System.out.println("\n   Обновление названия категории:");
        Category updatedCategory = categoryManager.updateCategoryName(cafe.getId(), "Кафе и рестораны");
        System.out.println("   ✓ Новое название: " + updatedCategory.getName());

        System.out.println("\n   Обновление описания операции:");
        List<Operation> ops = operationManager.getAllOperations();
        if (!ops.isEmpty()) {
            Operation op = ops.get(0);
            Operation updatedOp = operationManager.updateOperationDescription(
                    op.getId(), op.getDescription() + " (обновлено)");
            System.out.println("   ✓ Новое описание: " + updatedOp.getDescription());
        }

        System.out.println("\n   Удаление операции:");
        Operation tempOp = operationManager.createOperation(
                Operation.Type.EXPENSE, mainAccount.getId(),
                new BigDecimal("100.00"), LocalDate.now(),
                "Временная операция", transport.getId());
        System.out.println("   ✓ Создана временная операция: " + tempOp.getDescription());

        operationManager.deleteOperation(tempOp.getId());
        System.out.println("   ✓ Временная операция удалена");

        System.out.println("\n=== ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===\n");
    }

    private void interactiveMenu() {
        while (true) {
            System.out.println("\n=== ИНТЕРАКТИВНОЕ МЕНЮ ===");
            System.out.println("1. Показать все счета");
            System.out.println("2. Показать все категории");
            System.out.println("3. Показать все операции");
            System.out.println("4. Создать счет");
            System.out.println("5. Создать категорию");
            System.out.println("6. Создать операцию");
            System.out.println("7. Аналитика");
            System.out.println("8. Экспорт данных в JSON");
            System.out.println("9. Импорт данных из JSON");
            System.out.println("10. Обновить счет");
            System.out.println("11. Обновить категорию");
            System.out.println("12. Удалить операцию");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (choice) {
                    case 1 -> printAccounts();
                    case 2 -> printCategories();
                    case 3 -> printOperations();
                    case 4 -> createAccountInteractive();
                    case 5 -> createCategoryInteractive();
                    case 6 -> createOperationInteractive();
                    case 7 -> analyticsMenu();
                    case 8 -> exportMenu();
                    case 9 -> importMenu();
                    case 10 -> updateAccountInteractive();
                    case 11 -> updateCategoryInteractive();
                    case 12 -> deleteOperationInteractive();
                    case 0 -> {
                        System.out.println("До свидания!");
                        return;
                    }
                    default -> System.out.println("Неверный выбор!");
                }
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
            }
        }
    }

    private void printAccounts() {
        List<BankAccount> accounts = accountManager.getAllAccounts();
        System.out.println("\n--- Счета ---");
        if (accounts.isEmpty()) {
            System.out.println("Счета отсутствуют");
        } else {
            accounts.forEach(acc ->
                    System.out.println("• " + acc.getId() + " | " + acc.getName() + " | " +
                            acc.getBalance() + " руб."));
        }
    }

    private void printCategories() {
        List<Category> categories = categoryManager.getAllCategories();
        System.out.println("\n--- Категории ---");
        if (categories.isEmpty()) {
            System.out.println("Категории отсутствуют");
        } else {
            categories.forEach(cat ->
                    System.out.println("• " + cat.getId() + " | " + cat.getName() + " | " + cat.getType()));
        }
    }

    private void printOperations() {
        List<Operation> operations = operationManager.getAllOperations();
        System.out.println("\n--- Операции ---");
        if (operations.isEmpty()) {
            System.out.println("Операции отсутствуют");
        } else {
            operations.forEach(op ->
                    System.out.println("• " + op.getId() + " | " + op.getDate() + " | " +
                            op.getType() + " | " + op.getAmount() + " руб. | " +
                            op.getDescription() + " | Счет: " + op.getBankAccountId() +
                            " | Категория: " + op.getCategoryId()));
        }
    }

    private void createAccountInteractive() {
        System.out.print("Введите название счета: ");
        String name = scanner.nextLine();
        System.out.print("Введите начальный баланс: ");
        BigDecimal balance = scanner.nextBigDecimal();
        scanner.nextLine();

        BankAccount account = accountManager.createAccount(name, balance);
        System.out.println("✅ Счет создан: " + account.getName() + " (ID: " + account.getId() + ")");
    }

    private void createCategoryInteractive() {
        System.out.print("Введите название категории: ");
        String name = scanner.nextLine();
        System.out.print("Введите тип (INCOME/EXPENSE): ");
        String typeStr = scanner.nextLine().toUpperCase();
        Category.Type type = Category.Type.valueOf(typeStr);

        Category category = categoryManager.createCategory(name, type);
        System.out.println("✅ Категория создана: " + category.getName() + " (ID: " + category.getId() + ")");
    }

    private void createOperationInteractive() {
        try {
            System.out.print("Введите тип операции (INCOME/EXPENSE): ");
            String typeStr = scanner.nextLine().toUpperCase();
            Operation.Type type = Operation.Type.valueOf(typeStr);

            printAccounts();
            System.out.print("Введите ID счета: ");
            UUID accountId = UUID.fromString(scanner.nextLine());

            System.out.print("Введите сумму: ");
            BigDecimal amount = scanner.nextBigDecimal();
            scanner.nextLine();

            System.out.print("Введите дату (ГГГГ-ММ-ДД) или Enter для текущей даты: ");
            String dateStr = scanner.nextLine();
            LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

            System.out.print("Введите описание: ");
            String description = scanner.nextLine();

            printCategories();
            System.out.print("Введите ID категории: ");
            UUID categoryId = UUID.fromString(scanner.nextLine());

            Operation operation = operationManager.createOperation(
                    type, accountId, amount, date, description, categoryId
            );
            System.out.println("✅ Операция создана (ID: " + operation.getId() + ")");

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void analyticsMenu() {
        System.out.println("\n--- Аналитика ---");
        System.out.print("Введите начальную дату (ГГГГ-ММ-ДД): ");
        LocalDate start = LocalDate.parse(scanner.nextLine());
        System.out.print("Введите конечную дату (ГГГГ-ММ-ДД): ");
        LocalDate end = LocalDate.parse(scanner.nextLine());

        BigDecimal difference = analyticsService.calculateIncomeExpenseDifference(start, end);
        System.out.println("Разница доходов и расходов: " + difference + " руб.");

        System.out.println("\nДоходы по категориям:");
        analyticsService.groupIncomeByCategory(start, end)
                .forEach((cat, amount) ->
                        System.out.println("• " + cat.getName() + ": " + amount + " руб."));

        System.out.println("\nРасходы по категориям:");
        analyticsService.groupExpenseByCategory(start, end)
                .forEach((cat, amount) ->
                        System.out.println("• " + cat.getName() + ": " + amount + " руб."));
    }

    private void exportMenu() {
        System.out.print("Введите имя файла для экспорта (например, export.json): ");
        String filename = scanner.nextLine();

        dataExporter.export(
                accountManager.getAllAccounts(),
                categoryManager.getAllCategories(),
                operationManager.getAllOperations(),
                filename
        );
    }

    private void importMenu() {
        System.out.print("Введите имя файла для импорта: ");
        String filename = scanner.nextLine();

        ImportResult result = dataImporter.importData(filename);
        System.out.println("Результат импорта: " + result);

        if (!result.hasErrors()) {
            System.out.println("Импортированные данные можно просмотреть в соответствующих разделах.");
        } else {
            System.out.println("Ошибки при импорте:");
            result.getErrors().forEach(err -> System.out.println("  • " + err));
        }
    }

    private void updateAccountInteractive() {
        printAccounts();
        System.out.print("Введите ID счета для обновления: ");
        UUID id = UUID.fromString(scanner.nextLine());
        System.out.print("Введите новое название: ");
        String newName = scanner.nextLine();

        BankAccount updated = accountManager.updateAccountName(id, newName);
        System.out.println("✅ Счет обновлен: " + updated.getName());
    }

    private void updateCategoryInteractive() {
        printCategories();
        System.out.print("Введите ID категории для обновления: ");
        UUID id = UUID.fromString(scanner.nextLine());
        System.out.print("Введите новое название: ");
        String newName = scanner.nextLine();

        Category updated = categoryManager.updateCategoryName(id, newName);
        System.out.println("✅ Категория обновлена: " + updated.getName());
    }

    private void deleteOperationInteractive() {
        printOperations();
        System.out.print("Введите ID операции для удаления: ");
        UUID id = UUID.fromString(scanner.nextLine());

        operationManager.deleteOperation(id);
        System.out.println("✅ Операция удалена");
    }
}