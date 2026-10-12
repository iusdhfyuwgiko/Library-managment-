# ISME Knowledge Hub

ISME Knowledge Hub is a Java library management application with a Spring Boot web dashboard and a Swing desktop client backed by MySQL.

## Features

- Add books and members
- Issue books to members
- Return books with fine calculation
- Search books by title
- View book inventory in a table
- Desktop GUI with database-backed storage

## Database setup

The application uses MySQL by default and connects to `localhost:3306/library_db`. The URL creates the database if the MySQL user has permission. H2 is used only by the test profile.

Set your MySQL password in the environment before running the app. In PowerShell:

```powershell

$env:LIBRARY_DB_USER = 'root'
$env:LIBRARY_DB_PASSWORD = 'your_password'

mvn spring-boot:run
```

Optional overrides are `LIBRARY_DB_URL` and `LIBRARY_DB_DRIVER`. Keep database credentials out of source control.

To run the Swing client:

```powershell
mvn exec:java
```

## Test run

```bash
mvn test
```

## Project structure

- `src/main/java/com/library/gui/LibrarySwingApp.java` - Swing desktop interface
- `src/main/java/com/library/model` - domain objects
- `src/main/java/com/library/service` - business logic
- `src/test/java/com/library/service/LibraryServiceTest.java` - automated tests

## Notes
This project uses MySQL in normal runtime and an isolated H2 database for test execution.
