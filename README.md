# Banking Application Backend

Spring Boot backend for a banking platform with authentication, account management, and transaction workflows.

This service is designed for regular users and admins, with secure JWT-based APIs, role checks, and transactional safety.

## Features

- Authentication and authorization
  - User signup and login
  - Admin login
  - JWT-protected routes
- Account management
  - Create account
  - Fetch account details
  - List user accounts
- Transaction management
  - Start and confirm transaction flow
  - Withdraw and transfer (user)
  - Deposit (admin)
  - Transaction history and detail APIs
- Safety and consistency
  - Account ownership validation
  - Insufficient balance checks
  - Transaction expiry window
  - Consistent API response wrapper and global error handling
- Platform integrations
  - Redis for rate limiting and caching use cases
  - Kafka for async email events

## Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL (or compatible relational database)
- Redis
- Kafka
- Lombok

## Project Structure

Main source folders:

- src/main/java/com/ashwinsi/bankingApplication
  - Controller
  - Service
  - Repository
  - Domain
  - DTO
  - Config
  - Kafka
- src/main/resources
- src/test/java

## Prerequisites

- JDK 17+ (recommended)
- Maven 3.8+
- Running database
- Docker (for Redis/Kafka/Zookeeper via compose)

## Environment Configuration

Configure required values using your .env / application.properties setup:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- SALT
- JWT_EXPIRATION_TIME

Also configure any mail, Redis, and Kafka properties your environment requires.

## Docker Compose (Redis + Kafka + Zookeeper)

Use the provided docker-compose.yml to start local infrastructure dependencies.

```yaml
version: "3.8"
services:
	redis:
		image: redis:7-alpine
		container_name: banking_redis
		ports:
			- "6379:6379"
		restart: unless-stopped

	zookeeper:
		image: confluentinc/cp-zookeeper:7.4.0
		environment:
			ZOOKEEPER_CLIENT_PORT: 2181
		ports:
			- "2181:2181"

	kafka:
		image: confluentinc/cp-kafka:7.4.0
		depends_on:
			- zookeeper
		ports:
			- "9092:9092"
		environment:
			KAFKA_BROKER_ID: 1
			KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
			KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
			KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Start services:

```bash
docker compose up -d
```

Stop services:

```bash
docker compose down
```

## Run the Application

Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or build and run:

```bash
./mvnw clean package
java -jar target/*.jar
```

## API Groups

- /api/auth/\*
- /api/account/\*
- /api/transaction/\*
- /api/transaction/admin/\* (admin only)

## Notes

- Development seed user/admin data may be initialized on startup when configured.
- Use seed credentials only for local testing.

## License

Add your preferred license section here.
