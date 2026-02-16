package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DataContainer(@JsonProperty("accounts") List<AccountDto> accounts,
                            @JsonProperty("categories") List<CategoryDto> categories,
                            @JsonProperty("operations") List<OperationDto> operations) {

    public DataContainer(List<AccountDto> accounts, List<CategoryDto> categories,
                         List<OperationDto> operations) {
        this.accounts = accounts;
        this.categories = categories;
        this.operations = operations;
    }

    @Override
    public List<AccountDto> accounts() {
        return accounts;
    }

    @Override
    public List<CategoryDto> categories() {
        return categories;
    }

    @Override
    public List<OperationDto> operations() {
        return operations;
    }

    public static class AccountDto {
        private UUID id;
        private final String name;
        private final BigDecimal balance;

        public AccountDto(BankAccount account) {
            this.id = account.getId();
            this.name = account.getName();
            this.balance = account.getBalance();
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }

    public static class CategoryDto {
        private UUID id;
        private Category.Type type;
        private final String name;

        public CategoryDto(Category category) {
            this.id = category.getId();
            this.type = category.getType();
            this.name = category.getName();
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Category.Type getType() {
            return type;
        }

        public void setType(Category.Type type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }
    }

    public static class OperationDto {
        private UUID id;
        private Operation.Type type;
        private final UUID bankAccountId;
        private final BigDecimal amount;
        private final LocalDate date;
        private final String description;
        private final UUID categoryId;

        public OperationDto(Operation operation) {
            this.id = operation.getId();
            this.type = operation.getType();
            this.bankAccountId = operation.getBankAccountId();
            this.amount = operation.getAmount();
            this.date = operation.getDate();
            this.description = operation.getDescription();
            this.categoryId = operation.getCategoryId();
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Operation.Type getType() {
            return type;
        }

        public void setType(Operation.Type type) {
            this.type = type;
        }

        public UUID getBankAccountId() {
            return bankAccountId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public UUID getCategoryId() {
            return categoryId;
        }
    }
}