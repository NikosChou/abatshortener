package de.abat.shortener.statistics.boundary;

import de.abat.shortener.statistics.control.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/{shortenedId}")
    public StatisticsDto getStatistics(@PathVariable UUID shortenedId) {
        return statisticsService.getStatistics(shortenedId);
    }
}
