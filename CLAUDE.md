# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FRC Project Management System** - A Spring Boot web application for FIRST Robotics Competition teams to manage projects, tasks, and team coordination during build season.

- **Architecture**: Spring Boot 3.2 + Spring Data JPA + Thymeleaf
- **Java Version**: 21
- **Database**: H2 (development) / SQLite (production)
- **Build Tool**: Maven 3.11+
- **Current Phase**: Phase 2E-D (Advanced Task Management) - 85% complete

## Common Commands

### Development Commands
```bash
# Start development server
mvn spring-boot:run

# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package for production
mvn clean package -Pprod

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

### Testing Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TaskServiceTest

# Run tests with coverage
mvn test jacoco:report

# Run integration tests only
mvn test -Dtest=*IntegrationTest
```

### Database Commands
```bash
# Access H2 console (development)
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:./db/frc-project-dev
# Username: sa, Password: (empty)

# Reset database
rm -rf db/frc-project-dev.mv.db
mvn spring-boot:run  # Will recreate tables
```

## Architecture Overview

### Core Spring Boot Structure
- **`src/main/java/org/frcpm/`** - Main application package
  - **`config/`** - Spring configuration classes (Security, Database, WebSocket)
  - **`models/`** - JPA entities (Task, Project, TeamMember, etc.)
  - **`repositories/spring/`** - Spring Data JPA repositories
  - **`services/`** - Business logic layer with interfaces
  - **`services/impl/`** - Service implementations
  - **`web/controllers/`** - REST controllers and web endpoints
  - **`web/dto/`** - Data transfer objects
  - **`web/websocket/`** - WebSocket controllers for real-time features
  - **`security/`** - Authentication and authorization logic
  - **`integration/frc/`** - FRC API integration services

### Key Domain Models
- **`Task`** - Core task entity with Kanban workflow
- **`TaskDependency`** - Advanced task dependencies (NEW in Phase 2E-D)
- **`Project`** - Project management with deadlines
- **`TeamMember`** - Team profiles and role assignments
- **`User`** - Authentication with COPPA compliance

### Current Development Status
The service layer for task dependencies is **100% complete** (~25KB TaskDependencyServiceImpl.java). The remaining work focuses on:
1. **TaskDependencyController** - REST endpoints
2. **TaskDependencyDto** - API response objects
3. **UI templates** - Dependency management interfaces
4. **Testing** - Controller and integration tests

## Key Features

### Implemented Features
- **Task Management**: Complete CRUD with Kanban board
- **Real-time Updates**: WebSocket integration for live collaboration
- **Security**: Role-based access (Student/Mentor/Admin/Parent)
- **COPPA Compliance**: Data protection for students under 13
- **Task Dependencies**: Advanced service layer with critical path analysis
- **FRC API Integration**: Competition data synchronization (Phase 3A)

### Technology Stack
- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf templates, Bootstrap 5, WebSocket (SockJS/STOMP)
- **Database**: H2 (dev), SQLite (prod), HikariCP connection pooling
- **Build**: Maven with Spring Boot plugin
- **Security**: BCrypt password hashing, MFA for mentors

## Development Patterns

### Controller Pattern
Follow `TaskController.java` (~15KB) for REST endpoint patterns:
- Use `@RestController` for API endpoints
- Apply `@PreAuthorize` for role-based security
- Integrate WebSocket notifications for real-time updates
- Handle validation and error responses consistently

### Service Layer Pattern
Follow `TaskDependencyServiceImpl.java` (~25KB) for business logic:
- Interface-based design with Spring dependency injection
- Transactional operations with proper error handling
- Comprehensive validation and business rule enforcement
- Async operations for performance-critical tasks

### Template Pattern
Follow `tasks/kanban.html` (~12KB) for UI development:
- Extend `layout/base.html` for consistent styling
- Use Bootstrap 5 for responsive design
- Integrate WebSocket for real-time updates
- Apply proper security contexts (`sec:authorize`)

