.PHONY: help build test run docker-up docker-down docker-restart clean logs

# Default target
help:
	@echo "Bloomberg FX Deals Data Warehouse - Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  make build          - Build the Maven project"
	@echo "  make test           - Run unit tests with coverage"
	@echo "  make run            - Run the application locally (requires PostgreSQL)"
	@echo "  make docker-up      - Start PostgreSQL and application using Docker Compose"
	@echo "  make docker-down    - Stop all Docker containers"
	@echo "  make docker-restart - Restart all Docker containers"
	@echo "  make clean          - Clean Maven build artifacts"
	@echo "  make logs           - Show application logs"
	@echo "  make coverage       - Generate test coverage report"
	@echo "  make sample-import  - Import sample deals from sample-deals.json"

# Build the project
build:
	@echo "Building Maven project..."
	mvn clean package -DskipTests

# Run tests
test:
	@echo "Running unit tests..."
	mvn clean test

# Generate test coverage report
coverage:
	@echo "Generating test coverage report..."
	mvn clean test jacoco:report
	@echo "Coverage report generated at: target/site/jacoco/index.html"

# Run the application locally
run:
	@echo "Starting application..."
	@echo "Make sure PostgreSQL is running on localhost:5432"
	mvn spring-boot:run

# Start Docker containers
docker-up:
	@echo "Starting Docker containers..."
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 10
	@echo "Application should be available at http://localhost:8080"
	@echo "PostgreSQL is available at localhost:5432"

# Stop Docker containers
docker-down:
	@echo "Stopping Docker containers..."
	docker-compose down

# Restart Docker containers
docker-restart: docker-down docker-up

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	rm -rf target/
	rm -rf logs/

# Show application logs
logs:
	@echo "Showing application logs..."
	docker-compose logs -f app

# Import sample deals
sample-import:
	@echo "Importing sample deals..."
	@curl -X POST http://localhost:8080/api/v1/deals/bulk \
		-H "Content-Type: application/json" \
		-d @sample-deals.json || echo "Make sure the application is running!"

# Full build and test
all: clean test build
	@echo "Build and test completed successfully!"

