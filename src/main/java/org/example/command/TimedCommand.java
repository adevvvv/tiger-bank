package org.example.command;

import org.example.service.StatisticsService;

public class TimedCommand implements Command {
    private final Command command;
    private final StatisticsService statisticsService;

    public TimedCommand(Command command, StatisticsService statisticsService) {
        this.command = command;
        this.statisticsService = statisticsService;
    }

    @Override
    public void execute() {
        long startTime = System.nanoTime();
        try {
            command.execute();
        } finally {
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000; // конвертация в миллисекунды
            statisticsService.recordCommandExecution(command.getDescription(), durationMs);
        }
    }

    @Override
    public String getDescription() {
        return command.getDescription();
    }
}