package org.example.command;

import org.example.service.AccountManager;
import org.example.service.OperationManager;

import java.util.Scanner;
import java.util.UUID;

public class DeleteAccountCommand implements Command {
    private final AccountManager accountManager;
    private final OperationManager operationManager;
    private final Scanner scanner;

    public DeleteAccountCommand(AccountManager accountManager,
                                OperationManager operationManager,
                                Scanner scanner) {
        this.accountManager = accountManager;
        this.operationManager = operationManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Доступные счета ---");
        accountManager.getAllAccounts().forEach(acc ->
                System.out.println("• " + acc.getId() + " | " + acc.getName() + " | " + acc.getBalance() + " руб."));

        System.out.print("Введите ID счета для удаления: ");
        UUID id = UUID.fromString(scanner.nextLine());

        // Проверяем, есть ли операции по этому счету
        long operationsCount = operationManager.getOperationsByAccountId(id).size();
        if (operationsCount > 0) {
            System.out.print("⚠️ На счету есть " + operationsCount + " операций. Удалить счет вместе с операциями? (y/n): ");
            String confirm = scanner.nextLine();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("❌ Удаление отменено");
                return;
            }
            // Удаляем все операции по счету
            operationManager.getOperationsByAccountId(id).forEach(op ->
                    operationManager.deleteOperation(op.getId()));
        }

        accountManager.deleteAccount(id);
        System.out.println("✅ Счет удален");
    }

    @Override
    public String getDescription() {
        return "Удаление счета";
    }
}