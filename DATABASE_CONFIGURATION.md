# Database Configuration Guide

## Default Behavior

**By default, the application uses PostgreSQL.** 

If PostgreSQL is not available, the application will fail to start with a connection error.

## Using H2 for Local Development

If you don't have PostgreSQL installed and want to run the application locally, you have two options:

### Option 1: Use the Local Profile (Recommended)

The application includes an `application-local.yml` configuration that uses H2 in-memory database.

**To use it:**

1. **When running from IntelliJ:**
   - Edit Run Configuration
   - Add VM options: `-Dspring.profiles.active=local`
   - Or add to Program arguments: `--spring.profiles.active=local`

2. **When running with Maven:**
   ```
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. **When running the JAR:**
   ```
   java -jar target/fx-deals-warehouse-1.0.0.jar --spring.profiles.active=local
   ```

### Option 2: Modify application.yml Temporarily

You can temporarily change `application.yml` to use H2, but remember to change it back for production.

## Configuration Files

- **`application.yml`** - Default configuration (PostgreSQL)
- **`application-local.yml`** - Local development (H2 in-memory)
- **`application-postgres.yml`** - Alternative PostgreSQL config (same as default)

## Docker Deployment

When using Docker Compose, PostgreSQL is automatically started and configured. The application connects to it using environment variables defined in `docker-compose.yml`.

## Summary

| Scenario | Database Used | Configuration |
|----------|--------------|---------------|
| Default (no profile) | PostgreSQL | `application.yml` |
| Local development | H2 | `application-local.yml` (activate with profile) |
| Docker Compose | PostgreSQL | Environment variables in `docker-compose.yml` |
| Tests | H2 | `application.yml` in `src/test/resources` |

## Important Notes

- **H2 is in-memory** - Data is lost when the application stops
- **PostgreSQL persists data** - Data survives application restarts
- **For production, always use PostgreSQL**
- **H2 is only for local development and testing**

