package org.example.command;

import org.example.service.StatisticsService;

public class ShowStatisticsCommand implements Command {
    private final StatisticsService statisticsService;

    public ShowStatisticsCommand(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public void execute() {
        statisticsService.printStatistics();
    }

    @Override
    public String getDescription() {
        return "Показать статистику выполнения команд";
    }
}