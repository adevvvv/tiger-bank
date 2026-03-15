package org.example.command;

import org.example.service.OperationManager;

import java.util.Scanner;
import java.util.UUID;

public class DeleteOperationCommand implements Command {
    private final OperationManager operationManager;
    private final Scanner scanner;

    public DeleteOperationCommand(OperationManager operationManager, Scanner scanner) {
        this.operationManager = operationManager;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Доступные операции ---");
        operationManager.getAllOperations().forEach(op ->
                System.out.println("• " + op.getId() + " | " + op.getDate() + " | " +
                        op.getType() + " | " + op.getAmount() + " руб. | " + op.getDescription()));

        System.out.print("Введите ID операции для удаления: ");
        UUID id = UUID.fromString(scanner.nextLine());

        operationManager.deleteOperation(id);
        System.out.println("✅ Операция удалена");
    }

    @Override
    public String getDescription() {
        return "Удаление операции";
    }
}