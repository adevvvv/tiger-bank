package org.example.repository;

import org.example.model.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationRepository {
    Operation save(Operation operation);
    Optional<Operation> findById(UUID id);
    List<Operation> findAll();
    List<Operation> findByBankAccountId(UUID bankAccountId);
    List<Operation> findByCategoryId(UUID categoryId);
    List<Operation> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Operation> findByType(Operation.Type type);
    void delete(UUID id);
    boolean exists(UUID id);
    void update(Operation operation);
}