package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CricketMazzaService {

    private final WebClient webClient;

    public CricketMazzaService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://applive.cricketbox.in")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<Map<String, Object>> loadFullSchedule(int maxPages) {
        return ExceptionWrapper.handle(() -> {
            log.info("Starting to load full schedule with maxPages = {}", maxPages);
            List<Map<String, Object>> fullSchedule = new ArrayList<>();
            int currentPage = 1;
            boolean getNextPage = true;

            while (getNextPage) {
                log.info("Fetching match data for page {}", currentPage);
                List<Map<String, Object>> currentPageSchedule = getMatchData(currentPage, true, "T20");
                if (currentPageSchedule != null && !currentPageSchedule.isEmpty()) {
                    log.info("Fetched {} matches for page {}", currentPageSchedule.size(), currentPage);
                    fullSchedule.addAll(currentPageSchedule);
                } else {
                    log.info("No more matches found on page {}", currentPage);
                    getNextPage = false;
                }

                currentPage++;
                if (maxPages > 0 && currentPage > maxPages) {
                    log.info("Reached maximum page limit: {}", maxPages);
                    getNextPage = false;
                }
            }

            log.info("Finished loading full schedule. Total matches fetched: {}", fullSchedule.size());
            return fullSchedule;
        });
    }

    public List<Map<String, Object>> getMatchData(int pageNumber, boolean fixturesOrRecent, String recentType) {
        return ExceptionWrapper.handle(() -> {
            log.info("Getting match data: page={}, fixturesOrRecent={}, recentType={}", pageNumber, fixturesOrRecent, recentType);
            Map<String, Object> response = fetchMatchData(pageNumber - 1, fixturesOrRecent, recentType);

            if (response != null && response.containsKey("game_schedulesv1Result")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("game_schedulesv1Result");
                log.info("Processing {} matches from response", results.size());
                return processMatchData(results);
            }

            log.info("No 'game_schedulesv1Result' found in response for page {}", pageNumber);
            return List.of();
        });
    }

    private List<Map<String, Object>> processMatchData(List<Map<String, Object>> matchData) {
        return ExceptionWrapper.handle(() -> {
            log.info("Starting to process {} matches", matchData.size());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a").withZone(ZoneId.of("UTC"));

            for (Map<String, Object> match : matchData) {
                String team1Image = (String) match.get("TEAM1IMAGE");
                if (team1Image != null && !"TBC.png".equalsIgnoreCase(team1Image)) {
                    match.put("TEAM1IMAGE", "https://images.weserv.nl/?url=https://img.cricketmazza.com/" + team1Image + "?alt=media");
                }

                String team2Image = (String) match.get("TEAM2IMAGE");
                if (team2Image != null && !"TBC.png".equalsIgnoreCase(team2Image)) {
                    match.put("TEAM2IMAGE", "https://images.weserv.nl/?url=https://img.cricketmazza.com/" + team2Image + "?alt=media");
                }

                String seriesName = (String) match.get("SERIES_NAME");
                if (seriesName != null) {
                    seriesName = seriesName
                            .replaceAll("\\s*,?\\s*\\d{4}$", "")
                            .replaceAll("(?i)\\s*only\\s*test\\s*", "");
                    match.put("SERIES_NAME", seriesName.trim());
                }

                String matchType = match.containsKey("OVER_BALL")
                        ? String.valueOf(match.get("OVER_BALL"))
                        : String.valueOf(match.get("GAME_TYPE"));

                String matchTypeNormalized = switch (matchType) {
                    case "10" -> "t10";
                    case "20" -> "t20";
                    case "50" -> "odi";
                    default -> matchType.toLowerCase();
                };
                match.put("type", matchTypeNormalized);

                Object gameTimeObj = match.get("GAME_TIME");
                if (gameTimeObj != null) {
                    try {
                        long gameTimeMillis = Long.parseLong(gameTimeObj.toString()) * 1000;
                        match.put("datetime", formatter.format(Instant.ofEpochMilli(gameTimeMillis)));
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse GAME_TIME: {}", gameTimeObj);
                    }
                }
            }

            log.info("Finished processing matches");
            return matchData;
        });
    }

    public Map<String, Object> fetchMatchData(int pageIndex, boolean fixturesOrRecent, String recentType) {
        try {
            log.info("Fetching match data from Cricketmazza: pageIndex={}, fixturesOrRecent={}, recentType={}",
                    pageIndex, fixturesOrRecent, recentType);

            Map<String, Object> requestBody = Map.of(
                    "GAME_SCHEDULE", Map.of(
                            "GAMEDATE", "0",
                            "GAME_TYPE", fixturesOrRecent ? "ALL" : recentType,
                            "PAGEINDEX", pageIndex,
                            "UPCOMING", fixturesOrRecent ? "1" : "0"
                    )
            );

            Map<String, Object> result = webClient.post()
                    .uri("/games.svc/game_schedulesv1_v5")
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, "Basic Q01hemFhOmNNYXphQQ==");
                        headers.set(HttpHeaders.USER_AGENT, "CMm0B!LE");
                        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip");
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("HTTP error while fetching data. Status: {}, Body: {}", response.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("HTTP Error: " + response.statusCode() + ", Body: " + errorBody));
                            }))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof SocketException ||
                                    throwable instanceof WebClientResponseException))
                    .block();

            log.info("Successfully fetched data from Cricketmazza for pageIndex={}", pageIndex);
            return result;
        } catch (Exception e) {
            log.error("Error fetching match data from Cricketmazza", e);
            throw new RuntimeException("Error fetching match data from Cricketmazza", e);
        }
    }
}
