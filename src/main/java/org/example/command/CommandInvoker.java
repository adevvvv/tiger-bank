package org.example.command;

import org.example.service.StatisticsService;

import java.util.HashMap;
import java.util.Map;

public class CommandInvoker {
    private final Map<Integer, Command> commands = new HashMap<>();
    private final StatisticsService statisticsService;

    public CommandInvoker(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public void registerCommand(int key, Command command) {
        commands.put(key, new TimedCommand(command, statisticsService));
    }

    public void executeCommand(int key) {
        Command command = commands.get(key);
        if (command != null) {
            System.out.println("\n▶️ Выполняется: " + command.getDescription());
            command.execute();
        } else {
            System.out.println("❌ Неверный выбор!");
        }
    }

    public void printMenu() {
        System.out.println("\n=== ИНТЕРАКТИВНОЕ МЕНЮ ===");
        commands.forEach((key, command) ->
                System.out.println(key + ". " + command.getDescription()));
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    public boolean hasCommand(int key) {
        return commands.containsKey(key);
    }
}