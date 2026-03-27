package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.Repository.AuditLogRepository;
import com.ashwinsi.bankingApplication.Utils.Constants;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {
    //TODO in the future we will use redis so that cache will be invalid in based on time
    // caching logic for attempts storing
    private final Map<String, Integer> attemptsCache = new HashMap<>();
    // caching logic for the last time record of the attempt
    private final Map<String, LocalDateTime> attemptTime = new HashMap<>();

    private String keyBuilder(UUID userId, String action){
        return userId + "-" + action;
    }

    private void checkIsCacheValid(String key, int cacheExpirationTime){
        if(!attemptTime.containsKey(key)){
            attemptTime.put(key, LocalDateTime.now());
            attemptsCache.put(key, 0);
            return;
        }

        //cache is valid of only for 5 mins
        LocalDateTime lastActionTime = attemptTime.get(key);
        LocalDateTime currTime = LocalDateTime.now();

        if(currTime.isAfter(lastActionTime.plusMinutes(cacheExpirationTime))){
            attemptTime.put(key, currTime);
            attemptsCache.put(key, 0);
        }
    }

    private void updateCache(String key){
        attemptTime.put(key, LocalDateTime.now());
        attemptsCache.put(key, attemptsCache.get(key) + 1);
    }

    public void updateCache(UUID userId, String action){
        String key = keyBuilder(userId, action);
        attemptTime.put(key, LocalDateTime.now());
        attemptsCache.put(key, attemptsCache.get(key) + 1);
    }


    public boolean isAllowedToPerform(UUID userId, String action){
        String cacheKey = keyBuilder(userId, action);

        Integer [] mappingValue = Constants.ACTION_MAPPING.get(action);
        int attemptsThreshold = mappingValue[0];
        int cacheExpirationTime = mappingValue[1];

        checkIsCacheValid(cacheKey, cacheExpirationTime);

        //if user is allowed to perform the action we are returning as true and increasing the cache rate
        int timesActionPerformed = attemptsCache.get(cacheKey);

        if(timesActionPerformed > attemptsThreshold){
            return false;
        }

        updateCache(cacheKey);

        return true;
    }

    // Runs every 10 minutes to clear stale cache entries
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10 mins in milliseconds
    public void evictStaleCache() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        attemptTime.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoff)) {
                attemptsCache.remove(entry.getKey()); // Clean companion map too
                return true;
            }
            return false;
        });

        System.out.println("[AuditLogService] Cache eviction ran. Remaining entries: " + attemptsCache.size());
    }
}
