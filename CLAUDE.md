# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.5 REST API (Java 21) for a product demo application. Uses Maven with the Maven Wrapper. Currently a skeleton project ready for feature development.

## Build & Run Commands

```bash
./mvnw clean compile              # Compile
./mvnw clean test                 # Run all tests
./mvnw test -Dtest=ClassName      # Run a single test class
./mvnw test -Dtest=ClassName#method  # Run a single test method
./mvnw clean package              # Build executable JAR
./mvnw spring-boot:run            # Run the application
```

## Tech Stack & Key Libraries

- **Spring Boot 3.5** with starters: web, data-jpa, validation
- **PostgreSQL** with **Flyway** migrations (SQL files go in `src/main/resources/db/migration/`)
- **MapStruct 1.6** for DTO mapping — interfaces annotated with `@Mapper` generate implementations at compile time
- **Lombok** for boilerplate reduction — annotation processor configured in maven-compiler-plugin

## Architecture

Standard layered Spring Boot layout under `com.repochka.product_demo`:

- `controller/` — REST controllers
- `service/` — Business logic
- `entity/` — JPA entities (mapped to PostgreSQL tables)
- `dto/` — Data transfer objects
- `mapper/` — MapStruct mapper interfaces

Database schema changes are managed via Flyway migrations in `src/main/resources/db/migration/` following the naming convention `V{version}__{description}.sql`.

## Configuration

Application config is in `src/main/resources/application.yaml`. PostgreSQL connection and other environment-specific settings are configured there.
