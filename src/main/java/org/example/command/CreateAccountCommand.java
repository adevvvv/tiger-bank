package org.example.command;

import org.example.model.BankAccount;
import org.example.service.AccountManager;

import java.math.BigDecimal;
import java.util.Scanner;

public class CreateAccountCommand implements Command {
    private final AccountManager accountManager;
    private final Scanner scanner;

    public CreateAccountCommand(AccountManager accountManager, Scanner scanner) {
        this.accountManager = accountManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.print("Введите название счета: ");
        String name = scanner.nextLine();
        System.out.print("Введите начальный баланс: ");
        BigDecimal balance = scanner.nextBigDecimal();
        scanner.nextLine();

        BankAccount account = accountManager.createAccount(name, balance);
        System.out.println("✅ Счет создан: " + account.getName() + " (ID: " + account.getId() + ")");
    }

    @Override
    public String getDescription() {
        return "Создание счета";
    }
}