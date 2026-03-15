package org.example.service;

import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class StatisticsService {
    private final Map<String, List<Long>> commandExecutionTimes = new ConcurrentHashMap<>();

    public void recordCommandExecution(String commandDescription, long durationMs) {
        commandExecutionTimes.computeIfAbsent(commandDescription, k -> new ArrayList<>())
                .add(durationMs);
        System.out.println("Время выполнения '" + commandDescription + "': " + durationMs + " мс");
    }

    public void printStatistics() {
        System.out.println("\n=== СТАТИСТИКА ВЫПОЛНЕНИЯ КОМАНД ===");
        if (commandExecutionTimes.isEmpty()) {
            System.out.println("Статистика отсутствует");
            return;
        }

        commandExecutionTimes.forEach((command, times) -> {
            double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0);
            long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
            long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long totalTime = times.stream().mapToLong(Long::longValue).sum();

            System.out.println("\n" + command + ":");
            System.out.println("   Количество вызовов: " + times.size());
            System.out.println("   Среднее время: " + String.format("%.2f", avgTime) + " мс");
            System.out.println("   Минимальное время: " + minTime + " мс");
            System.out.println("   Максимальное время: " + maxTime + " мс");
            System.out.println("   Общее время: " + totalTime + " мс");
        });
    }

}