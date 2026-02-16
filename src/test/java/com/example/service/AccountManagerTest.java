package com.example.service;


import org.example.model.BankAccount;
import org.example.repository.BankAccountRepository;
import org.example.service.AccountManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты AccountManager")
class AccountManagerTest {

    @Mock
    private BankAccountRepository accountRepository;

    @InjectMocks
    private AccountManager accountManager;

    private UUID testId;
    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testAccount = new BankAccount(testId, "Test Account", new BigDecimal("1000.00"));
    }

    @Nested
    @DisplayName("Тесты создания счета")
    class CreateAccountTests {

        @Test
        @DisplayName("Создание счета с валидными данными должно возвращать созданный счет")
        void createAccount_ValidData_ShouldReturnAccount() {
            // Arrange
            String name = "Test Account";
            BigDecimal balance = new BigDecimal("1000.00");

            when(accountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BankAccount account = accountManager.createAccount(name, balance);

            // Assert
            assertNotNull(account);
            assertNotNull(account.getId());
            assertEquals(name, account.getName());
            assertEquals(balance, account.getBalance());
            verify(accountRepository, times(1)).save(any(BankAccount.class));
        }

        @Test
        @DisplayName("Создание счета с пустым именем должно выбрасывать исключение")
        void createAccount_EmptyName_ShouldThrowException() {
            // Arrange
            String name = "   ";
            BigDecimal balance = new BigDecimal("1000.00");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.createAccount(name, balance)
            );

            assertEquals("Account name cannot be empty", exception.getMessage());
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание счета с null именем должно выбрасывать исключение")
        void createAccount_NullName_ShouldThrowException() {
            // Arrange
            String name = null;
            BigDecimal balance = new BigDecimal("1000.00");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.createAccount(name, balance)
            );

            assertEquals("Account name cannot be empty", exception.getMessage());
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание счета с отрицательным балансом должно выбрасывать исключение")
        void createAccount_NegativeBalance_ShouldThrowException() {
            // Arrange
            String name = "Test Account";
            BigDecimal balance = new BigDecimal("-100.00");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.createAccount(name, balance)
            );

            assertEquals("Initial balance cannot be negative", exception.getMessage());
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Создание счета с null балансом должно выбрасывать исключение")
        void createAccount_NullBalance_ShouldThrowException() {
            // Arrange
            String name = "Test Account";
            BigDecimal balance = null;

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.createAccount(name, balance)
            );

            assertEquals("Initial balance cannot be negative", exception.getMessage());
            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Тесты получения счета")
    class GetAccountTests {

        @Test
        @DisplayName("Получение существующего счета должно возвращать Optional с счетом")
        void getAccount_ExistingId_ShouldReturnAccount() {
            // Arrange
            when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

            // Act
            Optional<BankAccount> result = accountManager.getAccount(testId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testAccount, result.get());
            verify(accountRepository, times(1)).findById(testId);
        }

        @Test
        @DisplayName("Получение несуществующего счета должно возвращать пустой Optional")
        void getAccount_NonExistingId_ShouldReturnEmpty() {
            // Arrange
            UUID nonExistingId = UUID.randomUUID();
            when(accountRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            // Act
            Optional<BankAccount> result = accountManager.getAccount(nonExistingId);

            // Assert
            assertTrue(result.isEmpty());
            verify(accountRepository, times(1)).findById(nonExistingId);
        }
    }

    @Nested
    @DisplayName("Тесты получения всех счетов")
    class GetAllAccountsTests {

        @Test
        @DisplayName("Получение всех счетов должно возвращать список")
        void getAllAccounts_ShouldReturnAllAccounts() {
            // Arrange
            List<BankAccount> expectedAccounts = Arrays.asList(
                    testAccount,
                    new BankAccount(UUID.randomUUID(), "Account 2", new BigDecimal("2000.00"))
            );
            when(accountRepository.findAll()).thenReturn(expectedAccounts);

            // Act
            List<BankAccount> result = accountManager.getAllAccounts();

            // Assert
            assertEquals(expectedAccounts.size(), result.size());
            assertEquals(expectedAccounts, result);
            verify(accountRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Получение всех счетов при пустом репозитории должно возвращать пустой список")
        void getAllAccounts_EmptyRepository_ShouldReturnEmptyList() {
            // Arrange
            when(accountRepository.findAll()).thenReturn(List.of());

            // Act
            List<BankAccount> result = accountManager.getAllAccounts();

            // Assert
            assertTrue(result.isEmpty());
            verify(accountRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Тесты обновления счета")
    class UpdateAccountTests {

        @Test
        @DisplayName("Обновление имени существующего счета должно возвращать обновленный счет")
        void updateAccountName_ExistingId_ShouldReturnUpdatedAccount() {
            // Arrange
            String newName = "Updated Account Name";
            when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
            doNothing().when(accountRepository).update(any(BankAccount.class));

            // Act
            BankAccount updated = accountManager.updateAccountName(testId, newName);

            // Assert
            assertEquals(newName, updated.getName());
            assertEquals(testAccount.getBalance(), updated.getBalance());
            verify(accountRepository, times(1)).findById(testId);
            verify(accountRepository, times(1)).update(any(BankAccount.class));
        }

        @Test
        @DisplayName("Обновление имени несуществующего счета должно выбрасывать исключение")
        void updateAccountName_NonExistingId_ShouldThrowException() {
            // Arrange
            UUID nonExistingId = UUID.randomUUID();
            String newName = "New Name";
            when(accountRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.updateAccountName(nonExistingId, newName)
            );

            assertTrue(exception.getMessage().contains("Account not found"));
            verify(accountRepository, times(1)).findById(nonExistingId);
            verify(accountRepository, never()).update(any());
        }

        @Test
        @DisplayName("Обновление имени с пустым именем должно выбрасывать исключение")
        void updateAccountName_EmptyName_ShouldThrowException() {
            // Arrange
            String newName = "   ";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.updateAccountName(testId, newName)
            );

            assertEquals("Account name cannot be empty", exception.getMessage());
            verify(accountRepository, never()).findById(any());
            verify(accountRepository, never()).update(any());
        }
    }

    @Nested
    @DisplayName("Тесты удаления счета")
    class DeleteAccountTests {

        @Test
        @DisplayName("Удаление существующего счета должно проходить успешно")
        void deleteAccount_ExistingId_ShouldDelete() {
            // Arrange
            when(accountRepository.exists(testId)).thenReturn(true);
            doNothing().when(accountRepository).delete(testId);

            // Act
            accountManager.deleteAccount(testId);

            // Assert
            verify(accountRepository, times(1)).exists(testId);
            verify(accountRepository, times(1)).delete(testId);
        }

        @Test
        @DisplayName("Удаление несуществующего счета должно выбрасывать исключение")
        void deleteAccount_NonExistingId_ShouldThrowException() {
            // Arrange
            UUID nonExistingId = UUID.randomUUID();
            when(accountRepository.exists(nonExistingId)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.deleteAccount(nonExistingId)
            );

            assertTrue(exception.getMessage().contains("Account not found"));
            verify(accountRepository, times(1)).exists(nonExistingId);
            verify(accountRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Тесты обновления баланса")
    class UpdateBalanceTests {

        @Test
        @DisplayName("Увеличение баланса на положительную сумму должно обновить баланс")
        void updateBalance_IncreaseBalance_ShouldUpdate() {
            // Arrange
            BigDecimal increase = new BigDecimal("500.00");
            BigDecimal expectedBalance = testAccount.getBalance().add(increase);

            when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
            doNothing().when(accountRepository).update(any(BankAccount.class));

            // Act
            accountManager.updateBalance(testId, increase);

            // Assert
            assertEquals(expectedBalance, testAccount.getBalance());
            verify(accountRepository, times(1)).findById(testId);
            verify(accountRepository, times(1)).update(testAccount);
        }

        @Test
        @DisplayName("Уменьшение баланса на положительную сумму должно обновить баланс")
        void updateBalance_DecreaseBalance_ShouldUpdate() {
            // Arrange
            BigDecimal decrease = new BigDecimal("-300.00");
            BigDecimal expectedBalance = testAccount.getBalance().add(decrease);

            when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
            doNothing().when(accountRepository).update(any(BankAccount.class));

            // Act
            accountManager.updateBalance(testId, decrease);

            // Assert
            assertEquals(expectedBalance, testAccount.getBalance());
            verify(accountRepository, times(1)).findById(testId);
            verify(accountRepository, times(1)).update(testAccount);
        }

        @Test
        @DisplayName("Уменьшение баланса ниже нуля должно выбрасывать исключение")
        void updateBalance_InsufficientFunds_ShouldThrowException() {
            // Arrange
            BigDecimal decrease = new BigDecimal("-2000.00");

            when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountManager.updateBalance(testId, decrease)
            );

            assertEquals("Insufficient funds", exception.getMessage());
            verify(accountRepository, times(1)).findById(testId);
            verify(accountRepository, never()).update(any());
        }
    }
}