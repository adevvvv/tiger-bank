package org.example.command;

import org.example.model.BankAccount;
import org.example.service.AccountManager;

import java.util.Scanner;
import java.util.UUID;

public class UpdateAccountCommand implements Command {
    private final AccountManager accountManager;
    private final Scanner scanner;

    public UpdateAccountCommand(AccountManager accountManager, Scanner scanner) {
        this.accountManager = accountManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Доступные счета ---");
        accountManager.getAllAccounts().forEach(acc ->
                System.out.println("• " + acc.getId() + " | " + acc.getName() + " | " + acc.getBalance() + " руб."));

        System.out.print("Введите ID счета для обновления: ");
        UUID id = UUID.fromString(scanner.nextLine());
        System.out.print("Введите новое название: ");
        String newName = scanner.nextLine();

        BankAccount updated = accountManager.updateAccountName(id, newName);
        System.out.println("✅ Счет обновлен: " + updated.getName());
    }

    @Override
    public String getDescription() {
        return "Обновление счета";
    }
}