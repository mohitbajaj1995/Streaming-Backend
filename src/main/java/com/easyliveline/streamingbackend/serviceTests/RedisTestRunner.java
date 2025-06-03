//package com.easyliveline.assistant.ServiceTests;
//
//import com.easyliveline.assistant.Services.RedisService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//@Component
//public class RedisTestRunner implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(RedisTestRunner.class);
//
//    private final StringRedisTemplate stringRedisTemplate;
//    private final RedisService redisService;
//    private final boolean isTestEnabled = true; // Set to true to enable the test
//
//    @Autowired
//    public RedisTestRunner(StringRedisTemplate stringRedisTemplate, RedisService redisService) {
//        this.stringRedisTemplate = stringRedisTemplate;
//        this.redisService = redisService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        if(isTestEnabled) {
//            logger.info("Starting RedisTestRunner to test deleteKeysContaining...");
//
//            // Define keys to insert
//            Map<String, String> keysToInsert = new HashMap<>();
//            keysToInsert.put("app:user:123_session_data", "user session details for 123");
//            keysToInsert.put("cache:prod:_session_abc:token", "some cached token");
//            keysToInsert.put("config:global:_session_timeout", "3600");
//            keysToInsert.put("app:user:123_profile_info", "user profile data for 123");
//            keysToInsert.put("cache:prod:master_data", "important master data");
//            keysToInsert.put("system:logs:20250514", "log entry");
//            keysToInsert.put("temp_data_session_end", "temporary session artifact");
//            keysToInsert.put("another:key:_SESSION_uppercase", "test for case sensitivity"); // Note: _session_ is lowercase
//
//            // Insert keys
//            logger.info("Inserting sample keys into Redis...");
//            keysToInsert.forEach((key, value) -> {
//                stringRedisTemplate.opsForValue().set(key, value);
//                logger.info("Inserted key: '{}'", key);
//            });
//
//            // List keys before deletion (optional, for logging)
//            Set<String> keysBefore = stringRedisTemplate.keys("*"); // WARNING: KEYS * is not for production
//            logger.info("Keys in Redis before deletion (example check): {}", keysBefore);
//
//            // The partial string to test deletion with
//            String partialToDelete = "_session_";
//            logger.info("Attempting to delete keys containing: '{}'", partialToDelete);
//
//            // Call the service method
//            redisService.deleteKeysContaining(partialToDelete);
//        }
////        logger.info("Deletion process completed for partial string: '{}'", partialToDelete);
////        logger.info("--------------------------------------------------------------------");
////        logger.info("VERIFICATION STEP: Please use redis-cli to check the keys.");
////        logger.info("Connect to your Redis instance (e.g., 'redis-cli') and use 'KEYS *' to see remaining keys.");
////        logger.info("Expected remaining keys (examples that should NOT be deleted):");
////        logger.info("- app:user:123_profile_info");
////        logger.info("- cache:prod:master_data");
////        logger.info("- system:logs:20250514");
////        logger.info("- another:key:_SESSION_uppercase (because SCAN pattern is case-sensitive)");
////        logger.info("Keys containing '{}' should be gone.", partialToDelete);
////        logger.info("--------------------------------------------------------------------");
//
//
//        // You can add more programmatic checks here if needed, e.g.,
//        // Set<String> keysAfter = stringRedisTemplate.keys("*");
//        // logger.info("Keys in Redis AFTER deletion (example check): {}", keysAfter);
//        // assert keysAfter does not contain "app:user:123_session_data", etc.
//    }
//}