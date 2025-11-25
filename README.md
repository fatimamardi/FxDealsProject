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
- **PostgreSQL 15** - The database (or H2 for local testing)
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

If you prefer to run it on your machine:

1. **Install Java 17** (or higher) if you don't have it
2. **Install Maven** - Download from Apache Maven website
3. **Set up a database** - Either PostgreSQL or use H2 (in-memory, no setup needed)
4. **Build the project** - Run Maven build command
5. **Start the application** - Run the Spring Boot app

For local development, we've configured H2 database by default, so you don't need PostgreSQL unless you want it.

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

**Port 8080 already in use?** Change the port in `application.yml` or stop whatever is using that port.

**Database connection errors?** Make sure PostgreSQL is running (if using it), or switch to H2 for local testing.

**Application won't start?** Check the logs - usually it's a database connection issue or missing dependency.

**Tests failing?** Make sure H2 is available for tests (it should be included automatically).

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

Check the other documentation files:
- `API_DOCUMENTATION.md` - Detailed API reference
- `POSTMAN_TESTING_GUIDE.md` - How to test with Postman
- `TEST_DOCKER.md` - Docker testing guide
- `LOCAL_SETUP.md` - Local development setup

## About This Project

This was developed as part of a Scrum team project for Bloomberg's FX deals analysis system. It follows best practices for enterprise Java development with proper error handling, logging, and testing.

---

For questions or issues, check the logs first - they usually tell you what's happening!
