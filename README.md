# Boot Reactive Security

Reactive Spring Boot API with JWT authentication, Spring Security (WebFlux), and Reactive MongoDB.

## Table of Contents
1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Features](#features)
5. [Prerequisites](#prerequisites)
6. [Configuration](#configuration)
7. [Run the Application](#run-the-application)
8. [API Documentation](#api-documentation)
9. [Authentication Flow](#authentication-flow)
10. [API Endpoints](#api-endpoints)
11. [Example cURL Commands](#example-curl-commands)
12. [Testing](#testing)
13. [Known Notes](#known-notes)

## Overview
This project provides:
- JWT-based login and token validation.
- User registration.
- Protected admin CRUD endpoints for users.
- Reactive stack using Spring WebFlux + Reactive MongoDB.
- OpenAPI/Swagger UI for API exploration.

## Tech Stack
- Java 25
- Spring Boot 4.0.2
- Spring WebFlux
- Spring Security
- Spring Data Reactive MongoDB
- JJWT 0.13.0
- Springdoc OpenAPI (WebFlux UI)
- Maven
- JUnit 5, Mockito, WebTestClient

## Project Structure
```text
src/main/java/com/github/suvratking/bootReactiveSecurity
├── admin
│   ├── controller/AdminController.java
│   ├── dto/UserRequest.java
│   ├── dto/UserResponse.java
│   └── service/UserService.java
├── auth
│   ├── config/SecurityConfiguration.java
│   ├── config/JwtTokenProvider.java
│   ├── config/JwtTokenAuthenticationFilter.java
│   ├── controller/AuthController.java
│   ├── dto/AuthenticationRequest.java
│   ├── entity/User.java
│   ├── repository/UserRepository.java
│   └── service/AuthService.java
├── config/OpenApiConfig.java
├── exception/GlobalExceptionHandler.java
└── BootReactiveSecurityApplication.java
```

## Features
- `POST /auth/register`: register user
- `POST /auth/login`: authenticate user, returns JWT
- `GET /admin/user`: list all users (protected)
- `POST /admin/user`: create user (protected)
- `GET /admin/user/{id}`: get user by id (protected)
- `PUT /admin/user/{id}`: update user (protected)
- `DELETE /admin/user/{id}`: delete user (protected)

## Prerequisites
- Java 25 installed and available in `PATH`
- Maven 3.9+ (or use `./mvnw`)
- MongoDB running locally on `localhost:27017`

## Configuration
Application config is in `src/main/resources/application.properties`.

Current defaults:
```properties
spring.application.name=boot-reactive-security
spring.mongodb.uri=mongodb://localhost:27017/spring-reactive-security
spring.data.mongodb.gridfs.database=files
spring.data.mongodb.gridfs.bucket=images

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
```

JWT configuration in current code (`JwtTokenProvider`) is hardcoded to:
- Expiry: 1 hour

## Run the Application
1. Start MongoDB locally.
2. Run the app:

```bash
./mvnw spring-boot:run
```

Build JAR:
```bash
./mvnw clean package
java -jar target/boot-reactive-security-0.0.1-SNAPSHOT.jar
```

## API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Authentication Flow
1. Register a user using `/auth/register`.
2. Login via `/auth/login` to receive `access_token`.
3. Call protected endpoints with header:
   `Authorization: Bearer <access_token>`.

## API Endpoints

### Public
- `POST /auth/register`
- `POST /auth/login`
- `GET /swagger-ui.html`
- `GET /v3/api-docs`

### Protected
- `GET /admin/user`
- `POST /admin/user`
- `GET /admin/user/{id}`
- `PUT /admin/user/{id}`
- `DELETE /admin/user/{id}`

## Example cURL Commands

Register:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "password": "Password@123",
    "active": true,
    "roles": ["ROLE_ADMIN"]
  }'
```

Login:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Password@123"
  }'
```

List users (protected):
```bash
curl -X GET http://localhost:8080/admin/user \
  -H "Authorization: Bearer <token>"
```

Create user (protected):
```bash
curl -X POST http://localhost:8080/admin/user \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 2,
    "username": "user2",
    "email": "user2@example.com",
    "password": "Password@123",
    "active": true,
    "roles": ["ROLE_USER"]
  }'
```

## Testing
Run all tests:
```bash
./mvnw test
```

Generate coverage report:
```bash
./mvnw test jacoco:report
```

Run a single test class:
```bash
./mvnw test -Dtest=AuthControllerTest
```

Additional test docs:
- `TEST_QUICK_REFERENCE.md`
- `TEST_GENERATION_SUMMARY.md`

## Known Notes
- `docker-compose.yml` currently defines Kafka/Zookeeper/Kafka UI, not MongoDB.
- There are two CORS configurations:
  - `CorsConfig` allows `http://localhost:3000`
  - `SecurityConfiguration` allows all origins via `allowedOriginPatterns("*")`
- User IDs are `Long` values and are managed in service logic (max ID + 1 for register flow).
