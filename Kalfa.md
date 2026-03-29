# Kafka Interview Cheat Sheet

---

## 1. Basics — What is Kafka?

**Q: What is Apache Kafka and why is it used?**
Kafka is a distributed event streaming platform used to build real-time data pipelines and streaming applications. It acts as a high-throughput, fault-tolerant message broker between services — producers publish events, consumers read them asynchronously. Common use cases: async notifications, audit logs, microservice communication, real-time analytics.

**Q: What is a Kafka Topic?**
A topic is a named channel to which messages are published. Think of it like a folder or a queue. Producers write to a topic, consumers read from it. Topics are split into partitions for parallel processing. Example: `otp-email-events`.

**Q: What is a Partition and why does it matter?**
Each topic is split into partitions — ordered, immutable sequences of records. Partitions allow parallel reads: multiple consumers can read from different partitions simultaneously. Order is only guaranteed within a single partition. More partitions = more parallelism = higher throughput.

**Q: What is an Offset?**
An offset is a sequential ID for each message within a partition. Kafka tracks the consumer's last-read offset. If a consumer crashes, it resumes from the committed offset — ensuring no message is lost or double-processed unintentionally.

**Q: What is a Broker? What is ZooKeeper?**
A broker is a Kafka server node that stores messages and serves producers/consumers. A cluster has multiple brokers for fault tolerance. ZooKeeper (legacy) was the coordinator that tracked live brokers, managed leader election, and stored cluster metadata. Newer Kafka versions (KRaft mode) replace ZooKeeper with a built-in consensus protocol.

---

## 2. Producer / Consumer — Message Flow

**Q: What is a Producer?**
A producer is any application that publishes messages to a Kafka topic. In Spring, this is done via `KafkaTemplate.send(topic, key, value)`. The key determines which partition the message lands in — same key = same partition = ordered delivery for that entity.

**Q: What is a Consumer and Consumer Group?**
A consumer reads messages from a topic. A consumer group is a set of consumers sharing a `group-id`. Kafka guarantees each partition is assigned to only one consumer per group — so messages are not duplicated. If you add more consumers to a group, Kafka rebalances partitions among them automatically.

**Q: How does Kafka guarantee message order?**
Kafka guarantees order only within a single partition. Across partitions, there is no global ordering. To guarantee ordered processing for a user, always use the same partition key (e.g. user email or user ID), so all their messages land in the same partition.

**Q: What is the difference between at-least-once and exactly-once delivery?**
At-least-once (default): messages are never lost, but may be re-delivered if the consumer crashes before committing its offset — so your consumer logic must be idempotent. Exactly-once: Kafka supports this with idempotent producers + transactional APIs, but it adds complexity. For most use cases (like OTP emails), at-least-once + idempotent checks is sufficient.

---

## 3. Architecture & Patterns

**Q: Why Kafka over RabbitMQ or direct REST calls?**
Kafka retains messages on disk (replayable), handles millions of events/sec, and decouples services — a downstream failure doesn't break the upstream. RabbitMQ deletes messages after consumption (not replayable). Direct REST calls are synchronous and tightly coupled — a slow email server slows your API. Kafka enables async, resilient, independently scalable services.

**Q: What is a Dead Letter Topic (DLT)?**
A DLT (e.g. `otp-email-events.DLT`) is a fallback topic where messages that failed processing after retries are sent. This prevents bad messages from blocking the main consumer loop. You can then inspect and replay DLT messages manually or via a separate service — instead of silently losing them.

**Q: How do you prevent duplicate emails if Kafka re-delivers a message?**
Make the consumer idempotent — before sending the email, check if an OTP for that transaction ID has already been sent (store a sent flag in Redis or DB). If yes, skip. This makes re-delivery harmless. This is called idempotency and is a critical design principle for event-driven systems.

**Q: What is a Replication Factor?**
Replication factor controls how many broker copies each partition has. A factor of 3 means 1 leader + 2 follower replicas. If the leader broker dies, a follower is promoted. For local dev, factor = 1 is fine. In production, always use 3+ for fault tolerance.

---

## 4. Spring Kafka — Implementation Specifics

**Q: How do you create a topic programmatically in Spring?**
Declare a `NewTopic` bean using `TopicBuilder.name().partitions().replicas().build()`. Spring Kafka auto-creates the topic on startup if it doesn't exist. You can also configure topics via the broker admin UI or CLI.

**Q: What does `@KafkaListener` do and what is `concurrency`?**
`@KafkaListener` marks a method as a consumer for a topic. `concurrency = "3"` spawns 3 listener threads — one per partition — enabling parallel message processing. Without concurrency, a single thread processes all partitions sequentially, limiting throughput.

**Q: What is `auto-offset-reset=earliest` vs `latest`?**
This controls where a new consumer group starts reading. `earliest` = read all existing messages from the beginning of the topic. `latest` = only read messages published after the consumer started. For OTP emails, use `earliest` so no event is missed if the service restarts.

---

## 5. Resume Bullet Points

- Integrated Apache Kafka to decouple OTP email delivery from the auth flow, replacing synchronous email calls with an async producer–consumer pipeline, improving API response time.
- Designed a partition-keyed message routing strategy (by user email) to guarantee per-user ordering across 3 partitions with concurrent consumers.
- Implemented Dead Letter Topic handling to capture failed email delivery attempts for retry and observability, preventing silent data loss.
- Configured idempotent consumer logic to safely handle Kafka's at-least-once delivery without sending duplicate OTP emails.