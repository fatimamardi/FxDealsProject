# Setting Up the Application Locally

Hey there! If you want to run this application on your own computer, here's how to get everything set up. It's pretty straightforward once you have the right tools installed.

## What You'll Need

Before we start, make sure you have these three things:

1. **Java 17 or newer** - The application is written in Java, so you need this to run it
2. **Maven** - This helps build and manage the project
3. **PostgreSQL** - The database where all the deal data gets stored

Let me walk you through installing each one if you don't have them yet.

## Installing Java

If you're not sure if you have Java, open a terminal and type `java -version`. If you see a version number (17 or higher), you're good to go!

If not, here's what to do:

1. Head over to https://adoptium.net/ and download Java 17 or any newer version
2. Run the installer and follow the prompts - it's pretty standard
3. Once it's done, close and reopen your terminal, then check again with `java -version`

That's it for Java!

## Installing Maven

Maven is what we use to build the project and manage all the libraries it needs. There are a few ways to get it:

### The Manual Way

1. Go to https://maven.apache.org/download.cgi
2. Download the zip file (look for something like `apache-maven-3.9.x-bin.zip`)
3. Unzip it somewhere like `C:\Program Files\Apache\maven`
4. Now we need to add it to your PATH so your computer can find it:
   - Right-click "This PC" → Properties → Advanced system settings
   - Click "Environment Variables"
   - Find "Path" in the system variables and click Edit
   - Click New and add the path to the `bin` folder (like `C:\Program Files\Apache\maven\bin`)
   - Click OK on everything
5. Close your terminal completely and open a new one
6. Type `mvn -version` to make sure it worked

### The Easy Way (Windows)

If you have Chocolatey installed (it's a package manager for Windows), you can just run:
```
choco install maven
```

Much simpler if you have it!

### Using Your IDE

If you're using IntelliJ IDEA or Eclipse, they usually come with Maven built-in. You can skip installing Maven separately and just run the application from your IDE. IntelliJ is especially good at this - just right-click the main class and hit Run.

## Setting Up PostgreSQL

This is the database where all your deal data will live. Here's how to get it ready:

### Installing PostgreSQL

1. Go to https://www.postgresql.org/download/windows/
2. Download the installer and run it
3. During installation, it will ask you to set a password for the `postgres` user - remember this password!
4. Let it install with the default settings - that's usually fine

### Creating the Database

Once PostgreSQL is installed, we need to create the database that our application will use:

1. Open **pgAdmin** (it should have been installed with PostgreSQL) or use the command line
2. Connect to your PostgreSQL server (it will ask for that password you set during installation)
3. Open the Query Tool or a SQL editor
4. Run these three commands:

```sql
CREATE DATABASE fxdealsdb;
CREATE USER fxdeals_user WITH PASSWORD 'fxdeals_password';
GRANT ALL PRIVILEGES ON DATABASE fxdealsdb TO fxdeals_user;
```

That creates the database, creates a user for our application, and gives that user permission to use the database.

### Making Sure It's Running

PostgreSQL should start automatically, but if you're having connection issues later, check:

- Open Windows Services (search for "services" in the Start menu)
- Look for "postgresql" service
- Make sure it says "Running"

## Building the Project

Now that you have everything installed, let's build the project:

1. Open a terminal in the project folder (the `bloomberg-fx-deals-warehouse` folder)
2. Type this command:
   ```
   mvn clean package
   ```

This will take a few minutes the first time because it downloads all the libraries the project needs. You'll see a lot of text scrolling by - that's normal! When it's done, you should see "BUILD SUCCESS" at the end.

## Running the Application

You've got a few options here:

### From IntelliJ (Easiest)

1. Open the project in IntelliJ IDEA
2. Find `FxDealsApplication.java` in the project
3. Right-click it and choose "Run 'FxDealsApplication'"
4. Watch the console - you should see it start up and eventually say "Started FxDealsApplication"

### From the Terminal with Maven

Just run:
```
mvn spring-boot:run
```

The application will start and you'll see logs in the terminal.

### Running the JAR File

If you built the project earlier, you can run the JAR directly:
```
java -jar target/fx-deals-warehouse-1.0.0.jar
```

## Checking If It Worked

Once the application starts, you should see a message like "Started FxDealsApplication in 5.234 seconds" in the console.

To test if it's actually working:

1. Open your web browser
2. Go to: `http://localhost:8080/api/v1/deals`
3. You should see: `[]` (an empty array, which means no deals imported yet)

If you see that, congratulations! The application is running.

## Testing the API

Now you can start testing! You can use Postman (which is great for this) or even just curl commands. Check out the `POSTMAN_TESTING_GUIDE.md` file for detailed examples of how to import deals and test all the features.

## When Things Go Wrong

### Can't Connect to Database?

- Make sure PostgreSQL is actually running (check Windows Services)
- Verify the database `fxdealsdb` exists (you can check in pgAdmin)
- Double-check the username and password in `application.yml` match what you created
- Make sure nothing is blocking port 5432 (your firewall might be)

### Port 8080 is Already Taken?

Something else is using port 8080. You can either:
- Find what's using it and stop it
- Change the port in `application.yml` - just change `server.port: 8080` to `server.port: 8081` (or any other free port)

### Maven Command Not Found?

- Make sure Maven is installed and in your PATH
- Close and reopen your terminal after adding Maven to PATH
- Try typing `mvn -version` to see if it's working

### Build Keeps Failing?

- Check your Java version with `java -version` - needs to be 17 or higher
- Make sure you have internet connection (Maven needs to download dependencies)
- Try deleting the `.m2` folder in your user directory and rebuilding (this clears Maven's cache)

## Configuration

Most of the settings are in `src/main/resources/application.yml`. You can change things like:
- Database connection details (if your PostgreSQL is set up differently)
- What port the application runs on
- How much logging you want to see

For Docker, the settings in `docker-compose.yml` take priority, so you don't need to worry about that file if you're using Docker.

## What's Next?

Once everything is running smoothly:
1. Try importing some deals using the API
2. Test the validation by sending invalid data
3. Try importing the same deal twice to see duplicate detection in action
4. Check out the logs to see what's happening behind the scenes

The `sample-deals.json` file has some example data you can use for testing. Just import it using the bulk endpoint and you'll have some data to work with.

Good luck, and if you run into issues, the logs usually tell you what's wrong!
