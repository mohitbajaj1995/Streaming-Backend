package com.easyliveline.streamingbackend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils; // For checking empty collections

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // Constants for batch operations
    private static final int DELETE_BATCH_SIZE = 500;
    private static final int SCAN_ITERATION_COUNT = 1000; // Number of keys to fetch per SCAN iteration

    @Autowired
    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // --- Key Management Methods ---

    /**
     * Deletes a single key.
     *
     * @param key The key to delete.
     * @return true if the key was deleted, false otherwise (e.g., key not found).
     */
    public boolean deleteKey(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Attempted to delete a null or empty key.");
            return false;
        }
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting key '{}': {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Deletes multiple keys.
     *
     * @param keys A list of keys to delete.
     */
    public void deleteKeys(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            log.warn("Attempted to delete an empty or null list of keys.");
            return;
        }
        try {
            Long count = redisTemplate.delete(keys);
            log.debug("Deleted keys: {}, count: {}", keys, count);
        } catch (Exception e) {
            log.error("Error deleting keys {}: {}", keys, e.getMessage(), e);
        }
    }

    /**
     * Deletes all keys from Redis that contain the given partial string.
     * This method uses SCAN for iteration, which is suitable for production environments.
     * Keys are deleted in batches for better performance.
     *
     * @param partial The substring to match within Redis keys. The match is case-sensitive
     * and uses glob-style patterns (e.g., "*" matches any sequence).
     */
    public void deleteKeysContaining(String partial) {
        if (partial == null || partial.isEmpty()) {
            log.warn("Partial string for deleting keys cannot be null or empty.");
            return;
        }
        log.info("Attempting to delete keys containing: '{}'", partial);
        redisTemplate.execute((RedisConnection connection) -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match("*" + partial + "*") // Glob-style pattern
                    .count(SCAN_ITERATION_COUNT)
                    .build();

            RedisKeyCommands keyCommands = connection.keyCommands();

            @SuppressWarnings("unchecked")
            RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();

            List<String> keysToDeleteInBatch = new ArrayList<>(DELETE_BATCH_SIZE);
            long totalKeysScanned = 0;
            long totalKeysMarkedForDeletion = 0;

            try (Cursor<byte[]> cursor = keyCommands.scan(options)) {
                while (cursor.hasNext()) {
                    totalKeysScanned++;
                    byte[] keyBytes = cursor.next();
                    String key = keySerializer.deserialize(keyBytes);
                    if (key != null) {
                        keysToDeleteInBatch.add(key);
                        totalKeysMarkedForDeletion++;
                        if (keysToDeleteInBatch.size() >= DELETE_BATCH_SIZE) {
                            deleteKeys(new ArrayList<>(keysToDeleteInBatch)); // Use existing batch delete
                            keysToDeleteInBatch.clear();
                        }
                    }
                }
                if (!keysToDeleteInBatch.isEmpty()) {
                    deleteKeys(new ArrayList<>(keysToDeleteInBatch));
                    keysToDeleteInBatch.clear();
                }
            } catch (Exception e) {
                log.error("Error during SCAN operation for pattern '*{}*': {}", partial, e.getMessage(), e);
            }
            log.info("Finished deleting keys containing '{}'. Scanned approximately {} keys, marked {} for deletion.",
                    partial, totalKeysScanned * SCAN_ITERATION_COUNT, totalKeysMarkedForDeletion); // Note: totalKeysScanned is iterations
            return null;
        });
    }

    /**
     * Checks if a key exists.
     *
     * @param key The key to check.
     * @return true if the key exists, false otherwise.
     */
    public boolean hasKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Error checking existence of key '{}': {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sets the time to live (TTL) for a key.
     *
     * @param key     The key.
     * @param timeout The timeout value.
     * @param unit    The time unit for the timeout.
     * @return true if the timeout was set, false otherwise.
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        if (key == null || key.isEmpty() || unit == null) {
            log.warn("Key, timeout, or unit cannot be null/empty for expire operation.");
            return false;
        }
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting TTL for key '{}': {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets the time to live (TTL) for a key.
     *
     * @param key  The key.
     * @param unit The time unit for the result.
     * @return The remaining TTL in the specified unit, or a negative value if the key does not exist or has no TTL.
     */
    public Long getExpire(String key, TimeUnit unit) {
        if (key == null || key.isEmpty() || unit == null) {
            return -2L; // Indicates an error or invalid input
        }
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("Error getting TTL for key '{}': {}", key, e.getMessage(), e);
            return -2L; // Indicates an error during operation
        }
    }

    // --- String Operations ---

    /**
     * Sets a string value for a key.
     *
     * @param key   The key.
     * @param value The string value.
     */
    public void setString(String key, String value) {
        if (key == null || key.isEmpty()) {
            log.warn("Key cannot be null or empty for setString operation.");
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set string for key: '{}'", key);
        } catch (Exception e) {
            log.error("Error setting string for key '{}': {}", key, e.getMessage(), e);
        }
    }

    /**
     * Sets a string value for a key with a specified TTL.
     *
     * @param key     The key.
     * @param value   The string value.
     * @param timeout The timeout value.
     * @param unit    The time unit for the timeout.
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        if (key == null || key.isEmpty() || unit == null) {
            log.warn("Key, timeout, or unit cannot be null/empty for setString with TTL operation.");
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set string with TTL for key: '{}', timeout: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting string with TTL for key '{}': {}", key, e.getMessage(), e);
        }
    }

    /**
     * Gets a string value by key.
     *
     * @param key The key.
     * @return The string value, or null if the key does not exist or an error occurs.
     */
    public String getString(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting string for key '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Increments the integer value of a key by one.
     * If the key does not exist, it is set to 0 before performing the operation.
     *
     * @param key The key.
     * @return The value of key after the increment, or null if an error occurs or the key holds a wrong type of value.
     */
    public Long increment(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Error incrementing key '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Increments the integer value of a key by the given delta.
     *
     * @param key   The key.
     * @param delta The amount to increment by.
     * @return The value of key after the increment, or null if an error occurs.
     */
    public Long incrementBy(String key, long delta) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Error incrementing key '{}' by delta {}: {}", key, delta, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Decrements the integer value of a key by one.
     *
     * @param key The key.
     * @return The value of key after the decrement, or null if an error occurs.
     */
    public Long decrement(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Error decrementing key '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Decrements the integer value of a key by the given delta.
     *
     * @param key   The key.
     * @param delta The amount to decrement by.
     * @return The value of key after the decrement, or null if an error occurs.
     */
    public Long decrementBy(String key, long delta) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            // Decrement is increment by negative delta
            return redisTemplate.opsForValue().increment(key, -delta);
        } catch (Exception e) {
            log.error("Error decrementing key '{}' by delta {}: {}", key, delta, e.getMessage(), e);
            return null;
        }
    }

    // --- List Operations ---

    /**
     * Appends a value to the right end of a list.
     *
     * @param key   The list key.
     * @param value The value to append.
     * @return The length of the list after the append operation, or null on error.
     */
    public Long listRightPush(String key, String value) {
        if (key == null || key.isEmpty()) return null;
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("Error right pushing to list '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Appends multiple values to the right end of a list.
     *
     * @param key    The list key.
     * @param values The values to append.
     * @return The length of the list after the append operation, or null on error.
     */
    public Long listRightPushAll(String key, List<String> values) {
        if (key == null || key.isEmpty() || CollectionUtils.isEmpty(values)) return null;
        try {
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("Error right pushing all to list '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves a range of elements from a list.
     *
     * @param key   The list key.
     * @param start The start index (0-based).
     * @param end   The end index (use -1 for the last element).
     * @return A list of elements, or an empty list if key does not exist or range is invalid.
     */
    public List<String> listRange(String key, long start, long end) {
        if (key == null || key.isEmpty()) return new ArrayList<>();
        try {
            List<String> range = redisTemplate.opsForList().range(key, start, end);
            return range != null ? range : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting range from list '{}': {}", key, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Removes and returns the first element of a list.
     *
     * @param key The list key.
     * @return The removed element, or null if the list is empty or key does not exist.
     */
    public String listLeftPop(String key) {
        if (key == null || key.isEmpty()) return null;
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Error left popping from list '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets the size of a list.
     * @param key The list key.
     * @return The size of the list, or 0 if the key does not exist or is not a list.
     */
    public Long listSize(String key) {
        if (key == null || key.isEmpty()) return 0L;
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("Error getting size of list '{}': {}", key, e.getMessage(), e);
            return 0L;
        }
    }


    // --- Set Operations ---

    /**
     * Adds one or more members to a set.
     *
     * @param key     The set key.
     * @param members The members to add.
     * @return The number of elements that were added to the set, not including all the elements already present. Null on error.
     */
    public Long setAdd(String key, String... members) {
        if (key == null || key.isEmpty() || members == null || members.length == 0) return null;
        try {
            return redisTemplate.opsForSet().add(key, members);
        } catch (Exception e) {
            log.error("Error adding to set '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets all members of a set.
     *
     * @param key The set key.
     * @return A set of members, or an empty set if key does not exist.
     */
    public Set<String> setMembers(String key) {
        if (key == null || key.isEmpty()) return Set.of();
        try {
            Set<String> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : Set.of();
        } catch (Exception e) {
            log.error("Error getting members from set '{}': {}", key, e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * Checks if a member exists in a set.
     *
     * @param key    The set key.
     * @param member The member to check.
     * @return true if the member exists in the set, false otherwise.
     */
    public boolean setIsMember(String key, String member) {
        if (key == null || key.isEmpty() || member == null) return false;
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, member);
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("Error checking member in set '{}': {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Removes members from a set.
     * @param key The set key.
     * @param members The members to remove.
     * @return The number of members removed from the set, not including non-existing members. Null on error.
     */
    public Long setRemove(String key, Object... members) {
        if (key == null || key.isEmpty() || members == null || members.length == 0) return null;
        try {
            return redisTemplate.opsForSet().remove(key, members);
        } catch (Exception e) {
            log.error("Error removing members from set '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }


    // --- Hash Operations ---

    /**
     * Sets a field-value pair in a hash.
     *
     * @param key     The hash key.
     * @param hashKey The field (key within the hash).
     * @param value   The value for the field.
     */
    public void hashPut(String key, String hashKey, String value) {
        if (key == null || key.isEmpty() || hashKey == null || hashKey.isEmpty()) return;
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            log.debug("Put into hash '{}', field '{}'", key, hashKey);
        } catch (Exception e) {
            log.error("Error putting into hash '{}', field '{}': {}", key, hashKey, e.getMessage(), e);
        }
    }

    /**
     * Gets a value from a hash by field.
     *
     * @param key     The hash key.
     * @param hashKey The field.
     * @return The value, or null if the field or hash does not exist.
     */
    public String hashGet(String key, String hashKey) {
        if (key == null || key.isEmpty() || hashKey == null || hashKey.isEmpty()) return null;
        try {
            Object value = redisTemplate.opsForHash().get(key, hashKey);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Error getting from hash '{}', field '{}': {}", key, hashKey, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets all field-value pairs from a hash.
     *
     * @param key The hash key.
     * @return A map of field-value pairs, or an empty map if the hash does not exist.
     */
    public Map<Object, Object> hashGetAll(String key) {
        if (key == null || key.isEmpty()) return Map.of();
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            return entries != null ? entries : Map.of();
        } catch (Exception e) {
            log.error("Error getting all from hash '{}': {}", key, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * Deletes one or more hash fields.
     * @param key The hash key.
     * @param hashKeys The fields to delete.
     * @return The number of fields that were removed. Null on error.
     */
    public Long hashDelete(String key, Object... hashKeys) {
        if (key == null || key.isEmpty() || hashKeys == null || hashKeys.length == 0) return null;
        try {
            return redisTemplate.opsForHash().delete(key, hashKeys);
        } catch (Exception e) {
            log.error("Error deleting from hash '{}', fields '{}': {}", key, hashKeys, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Checks if a field exists in a hash.
     * @param key The hash key.
     * @param hashKey The field to check.
     * @return true if the field exists, false otherwise.
     */
    public boolean hashHasKey(String key, String hashKey) {
        if (key == null || key.isEmpty() || hashKey == null || hashKey.isEmpty()) return false;
        try {
            return redisTemplate.opsForHash().hasKey(key, hashKey);
        } catch (Exception e) {
            log.error("Error checking field in hash '{}', field '{}': {}", key, hashKey, e.getMessage(), e);
            return false;
        }
    }
}
