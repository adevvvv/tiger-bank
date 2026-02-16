package org.example.repository;


import org.example.model.BankAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository {
    BankAccount save(BankAccount account);
    Optional<BankAccount> findById(UUID id);
    List<BankAccount> findAll();
    void delete(UUID id);
    boolean exists(UUID id);
    void update(BankAccount account);
}
