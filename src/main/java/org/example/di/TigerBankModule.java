package org.example.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.example.dto.*;
import org.example.repository.*;
import org.example.service.*;

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
        bind(JsonDataHandler.class).in(Singleton.class);
        bind(YamlDataHandler.class).in(Singleton.class);
        bind(CsvDataHandler.class).in(Singleton.class);
        bind(DataHandlerFactory.class).in(Singleton.class);

        // По умолчанию используем JSON для обратной совместимости
        bind(DataExporter.class).to(JsonDataHandler.class);
        bind(DataImporter.class).to(JsonDataHandler.class);
    }
}