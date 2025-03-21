# FRC Project Management System

A comprehensive project management tool designed specifically for FIRST Robotics Competition (FRC) teams to manage their robot build season from kickoff to competition.

## Overview

This application helps FRC teams with:
- Project timeline management with FRC-specific milestones
- Task dependency tracking and critical path analysis
- Subteam organization and member management
- Meeting scheduling and attendance tracking
- Gantt chart visualization
- Component/part tracking

## Development Setup

### Prerequisites

- JDK 17 or higher
- Maven or Gradle
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)
- Scene Builder (for FXML editing) - optional
- Git

### Setup Instructions

1. Clone the repository:
   ```
   git clone https://github.com/your-organization/frc-project-manager.git
   cd frc-project-manager
   ```

2. Build the project:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn javafx:run
   ```

## Project Structure

The project follows a standard Maven/JavaFX structure:

```
frc-project-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── frcpm/
│   │   │           ├── MainApp.java
│   │   │           ├── config/
│   │   │           ├── controllers/
│   │   │           ├── models/
│   │   │           ├── repositories/
│   │   │           ├── services/
│   │   │           └── utils/
│   │   └── resources/
│   │       ├── css/
│   │       ├── fxml/
│   │       └── images/
│   └── test/
│       └── java/
└── pom.xml
```

## Database

The application uses an embedded H2 database with Hibernate ORM for persistence. The database file is stored in the `db` directory in the application folder.

## Architecture

The application follows the Model-View-Controller (MVC) pattern:

- **Models**: Java POJOs representing data entities with JPA annotations
- **Views**: FXML files for UI layouts
- **Controllers**: JavaFX controllers for UI logic
- **Repositories**: Data access layer using JPA
- **Services**: Business logic layer

## Migration Information

This project is a migration from a Django web application to a JavaFX desktop application with H2 Database. The migration is being performed in phases:

### Phase 1: Project Setup & Architecture Design
- Setting up the Java development environment
- Defining application architecture
- Creating project structure
- Implementing basic application shell
- Designing data models in Java

### Future Phases
- Implementing the database schema
- Creating repository interfaces and implementations
- Building data access methods
- Developing the user interface
- Implementing business logic

## License

[MIT License](LICENSE)

## Acknowledgments

- Original Django codebase developers
- FIRST Robotics Competition community