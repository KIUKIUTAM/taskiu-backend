# Taskiu Backend — Spring Boot 3, Java 21

High-performance backend service for Taskiu built with Spring Boot 3.5 and Java 21, featuring polyglot persistence, secure authentication, and event-driven integrations.

## Overview
- Stateless API using Spring Security with JWT for auth
- OAuth2 login integration (Google, GitHub) with Authorization Code + PKCE
- Polyglot persistence: PostgreSQL (JPA), MongoDB (refresh tokens), Redis (caching)
- Asynchronous messaging with RabbitMQ
- Object storage with MinIO for avatars and attachments
- Email sending and templating with Spring Mail + Thymeleaf

## Tech Stack
- Core: Spring Boot 3.5.10, Java 21 ([build.gradle](file:///f:/taskiu/taskiu-backend/build.gradle))
- Persistence: Spring Data JPA (PostgreSQL), Spring Data MongoDB, Spring Data Redis
- Messaging: Spring AMQP (RabbitMQ), Fanout exchange with queues
- Auth: Spring Security, jjwt (HS512), OAuth2 client
- Storage: MinIO Java SDK
- Templates: Thymeleaf (email)
- Utility: Lombok, spring-dotenv

## Architecture & Modules
- Modular structure under `modules/` following service + repository separation
  - Auth: OAuth2 flows, JWT issuing, refresh token rotation
  - User: Profile, avatar upload via MinIO
  - Teams: Team entity, members, role-based access enforcement
  - Task: JPA entities for tasks (soft delete via SQL annotations)
  - Email: Verification and notifications
- Cross-cutting concerns via AOP
  - Permission checks implemented in [PermissionAspect.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/common/aspect/PermissionAspect.java)

## Security
- JWT with HS512 signing ([JwtUtils.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/common/utils/JwtUtils.java))
- Stateless filter chain and CORS configuration ([SecurityConfig.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/common/config/SecurityConfig.java))
- Refresh token rotation stored in MongoDB with TTL ([RefreshToken.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/modules/auth/entity/RefreshToken.java), [RefreshTokenService.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/modules/auth/service/RefreshTokenService.java))
- OAuth2 Authorization Code exchange with PKCE ([AuthService.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/modules/auth/service/AuthService.java))

## Messaging
- Fanout exchange and queue binding ([QueueConfiguration.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/common/config/QueueConfiguration.java))
- JSON message converter ([RabbitMQConfiguration.java](file:///f:/taskiu/taskiu-backend/src/main/java/com/tavinki/taskiu/common/config/RabbitMQConfiguration.java))

## Email Templates
- Thymeleaf template for email verification ([email-verification.html](file:///f:/taskiu/taskiu-backend/src/main/resources/templates/email-verification.html))

## Configuration
- Main properties in [application.yaml](file:///f:/taskiu/taskiu-backend/src/main/resources/application.yaml)
- Environment variables wired via docker-compose ([docker-compose.yaml](file:///f:/taskiu/docker-compose.yaml))

### Key Environment Variables
- Database: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- MongoDB: `MONGO_ROOT_USERNAME`, `MONGO_ROOT_PASSWORD`
- Redis: `REDIS_PASSWORD`
- RabbitMQ: `RABBITMQ_USER`, `RABBITMQ_PASSWORD`
- MinIO: `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `MINIO_BUCKET_NAME`
- JWT: `JWT_SECRET`, `JWT_REFRESH_TOKEN_DURATION`
- Mail: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- OAuth2: `GOOGLE_*`, `GITHUB_*`
- App: `APP_EXPECTED_HOST`, `APP_EXPECTED_API_HOST`, `APP_CORS_ALLOWED_ORIGINS`

## Development
### Prerequisites
- Java 21, Gradle, Docker

### Run Locally
```bash
./gradlew bootRun
```

### Build
```bash
./gradlew build
```

### Test
Testcontainers enabled in Gradle for integration tests.
```bash
./gradlew test
```

## Logging
- Logback configuration at [logback-spring.xml](file:///f:/taskiu/taskiu-backend/src/main/resources/logback-spring.xml)
- Container logs mounted via docker-compose

## Project Links
- Root compose: [docker-compose.yaml](file:///f:/taskiu/docker-compose.yaml)
