package org.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.example.console.ConsoleApp;
import org.example.di.TigerBankModule;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  Добро пожаловать в ТигрБанк!  ");
        System.out.println("=================================");

        try {
            // Создаем DI-контейнер
            Injector injector = Guice.createInjector(new TigerBankModule());

            // Получаем экземпляр консольного приложения и запускаем его
            ConsoleApp consoleApp = injector.getInstance(ConsoleApp.class);
            consoleApp.start();

        } catch (Exception e) {
            System.err.println("Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}