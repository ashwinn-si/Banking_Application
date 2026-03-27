# Moving Rate Limiting from In-Memory HashMaps to Redis

This document explains the rationale behind refactoring `AuditLogService` from native Java `HashMap` caches to a distributed Redis cache.

## 1. Why Migrating off HashMaps was Necessary (The Problem)

- **Memory Leaks & Eviction:** The old implementation required a manual background cron job (`@Scheduled(fixedRate = 10 * 60 * 1000)`) to iterate through HashMaps and remove old entries. If the cron job failed or the app scaled to millions of users, the Java Heap would fill up leading to an `OutOfMemoryError`.
- **Concurrency & Race Conditions:** Standard `HashMap` is not thread-safe. Overlapping requests modifying the map at the exact same millisecond could cause counts to be overwritten or missed. Even `ConcurrentHashMap` requires careful handling to increment safely without race conditions.
- **Microservices Scaling:** A local memory map doesn’t work horizontally. If we deploy 3 instances of the Banking Application, a user gets 5 tries _per server_ (15 total) instead of 5 tries globally.

## 2. Why Redis? (The Solution)

- **Native Support for TTL (Time-To-Live):** Redis natively supports key expiration (`EXPIRE`). Redis automatically deletes the counter memory when the cache time expires, allowing us to delete our messy cron-job logic. Freeing up JVM threads.
- **Single-Threaded Atomic Increments:** Because Redis executes commands in a single-threaded event loop, calling `INCR` is perfectly atomic. It natively solves the race condition challenge without requiring Java `synchronized` blocks.
- **Distributed & Stateless Servers:** Now that the state is externalized to Redis, our Spring Boot application becomes "stateless." We can scale to 10 instances, and they all read from the same Redis source of truth.

## 3. Implementation Details

- **`StringRedisTemplate`:** We use this Spring tool because our rate-limit keys are strings (`rate_limit:{userId}:LOGIN`) and our values are numeric string representations, which `INCR` handles perfectly.
- **The Logic:**
  1. Read the key. If it exceeds the threshold limit, return false.
  2. If valid, increment the key using `redisTemplate.opsForValue().increment(key)`.
  3. If the value returned is exactly `1`, this means it's a fresh rate-limit window, so we apply the TTL using `redisTemplate.expire(...)`.