## Database Schema

### Core Tables
```sql
-- Main entities
users                 -- Authentication with COPPA compliance
team_members         -- Team profiles and roles
projects             -- Project management with deadlines
tasks                -- Task management with Kanban status
task_dependencies    -- Advanced dependency relationships (NEW)
meetings             -- Meeting coordination
audit_logs           -- Security and compliance tracking
```

### Key Relationships
- Tasks belong to Projects and are assigned to TeamMembers
- TaskDependencies create predecessor/successor relationships
- Users can have multiple TeamMember profiles across projects
- All changes are tracked in audit_logs for compliance

## Configuration

### Application Profiles
- **development**: H2 database, debug logging, hot reload
- **production**: SQLite database, optimized for deployment
- **test**: In-memory H2, isolated test environment

### Important Configuration Files
- **`application.yml`** - Main configuration with FRC API settings
- **`application-development.yml`** - Development-specific overrides
- **`application-production.yml`** - Production deployment settings

### Environment Variables
```bash
# Database
SPRING_PROFILES_ACTIVE=development

# Security
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure_password

# FRC API Integration
FRC_API_USERNAME=your_frc_username
FRC_API_AUTH_KEY=your_frc_auth_key
TEAM_NUMBER=2408
```

## Testing Guidelines

### Test Structure
- **Unit Tests**: `src/test/java/org/frcpm/services/impl/` - Service layer tests
- **Integration Tests**: `src/test/java/org/frcpm/repositories/` - Database integration
- **Controller Tests**: `src/test/java/org/frcpm/web/controllers/` - Web layer tests
- **Security Tests**: `src/test/java/org/frcpm/security/` - Authentication tests

### Test Patterns
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Use `@DataJpaTest` for repository tests
- Mock external dependencies with `@MockBean`

## Security Considerations

### Role-Based Access Control
```java
@PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")  // Management functions
@PreAuthorize("isAuthenticated()")              // Basic access
@PreAuthorize("hasRole('ADMIN')")               // Admin-only operations
```

### COPPA Compliance
- Automatic age verification for student accounts
- Parental consent workflow for users under 13
- Data minimization and retention policies
- Comprehensive audit logging for compliance

## Common Issues and Solutions

### Compilation Issues
- Ensure Java 21 is configured in IDE and Maven
- Check for excluded files in `pom.xml` (JavaFX remnants)
- Verify Spring Boot version compatibility

### Database Issues
- Delete H2 database files to reset schema
- Check application.yml for correct profile settings
- Verify HikariCP connection pool configuration

### WebSocket Issues
- Ensure WebSocket configuration is properly loaded
- Check CORS settings for local development
- Verify SockJS/STOMP client integration

## Immediate Development Priorities

1. **TaskDependencyController** - Complete REST API endpoints
2. **TaskDependencyDto** - API response objects
3. **Dependency UI Templates** - Management interfaces
4. **Critical Path Visualization** - Advanced project planning
5. **Integration Testing** - Comprehensive test coverage

## Reference Files

### Large Implementation Files (>10KB)
- **`TaskController.java`** (~15KB) - Complete controller pattern
- **`TaskDependencyServiceImpl.java`** (~25KB) - Enterprise service layer
- **`TaskDependencyRepository.java`** (~15KB) - Specialized queries
- **`tasks/kanban.html`** (~12KB) - Production-ready UI

### Configuration Examples
- **`SecurityConfig.java`** - Complete security setup
- **`WebSocketConfig.java`** - Real-time feature configuration
- **`DatabaseConfig.java`** - JPA and connection pooling setup

## Support Resources

- **H2 Console**: http://localhost:8080/h2-console (development)
- **Application Health**: http://localhost:8080/actuator/health
- **WebSocket Test**: http://localhost:8080/tasks/kanban (real-time features)
- **API Documentation**: Follow OpenAPI patterns in existing controllers