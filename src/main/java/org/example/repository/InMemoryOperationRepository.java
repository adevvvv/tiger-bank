package org.example.repository;


import com.google.inject.Singleton;
import org.example.model.Operation;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class InMemoryOperationRepository implements OperationRepository {

    private final Map<UUID, Operation> operations = new ConcurrentHashMap<>();

    @Override
    public Operation save(Operation operation) {
        operations.put(operation.getId(), operation);
        return operation;
    }

    @Override
    public Optional<Operation> findById(UUID id) {
        return Optional.ofNullable(operations.get(id));
    }

    @Override
    public List<Operation> findAll() {
        return new ArrayList<>(operations.values());
    }

    @Override
    public List<Operation> findByBankAccountId(UUID bankAccountId) {
        return operations.values().stream()
                .filter(op -> op.getBankAccountId().equals(bankAccountId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findByCategoryId(UUID categoryId) {
        return operations.values().stream()
                .filter(op -> categoryId.equals(op.getCategoryId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return operations.values().stream()
                .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findByType(Operation.Type type) {
        return operations.values().stream()
                .filter(op -> op.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        operations.remove(id);
    }

    @Override
    public boolean exists(UUID id) {
        return operations.containsKey(id);
    }

    @Override
    public void update(Operation operation) {
        if (exists(operation.getId())) {
            operations.put(operation.getId(), operation);
        } else {
            throw new IllegalArgumentException("Operation not found with id: " + operation.getId());
        }
    }
}