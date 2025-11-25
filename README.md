# Bloomberg FX Deals Data Warehouse

This is a data warehouse application built for Bloomberg to handle FX (Foreign Exchange) deal imports. The system accepts deal information, validates it thoroughly, and stores it in a database while preventing duplicate imports.

## What This Application Does

When you send FX deal data to this application, it will:

- **Validate everything** - Checks that all required fields are present, formats are correct, and data makes sense
- **Prevent duplicates** - Won't let you import the same deal twice (based on the unique deal ID)
- **Save what it can** - Even if some deals in a batch fail, the valid ones still get saved (no rollback)
- **Log everything** - Keeps detailed logs so you can track what happened

## The Deal Data We Accept

Each deal needs these five pieces of information:

1. **Deal Unique Id** - A unique identifier for this deal (up to 100 characters)
2. **From Currency ISO Code** - The source currency using 3-letter codes like USD, EUR, GBP
3. **To Currency ISO Code** - The target currency (also 3-letter ISO code)
4. **Deal Timestamp** - When the deal happened (can't be in the future)
5. **Deal Amount** - The amount in the ordering currency (supports up to 4 decimal places)

## What Gets Validated

The system checks several things before accepting a deal:

- All required fields are present
- Currency codes are exactly 3 uppercase letters
- From and To currencies are different
- Timestamp is not in the future
- Amount is greater than zero and has proper decimal precision
- Deal ID doesn't already exist in the database

## Technology We Used

- **Java 17** - The programming language
- **Spring Boot 3.2.0** - Makes building web APIs easier
- **Maven** - Handles dependencies and building
- **PostgreSQL 15** - The database
- **Docker** - For easy deployment
- **JUnit & Mockito** - For testing

## Getting Started

### Option 1: Using Docker (Easiest Way)

If you have Docker installed, this is the simplest approach:

1. Make sure Docker Desktop is running
2. Open a terminal in the project folder
3. Run `docker-compose up -d` to start everything
4. Wait a minute for the application to start
5. Open your browser to `http://localhost:8080/api/v1/deals` to test

The Docker setup includes both the database and the application, so you don't need to install anything else.

### Option 2: Running Locally

If you want to run it directly on your computer instead of using Docker:

1. **Install Java 17 or newer** - You can get it from adoptium.net
2. **Install Maven** - Download from the Apache Maven website and add it to your PATH
3. **Install PostgreSQL** - Get it from postgresql.org, then create the database and user (see LOCAL_SETUP.md for details)
4. **Build the project** - Run `mvn clean package` in the project folder
5. **Start it up** - Run `mvn spring-boot:run` or use your IDE

The application needs PostgreSQL to be running - it won't work without it. Check out `LOCAL_SETUP.md` for step-by-step instructions if you need help.

## How to Use the API

### Import a Single Deal

Send a POST request to `/api/v1/deals` with a JSON body containing the deal information. You'll get back the saved deal with an ID.

### Import Multiple Deals

Send a POST request to `/api/v1/deals/bulk` with a JSON array of deals. The response tells you how many succeeded, how many were duplicates, and which ones failed (if any).

### Get All Deals

Send a GET request to `/api/v1/deals` to retrieve all deals that have been imported.

### Get a Specific Deal

Send a GET request to `/api/v1/deals/{dealUniqueId}` to get one deal by its unique ID.

## Testing

We've included unit tests that cover the validation logic, service layer, and controllers. You can run them with Maven's test command. The project maintains good test coverage (over 80%) to ensure reliability.

## Project Structure

The code is organized in a standard Spring Boot structure:

- **Controller** - Handles HTTP requests
- **Service** - Contains business logic (validation, duplicate checking)
- **Repository** - Talks to the database
- **Model** - Represents the deal data
- **DTO** - Data transfer objects for requests and responses
- **Exception Handler** - Catches and formats errors nicely

## Configuration

Most settings are in `application.yml`. You can change:
- Database connection details
- Server port
- Logging levels
- JPA settings

For Docker deployments, environment variables in `docker-compose.yml` override these settings.

## Common Issues and Solutions

**Port 8080 already in use?** 
- Something else is using that port. You can change the port in `application.yml` (just change `server.port: 8080` to something else like 8081), or find what's using port 8080 and stop it.

**Database connection errors?** 
- First, make sure PostgreSQL is actually running (check Windows Services)
- Verify the database `fxdealsdb` exists and the username/password in `application.yml` are correct
- Sometimes it's a firewall issue blocking port 5432

**Application won't start?** 
- Check the console logs - they usually tell you exactly what's wrong
- Most of the time it's a database connection problem or a missing dependency

**Tests failing?** 
- Tests automatically use an in-memory database (H2), so they should work without PostgreSQL
- If tests fail, make sure Maven downloaded all dependencies - try running `mvn clean install` again

## Sample Data

There's a `sample-deals.json` file in the project root with example deals you can use for testing. Just import it using the bulk endpoint.

## Deployment

For production, you can:
- Build a JAR file and run it with Java
- Use Docker Compose (as provided)
- Deploy to a cloud platform

The Docker setup is production-ready and includes health checks for the database.

## Logging

The application logs to both console and a log file (`logs/fx-deals-warehouse.log`). Logs include:
- When deals are imported
- Validation errors
- Duplicate detection
- Any unexpected errors

## What Makes This Special

- **No rollback policy** - Each deal is saved independently, so valid deals aren't lost if others fail
- **Comprehensive validation** - Catches problems before they reach the database
- **Duplicate prevention** - Both in the same batch and against existing records
- **Detailed error messages** - Tells you exactly what went wrong

## Need Help?

We've got documentation for different scenarios:
- `API_DOCUMENTATION.md` - Complete API reference with all the endpoints
- `POSTMAN_TESTING_GUIDE.md` - Step-by-step guide for testing with Postman
- `LOCAL_SETUP.md` - Detailed instructions for setting up on your local machine
- `TEST_DOCKER.md` - How to test using Docker Compose

## About This Project

This was built as part of a Scrum team project for Bloomberg's FX deals analysis system. We focused on making it robust, well-tested, and easy to use. The code follows enterprise Java best practices with proper error handling, comprehensive logging, and good test coverage.

---

**Tip:** If something's not working, check the logs first - they're usually pretty helpful in figuring out what went wrong!
