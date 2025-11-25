# Step-by-Step Local Setup Guide

## Step 1: Install Maven

Since Maven is not installed, we need to install it first.

### Option A: Download and Install Maven Manually

1. **Download Maven:**
   - Go to: https://maven.apache.org/download.cgi
   - Download `apache-maven-3.9.x-bin.zip` (latest version)

2. **Extract and Setup:**
   - Extract to `C:\Program Files\Apache\maven` (or your preferred location)
   - Add to PATH:
     - Open System Properties → Environment Variables
     - Add `C:\Program Files\Apache\maven\bin` to PATH
     - Or use: `C:\Program Files\Apache\maven\bin` in your terminal

3. **Verify Installation:**
   ```bash
   mvn -version
   ```

### Option B: Use Chocolatey (if installed)

```bash
choco install maven
```

### Option C: Use IDE (IntelliJ IDEA / Eclipse)

If you have an IDE, you can:
- **IntelliJ IDEA:** Right-click `pom.xml` → Maven → Reload Project, then Run `FxDealsApplication`
- **Eclipse:** Right-click project → Run As → Spring Boot App

---

## Step 2: Configure for Local Development

We have two options:

### Option A: Use H2 In-Memory Database (Easiest - No Setup Required)

The project is already configured with H2 for local testing. Just run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Or set environment variable:
```bash
$env:SPRING_PROFILES_ACTIVE="local"
mvn spring-boot:run
```

### Option B: Use PostgreSQL

1. **Install PostgreSQL:**
   - Download from: https://www.postgresql.org/download/windows/
   - Install with default settings

2. **Create Database:**
   ```sql
   CREATE DATABASE fxdealsdb;
   CREATE USER fxdeals_user WITH PASSWORD 'fxdeals_password';
   GRANT ALL PRIVILEGES ON DATABASE fxdealsdb TO fxdeals_user;
   ```

3. **Run Application:**
   ```bash
   mvn spring-boot:run
   ```

---

## Step 3: Build the Project

Once Maven is installed:

```bash
cd bloomberg-fx-deals-warehouse
mvn clean package
```

This will:
- Download all dependencies
- Compile the code
- Run tests
- Create a JAR file in `target/` folder

---

## Step 4: Run the Application

### Method 1: Using Maven (Recommended)

```bash
# With H2 database (easiest)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# With PostgreSQL (default)
mvn spring-boot:run
```

### Method 2: Run the JAR directly

```bash
# First build
mvn clean package

# Then run
java -jar target/fx-deals-warehouse-1.0.0.jar --spring.profiles.active=local
```

---

## Step 5: Verify the Application is Running

1. **Check Application Endpoint:**
   ```bash
   curl http://localhost:8080/api/v1/deals
   ```
   
   Or open in browser: http://localhost:8080/api/v1/deals

2. **Expected Response:**
   ```json
   []
   ```
   (Empty array if no deals are imported yet)

3. **H2 Console (if using local profile):**
   - URL: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:fxdealsdb`
   - Username: `sa`
   - Password: (empty)

---

## Step 6: Test the API

### Import a Single Deal

```bash
curl -X POST http://localhost:8080/api/v1/deals ^
  -H "Content-Type: application/json" ^
  -d "{\"dealUniqueId\":\"DEAL-001\",\"fromCurrencyIsoCode\":\"USD\",\"toCurrencyIsoCode\":\"EUR\",\"dealTimestamp\":\"2024-01-15T10:30:00\",\"dealAmount\":1000000.50}"
```

### Import Bulk Deals

```bash
curl -X POST http://localhost:8080/api/v1/deals/bulk ^
  -H "Content-Type: application/json" ^
  -d @sample-deals.json
```

### Get All Deals

```bash
curl http://localhost:8080/api/v1/deals
```

---

## Troubleshooting

### Maven Not Found
- Ensure Maven is installed and in PATH
- Restart terminal after adding to PATH
- Verify with: `mvn -version`

### Port 8080 Already in Use
- Change port in `application.yml`: `server.port: 8081`
- Or stop the service using port 8080

### Database Connection Error
- If using PostgreSQL: Ensure PostgreSQL is running
- If using H2: Make sure you're using `local` profile

### Build Errors
- Check Java version: `java -version` (should be 17+)
- Clean and rebuild: `mvn clean install`

---

## Next Steps

Once the application is running locally:
1. Test all endpoints
2. Import sample deals
3. Check logs in `logs/fx-deals-warehouse.log`
4. Then we can proceed with Docker setup

