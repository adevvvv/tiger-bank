package org.example.dto;


import org.example.model.BankAccount;
import org.example.model.Category;
import org.example.model.Operation;

import java.util.List;

public interface DataExporter {
    void export(List<BankAccount> accounts,
                List<Category> categories,
                List<Operation> operations,
                String filePath);
}

