package com.easyliveline.streamingbackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class ScheduleRunner {

    private final CricketMazzaService cricketMazzaService;
    private final RedisService redisService;
    private final Executor virtualThreadExecutor;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ScheduleRunner(CricketMazzaService cricketMazzaService, RedisService redisService,
                          @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.cricketMazzaService = cricketMazzaService;
        this.redisService = redisService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @PostConstruct
    public void init() {
        log.info("ScheduleRunner initialized at {}", Instant.now());
        // Initial fetch on startup
        virtualThreadExecutor.execute(this::fetchSchedule);
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour on the hour
    public void fetchScheduleHourly() {
        log.info("Scheduled fetchScheduleHourly triggered at {}", Instant.now());
        // Submit fetchSchedule to virtual thread executor explicitly
        virtualThreadExecutor.execute(this::fetchSchedule);
    }

    private void fetchSchedule() {
        log.info("Schedule fetch started at {}", Instant.now());
        int maxRetries = 3;
        int attempt = 0;
        boolean success = false;

        while (attempt < maxRetries && !success) {
            try {
                List<Map<String, Object>> data = cricketMazzaService.loadFullSchedule(5);

                Map<String, Object> result = new HashMap<>();
                result.put("timestamp", Instant.now().toString());
                result.put("data", data);

                String json = new ObjectMapper().writeValueAsString(result);

                redisService.setString("schedule:", json);
                success = true;
                log.info("Schedule fetched and stored in Redis.");
            } catch (Exception e) {
                attempt++;
                log.error("Attempt {} failed: {}", attempt, e.getMessage(), e);
                // Instead of blocking Thread.sleep, schedule a delayed retry asynchronously
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    scheduledExecutorService.schedule(latch::countDown, 2L * attempt, TimeUnit.SECONDS);
                    latch.await(); // This await is on ScheduledExecutorService thread, safe to block here
                } catch (InterruptedException ie) {
                    log.warn("Sleep interrupted during backoff");
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!success) {
            log.error("Schedule fetch failed after {} retries.", maxRetries);
        }
        log.info("Schedule fetch ended at {}", Instant.now());
    }
}
