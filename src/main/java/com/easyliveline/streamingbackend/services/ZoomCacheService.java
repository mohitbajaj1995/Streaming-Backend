package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.models.Zoom;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class ZoomCacheService {

    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;

    public ZoomCacheService(WebClient.Builder webClientBuilder, RedisTemplate<String, Object> redisTemplate, RedisService redisService) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.zoom.us/v2")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
    }

    public String getUserZakToken(String userIdOrEmail, String authAccessToken) {
        return ExceptionWrapper.handle(() -> {
            System.out.println("Fetching zakToken token from Zoom API...");
            String encodedUserId = UriUtils.encodePath(userIdOrEmail.trim(), StandardCharsets.UTF_8);

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{userId}/token")
                            .queryParam("type", "zak")
                            .build(encodedUserId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authAccessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(json -> json.path("token").asText(""))
                    .block();
        });
    }

//    @Cacheable(value = "oauthTokens", key = "#apiKey + '-' + #apiSecret + '-' + #accountId")
    public String getOauthAccessToken(Zoom zoom) {
        return ExceptionWrapper.handle(() -> {
            String cacheOAuthTokenKey = "oauthToken:_zoom_" + zoom.getId();

            // Check Redis for cached token
            if (redisService.hasKey(cacheOAuthTokenKey)) {
                return redisService.getString(cacheOAuthTokenKey);
            }

            // Fetch token from Zoom API
            System.out.println("Fetching OAuth token from Zoom API...");
            WebClient tokenClient = WebClient.builder()
                    .baseUrl("https://zoom.us")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(zoom.getApiKey(), zoom.getApiSecret()))
                    .build();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "account_credentials");
            formData.add("account_id", zoom.getAccountId());

            JsonNode response = tokenClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("access_token")) {
                String accessToken = response.path("access_token").asText(null);
                int expiresIn = response.path("expires_in").asInt(3600); // fallback to 1 hour

                // Cache token in Redis with expiration
                redisService.setString(cacheOAuthTokenKey, accessToken, expiresIn, TimeUnit.SECONDS);
                return accessToken;
            }
            return null;
        });
    }


    private String getBasicAuthHeader(String apiKey, String apiSecret) {
        return ExceptionWrapper.handle(() -> {
            String auth = apiKey + ":" + apiSecret;
            return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        });
    }
}
