package org.example.service;

import com.google.inject.Inject;
import org.example.model.Category;
import org.example.model.Operation;
import org.example.repository.BankAccountRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OperationManager {

    private final OperationRepository operationRepository;
    private final BankAccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final AccountManager accountManager;

    @Inject
    public OperationManager(
            OperationRepository operationRepository,
            BankAccountRepository accountRepository,
            CategoryRepository categoryRepository,
            AccountManager accountManager) {
        this.operationRepository = operationRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.accountManager = accountManager;
    }

    public Operation createOperation(
            Operation.Type type,
            UUID bankAccountId,
            BigDecimal amount,
            LocalDate date,
            String description,
            UUID categoryId) {

        validateOperation(type, bankAccountId, amount, categoryId);

        Operation operation = new Operation(
                UUID.randomUUID(),
                type,
                bankAccountId,
                amount,
                date != null ? date : LocalDate.now(),
                description != null ? description.trim() : "",
                categoryId
        );

        BigDecimal balanceChange = type == Operation.Type.INCOME ? amount : amount.negate();
        accountManager.updateBalance(bankAccountId, balanceChange);

        return operationRepository.save(operation);
    }

    private void validateOperation(Operation.Type type, UUID bankAccountId,
                                   BigDecimal amount, UUID categoryId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        if (type == null) {
            throw new IllegalArgumentException("Тип операции не может быть null");
        }
        if (bankAccountId == null) {
            throw new IllegalArgumentException("ID счета не может быть null");
        }
        if (!accountRepository.exists(bankAccountId)) {
            throw new IllegalArgumentException("Счет не найден");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("ID категории не может быть null");
        }
        if (!categoryRepository.exists(categoryId)) {
            throw new IllegalArgumentException("Категория не найдена");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        if ((type == Operation.Type.INCOME && category.getType() != Category.Type.INCOME) ||
                (type == Operation.Type.EXPENSE && category.getType() != Category.Type.EXPENSE)) {
            throw new IllegalArgumentException("Тип категории не соответствует типу операции");
        }
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public List<Operation> getOperationsByAccountId(UUID accountId) {
        return operationRepository.findByBankAccountId(accountId);
    }

    public List<Operation> getOperationsByCategoryId(UUID categoryId) {
        return operationRepository.findByCategoryId(categoryId);
    }

    public void deleteOperation(UUID id) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Операция не найдена с id: " + id));

        BigDecimal balanceChange = operation.getType() == Operation.Type.INCOME ?
                operation.getAmount().negate() : operation.getAmount();
        accountManager.updateBalance(operation.getBankAccountId(), balanceChange);

        operationRepository.delete(id);
    }
}