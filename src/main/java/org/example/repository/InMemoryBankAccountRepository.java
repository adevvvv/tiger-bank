package org.example.repository;


import com.google.inject.Singleton;
import org.example.model.BankAccount;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryBankAccountRepository implements BankAccountRepository {

    private final Map<UUID, BankAccount> accounts = new ConcurrentHashMap<>();

    @Override
    public BankAccount save(BankAccount account) {
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<BankAccount> findById(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<BankAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public void delete(UUID id) {
        accounts.remove(id);
    }

    @Override
    public boolean exists(UUID id) {
        return accounts.containsKey(id);
    }

    @Override
    public void update(BankAccount account) {
        if (exists(account.getId())) {
            accounts.put(account.getId(), account);
        } else {
            throw new IllegalArgumentException("Учетная запись с идентификатором не найдена: " + account.getId());
        }
    }
}