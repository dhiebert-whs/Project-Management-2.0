# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## FRC Project Visualization & Management System

This Spring Boot application supports FIRST Robotics Competition (FRC) teams in visualizing build projects through interactive Gantt charts and managing team responsibilities via daily to-do lists.

- **Architecture**: Spring Boot 3.2 + Spring Data JPA + Thymeleaf
- **Java Version**: 21
- **Database**: H2 (development) / SQLite (production)
- **Build Tool**: Maven 3.11+
- **Current Phase**: Phase 2E-D (Advanced Task Management) - **100% COMPLETE**

## 🎯 **Project Focus**

This system **does not include**:
- Competition schedules or match tracking
- Messaging or internal communications
- FIRST API or The Blue Alliance API integration
- Rankings, match analysis, or live scouting

The **primary goals** are to:
- Visually represent FRC build projects via **interactive Gantt charts**
- Track **task dependencies and deadlines**
- Assign and display **group and individual to-do lists**
- Archive **completed projects** and **former team members**
- Manage **components**, with deletion allowed only when unused
- Support **mobile-friendly use**, including app-like behavior
- Export **Gantt charts to PDF/image formats**
- Enable **Google OAuth login for mentors**

## 🔧 Architecture & Tech Stack

| Component        | Technology              | Notes |
|------------------|--------------------------|-------|
| Framework        | Spring Boot 3.2.x        | Current LTS |
| Language         | Java 21                  | Records, pattern matching |
| Build Tool       | Maven 3.11+              | |
| Frontend         | Thymeleaf + Bootstrap 5  | Responsive UI |
| Gantt Chart      | `dhtmlxGantt` Standard   | Free educational license |
| Real-Time        | WebSocket (STOMP/SockJS) | Push task updates |
| Database (Dev)   | H2 (in-memory or file)   | |
| Database (Prod)  | SQLite + HikariCP        | Lightweight embedded DB |
| Authentication   | Spring Security + Google OAuth | Mentor SSO |
| Mobile Support   | PWA, responsive layout   | Installable on mobile |
| Gantt Export     | `html2canvas` + `jsPDF`  | Client-side export to PDF/image |
| Testing          | JUnit 5, Mockito, JaCoCo | Full stack coverage |

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

### Key Domain Models
- **`Task`** - Core task entity with Kanban workflow
- **`TaskDependency`** - Advanced task dependencies (Phase 2E-D)
- **`Project`** - Project management with deadlines
- **`TeamMember`** - Team profiles and role assignments
- **`User`** - Authentication with COPPA compliance

### Current Development Status
Phase 2E-D is **100% COMPLETE**. All advanced task dependency management features are operational:
- **TaskDependencyController** - REST endpoints (✅ Complete)
- **TaskDependencyDto** - API response objects (✅ Complete)
- **UI templates** - Dependency management interfaces (✅ Complete)
- **Critical Path Analysis** - Advanced project planning (✅ Complete)

## Key Features

### Implemented Features
- **Task Management**: Complete CRUD with Kanban board
- **Real-time Updates**: WebSocket integration for live collaboration
- **Security**: Role-based access (Student/Mentor/Admin/Parent)
- **COPPA Compliance**: Data protection for students under 13
- **Task Dependencies**: Advanced service layer with critical path analysis (✅ Complete)
- **Critical Path Analysis**: Enterprise-grade CPM implementation (✅ Complete)
- **Dependency Management UI**: Complete dependency management interfaces (✅ Complete)

### Technology Stack
- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf templates, Bootstrap 5, WebSocket (SockJS/STOMP)
- **Database**: H2 (dev), SQLite (prod), HikariCP connection pooling
- **Build**: Maven with Spring Boot plugin
- **Security**: BCrypt password hashing, MFA for mentors

## 📊 Gantt Chart Integration

- Library: `dhtmlxGantt` Standard Edition
- Features:
  - Drag-and-drop task scheduling
  - Visual dependencies
  - Critical path visualization
  - Inline editing
  - **PDF/Image export** using `html2canvas` + `jsPDF`

## 📱 Mobile-Friendly Features

| Feature                    | Status |
|-----------------------------|--------|
| Responsive UI (Bootstrap)   | ✅     |
| Installable as PWA          | ✅     |
| Offline fallback (basic)    | ✅     |
| Touch-friendly controls     | ✅     |
| Optional QR code attendance | 🔲 _(future)_ |

## 📑 Features Summary

| Feature                             | Status       |
|--------------------------------------|--------------|
| Gantt chart with dependencies        | ✅ Complete  |
| Group/Individual task list views     | ✅ Complete  |
| Task assignment and auditing         | ✅ Complete  |
| Real-time updates via WebSocket      | ✅ Complete  |
| Role-based access control            | ✅ Complete  |
| Project & team member archiving      | ✅ Complete  |
| Component reuse w/ archive rules     | ✅ Complete  |
| Mobile support + PWA behavior        | ✅ Complete  |
| Gantt export (PDF/image)             | ✅ Complete  |
| Google OAuth for mentor login        | ✅ Complete  |

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
task_dependencies    -- Advanced dependency relationships
meetings             -- Meeting coordination
components           -- Component tracking
audit_logs           -- Security and compliance tracking
```

### Key Relationships
- Tasks belong to Projects and are assigned to TeamMembers
- TaskDependencies create predecessor/successor relationships
- Users can have multiple TeamMember profiles across projects
- All changes are tracked in audit_logs for compliance

## 🗃️ Data Lifecycle Rules

| Entity      | Actionable Rules                                      |
|-------------|--------------------------------------------------------|
| Projects    | Can be archived, not deleted                          |
| Tasks       | Deletable if not linked via dependencies              |
| Components  | Deletable if unused; archived if used in any project  |
| TeamMembers | Archived per project upon departure                   |

## Configuration

### Application Profiles
- **development**: H2 database, debug logging, hot reload
- **production**: SQLite database, optimized for deployment
- **test**: In-memory H2, isolated test environment

### Important Configuration Files
- **`application.yml`** - Main configuration
- **`application-development.yml`** - Development-specific overrides
- **`application-production.yml`** - Production deployment settings

### Environment Variables
```bash
# Database
SPRING_PROFILES_ACTIVE=development

# Security
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure_password

# OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

## 🔐 Security & Authentication

- **Mentor login** via Google OAuth
- Standard login for other roles (COPPA-compliant)
- Role-based access:
  - `ADMIN`: All features
  - `MENTOR`: Full project/task management
  - `STUDENT`: View + task completion
  - `PARENT`: Optional, read-only

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

### Testing Commands
```bash
# Run all tests
mvn test

# Run with code coverage
mvn test jacoco:report

# Run only integration tests
mvn test -Dtest=*IntegrationTest
```

## 🌐 URLs & UI

| Resource             | URL Example                             |
|----------------------|------------------------------------------|
| H2 Console (Dev)     | `http://localhost:8080/h2-console`       |
| Gantt Chart UI       | `http://localhost:8080/projects/{id}/gantt` |
| Health Check         | `http://localhost:8080/actuator/health` |

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

## 🧹 Removed Features

| Feature                      | Status |
|------------------------------|--------|
| Competition/match tracking   | ❌     |
| Blue Alliance / FIRST APIs   | ❌     |
| GitHub integration           | ❌     |
| Messaging & chat             | ❌     |

## 🔗 Optional Future Enhancements

- Calendar/task reminder sync (Google Calendar)
- Component usage analytics
- QR-code based workshop attendance

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