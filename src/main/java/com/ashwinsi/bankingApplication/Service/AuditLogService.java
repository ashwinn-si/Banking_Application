package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.Utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuditLogService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public AuditLogService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String keyBuilder(UUID userId, String action) {
        return "rate_limit:" + userId + ":" + action;
    }

    public void updateCache(UUID userId, String action) {
        String key = keyBuilder(userId, action);
        Integer[] mappingValue = Constants.ACTION_MAPPING.get(action);

        if (mappingValue != null) {
            int cacheExpirationTime = mappingValue[1];

            // Increment the counter
            Long count = redisTemplate.opsForValue().increment(key);

            // If this is the first attempt, set the Time-To-Live (TTL)
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(cacheExpirationTime));
            }
        }
    }

    public boolean isAllowedToPerform(UUID userId, String action) {
        String cacheKey = keyBuilder(userId, action);

        Integer[] mappingValue = Constants.ACTION_MAPPING.get(action);
        if (mappingValue == null) {
            return true; // If action is unmapped, default to allowing it
        }

        int attemptsThreshold = mappingValue[0];
        int cacheExpirationTime = mappingValue[1];

        // Fetch current attempts from Redis
        String currentAttemptsStr = redisTemplate.opsForValue().get(cacheKey);
        int timesActionPerformed =
                currentAttemptsStr != null ? Integer.parseInt(currentAttemptsStr) : 0;

        // If the threshold is reached or exceeded, block the action
        if (timesActionPerformed >= attemptsThreshold) {
            return false;
        }

        // Action is allowed, increment the counter natively in Redis
        Long newCount = redisTemplate.opsForValue().increment(cacheKey);

        // If this is the first execution in the window, set the expiry
        if (newCount != null && newCount == 1L) {
            redisTemplate.expire(cacheKey, Duration.ofMinutes(cacheExpirationTime));
        }

        return true;
    }
}
