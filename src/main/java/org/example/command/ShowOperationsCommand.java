package org.example.command;

import org.example.service.OperationManager;

public class ShowOperationsCommand implements Command {
    private final OperationManager operationManager;

    public ShowOperationsCommand(OperationManager operationManager) {
        this.operationManager = operationManager;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Операции ---");
        var operations = operationManager.getAllOperations();
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

    @Override
    public String getDescription() {
        return "Просмотр операций";
    }
}