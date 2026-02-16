package org.example.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.example.dto.DataExporter;
import org.example.dto.DataImporter;
import org.example.dto.JsonDataHandler;
import org.example.repository.*;
import org.example.service.AccountManager;
import org.example.service.AnalyticsService;
import org.example.service.CategoryManager;
import org.example.service.OperationManager;



public class TigerBankModule extends AbstractModule {

    @Override
    protected void configure() {
        // Привязка репозиториев к их in-memory реализациям
        bind(BankAccountRepository.class).to(InMemoryBankAccountRepository.class).in(Singleton.class);
        bind(CategoryRepository.class).to(InMemoryCategoryRepository.class).in(Singleton.class);
        bind(OperationRepository.class).to(InMemoryOperationRepository.class).in(Singleton.class);

        // Привязка менеджеров
        bind(AccountManager.class);
        bind(CategoryManager.class);
        bind(OperationManager.class);

        // Привязка сервиса аналитики
        bind(AnalyticsService.class);

        // Привязка обработчиков импорта/экспорта
        bind(DataExporter.class).to(JsonDataHandler.class).in(Singleton.class);
        bind(DataImporter.class).to(JsonDataHandler.class).in(Singleton.class);
    }
}