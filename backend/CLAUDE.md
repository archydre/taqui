# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This file documents the **backend** module only (Spring Boot). The frontend lives in `../frontend` and has its own CLAUDE.md.

## Stack

- Java 21, Spring Boot 3.5.15, Maven (via the included `mvnw` wrapper — Maven is not installed globally).
- Spring Web (MVC), Spring Data JPA, PostgreSQL driver, Spring Security, Bean Validation.
- Lombok + MapStruct 1.6.3 for boilerplate reduction and DTO/entity mapping.
- Springdoc OpenAPI 2.8.17 (Swagger UI at `/swagger-ui.html`).
- Base package: `com.taqui.backend`.

## Commands

Run all commands from the `backend/` directory. On Windows use `mvnw.cmd`; on Unix use `./mvnw`.

```powershell
.\mvnw.cmd compile                 # compile (also runs MapStruct annotation processor)
.\mvnw.cmd spring-boot:run         # run the app (auto-starts Postgres, see below)
.\mvnw.cmd test                    # run all tests
.\mvnw.cmd test -Dtest=ClassName#method   # run a single test
.\mvnw.cmd clean package           # build the executable jar into target/
```

## Database

PostgreSQL is provided by `docker-compose.yml` (db/user/password all `taqui`, port 5432, persisted in the `taqui-pgdata` volume).

Because the `spring-boot-docker-compose` dependency is present, `spring-boot:run` **automatically starts the compose stack** and stops it when the app exits — no manual `docker compose up` needed. This requires Docker Desktop to be running. Connection details are still set explicitly in `application.properties`.

**Flyway owns the schema.** Migrations live in `src/main/resources/db/migration` (`V1__init.sql` is the baseline, extracted from the schema Hibernate generates for the current entities). Flyway runs them on startup; `spring.jpa.hibernate.ddl-auto=validate` makes Hibernate only *check* that the entities match the Flyway-created schema — it never creates or alters a table. Every schema change is a new versioned migration (`V2__...sql`, `V3__...sql`); do **not** hand-edit tables (e.g. in a DB GUI) or the next `validate` will flag the drift. Tests apply the same migrations against the Testcontainers Postgres (the test profile also uses `ddl-auto=validate`), so a broken migration turns the suite red.

## MapStruct conventions

Mappers are `@Mapper(componentModel = "spring")` interfaces so the generated implementation is a Spring bean and can be injected into services. Implementations are generated at compile time into `target/generated-sources/annotations/` — they do not exist until you run a compile, and that directory is empty when no mapper interfaces exist yet.

Lombok and MapStruct are both annotation processors. Their order is fixed in `maven-compiler-plugin` (`lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`); do not reorder, or mappers may fail to read Lombok-generated getters/setters.

## Notes

- `spring-boot-devtools` is included, so the app hot-restarts on recompile during development.
- Spring Security is on the classpath, so **all endpoints are secured by default** (HTTP Basic, generated password in the startup log) until a `SecurityConfig` is added. This also locks down Swagger UI.
