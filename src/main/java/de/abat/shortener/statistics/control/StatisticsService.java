package de.abat.shortener.statistics.control;

import de.abat.shortener.events.ShortUrlCreated;
import de.abat.shortener.events.ShortUrlVisited;
import de.abat.shortener.statistics.boundary.StatisticsDto;
import de.abat.shortener.statistics.entity.Statistics;
import de.abat.shortener.statistics.entity.StatisticsRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @ApplicationModuleListener
    public void newShortenedUrl(ShortUrlCreated newShortRequest) throws InterruptedException {
        log.info("new event {}", newShortRequest);
        statisticsRepository.save(new Statistics(UUID.randomUUID(), newShortRequest.shortenedUrl()));
    }


    @EventListener
    @Transactional
    public void newShortenedUrl(ShortUrlVisited shortUrlVisited) {
        log.info("url visited {}", shortUrlVisited);
        Statistics entity = statisticsRepository.findByShortenedUrl(shortUrlVisited.id());
        entity.setCount(entity.getCount() + 1);
    }

    @Transactional
    public StatisticsDto getStatistics(UUID shortenedId) {
        var entity = statisticsRepository.findByShortenedUrl(shortenedId);
        return new StatisticsDto(entity.getCount());
    }
}
