package org.example.dto;


import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private final List<BankAccount> accounts = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final List<Operation> operations = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public void addAccount(BankAccount account) {
        accounts.add(account);
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public List<String> getErrors() { return errors; }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Импортировано: счетов=%d, категорий=%d, операций=%d, ошибок=%d",
                accounts.size(), categories.size(), operations.size(), errors.size());
    }
}
