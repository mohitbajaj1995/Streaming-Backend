package com.easyliveline.streamingbackend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default configuration (optional fallback)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // fallback TTL
                .disableCachingNullValues();

        // Specific TTLs per cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("oauthTokens", defaultConfig.entryTtl(Duration.ofMinutes(55)));
        cacheConfigs.put("zakTokens", defaultConfig.entryTtl(Duration.ofMinutes(90)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}



//@Bean
//public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//    RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//            .entryTtl(Duration.ofMinutes(10))  // Set TTL for 10 minutes
//            .disableCachingNullValues();
//
//    return RedisCacheManager.builder(redisConnectionFactory)
//            .cacheDefaults(cacheConfiguration)
//            .build();
//}
