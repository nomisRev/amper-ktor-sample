# Ktor Amper Sample

This project is a sample Ktor application built with the [Amper](https://github.com/JetBrains/amper) build system. It demonstrates how to create a RESTful API with Ktor, using PostgreSQL for data storage.

## Building and Running

This project uses Amper as its build system. Amper is a new build system from JetBrains that aims to simplify project configuration.

### Building the Project

To build the project, run:

```bash
./amper build
```

### Running the Application

To run the application, use:

```bash
docker-compose up -d
./amper run
```

If the server starts successfully, you'll see output indicating it's running at http://0.0.0.0:8080.

### Running Tests

To run the tests, use:

```bash
./amper test
```

## Project Overview

This is a sample Ktor application that provides a RESTful API for managing users, posts, and comments. It uses:

- [Ktor](https://ktor.io/) for the web framework
- [Exposed](https://github.com/JetBrains/Exposed) for database access
- [PostgreSQL](https://www.postgresql.org/) for data storage
- [HikariCP](https://github.com/brettwooldridge/HikariCP) for connection pooling
- [JWT](https://jwt.io/) for authentication
- [Amper](https://github.com/JetBrains/amper) as the build system

## Project Structure

```
.
├── amper / amper.bat     # Amper build system executables
├── module.yaml           # Amper module configuration
├── docker-compose.yml    # Docker configuration for PostgreSQL
├── resources/            # Application resources
│   ├── application.yaml  # Application configuration
│   └── logback.xml       # Logging configuration
├── src/                  # Source code
│   └── com/example/      # Application code
│       ├── Application.kt        # Main application entry point
│       ├── comments/             # Comments API
│       ├── config/               # Application configuration
│       ├── posts/                # Posts API
│       └── users/                # Users API
└── test/                 # Test code
    └── com/example/      # Test classes
```

## Setup

### Database Setup

The application uses PostgreSQL as its database. You can start it using Docker Compose:

```bash
docker-compose up -d
```

This will start a PostgreSQL instance with the following configuration:
- Database: `ktor_sample`
- Username: `ktor_user`
- Password: `ktor_password`
- Port: `5432`

## Configuration

The application is configured using the `resources/application.yaml` file, which includes:

- Ktor application settings
- JWT authentication configuration
- Database connection settings

## API Endpoints

The application provides RESTful API endpoints for:

- Users: `/users`
- Posts: `/posts`
- Comments: `/comments`

## Current Status

Note that this project may require additional dependencies to be added to the `module.yaml` file. If you encounter compilation errors, check the build logs and add the missing dependencies.

## Resources

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Amper Documentation](https://github.com/JetBrains/amper/tree/main/docs)
- [Exposed Documentation](https://github.com/JetBrains/Exposed/wiki)
