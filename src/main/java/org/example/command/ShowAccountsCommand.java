package org.example.command;

import org.example.service.AccountManager;

public class ShowAccountsCommand implements Command {
    private final AccountManager accountManager;

    public ShowAccountsCommand(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Счета ---");
        var accounts = accountManager.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("Счета отсутствуют");
        } else {
            accounts.forEach(acc ->
                    System.out.println("• " + acc.getId() + " | " + acc.getName() + " | " +
                            acc.getBalance() + " руб."));
        }
    }

    @Override
    public String getDescription() {
        return "Просмотр счетов";
    }
}