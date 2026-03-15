package org.example.service;

import com.google.inject.Inject;
import org.example.model.BankAccount;
import org.example.repository.BankAccountRepository;
import org.example.repository.OperationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountManager {

    private final BankAccountRepository accountRepository;
    private final OperationRepository operationRepository;

    @Inject
    public AccountManager(BankAccountRepository accountRepository,
                          OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    public BankAccount createAccount(String name, BigDecimal initialBalance) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        BankAccount account = new BankAccount(
                UUID.randomUUID(),
                name.trim(),
                initialBalance
        );

        return accountRepository.save(account);
    }

    public Optional<BankAccount> getAccount(UUID id) {
        return accountRepository.findById(id);
    }

    public List<BankAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    public BankAccount updateAccountName(UUID id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }

        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        account.setName(newName.trim());
        accountRepository.update(account);
        return account;
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.exists(id)) {
            throw new IllegalArgumentException("Account not found with id: " + id);
        }

        // Проверяем, есть ли операции по этому счету
        List<org.example.model.Operation> operations = operationRepository.findByBankAccountId(id);
        if (!operations.isEmpty()) {
            // Удаляем все операции по счету
            operations.forEach(op -> operationRepository.delete(op.getId()));
        }

        accountRepository.delete(id);
    }

    public void updateBalance(UUID accountId, BigDecimal amountChange) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));

        BigDecimal newBalance = account.getBalance().add(amountChange);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(newBalance);
        accountRepository.update(account);
    }
}