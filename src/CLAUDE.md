# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## FRC Project Visualization & Management System

This Spring Boot application supports FIRST Robotics Competition (FRC) teams in visualizing build projects through interactive Gantt charts and managing team responsibilities via daily to-do lists.

- **Architecture**: Spring Boot 3.2 + Spring Data JPA + Thymeleaf + Spring Security
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
| Framework        | Spring Boot 3.2.x        | Full Spring ecosystem |
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
| Dependency Injection | Spring IoC Container | All services use @Service, @Component, @Repository |
| Transaction Management | Spring @Transactional | Database operations |
| Validation       | Spring Validation + Bean Validation | Input validation |
| Configuration    | Spring @Configuration + @Profile | Environment-specific configs |

**Note**: This project **DOES NOT** use TestFX architecture. The system has been fully transitioned from JavaFX to a **Spring Boot web application** with Thymeleaf templates. All components use Spring's dependency injection and lifecycle management.

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

# Run security tests
mvn test -Dtest=*SecurityTest

# Run repository tests
mvn test -Dtest=*RepositoryTest
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
  - **`config/`** - Spring configuration classes (@Configuration, @EnableWebSecurity, @EnableWebSocket)
  - **`models/`** - JPA entities (@Entity, @Table, Spring Data JPA)
  - **`repositories/spring/`** - Spring Data JPA repositories (@Repository, extending JpaRepository)
  - **`services/`** - Business logic layer interfaces
  - **`services/impl/`** - Service implementations (@Service, @Transactional)
  - **`web/controllers/`** - Spring MVC controllers (@RestController, @Controller)
  - **`web/dto/`** - Data transfer objects with validation (@Valid, @NotNull, etc.)
  - **`web/websocket/`** - WebSocket controllers (@MessageMapping, @SendTo)
  - **`security/`** - Spring Security configuration and services
  - **`events/`** - Spring Application Events (@EventListener)
  - **`utils/`** - Utility classes (@Component where appropriate)

### Spring Framework Integration
All components follow Spring best practices:
- **Dependency Injection**: @Autowired, @Qualifier, constructor injection
- **Transaction Management**: @Transactional on service methods
- **Security**: @PreAuthorize, @Secured annotations
- **Configuration**: @ConfigurationProperties, @Value for property injection
- **Profiles**: @Profile for environment-specific beans
- **Events**: @EventListener for decoupled communication
- **Validation**: @Valid, @Validated for input validation
- **Caching**: @Cacheable for performance optimization

### Key Domain Models
- **`Task`** - Core task entity with Kanban workflow
- **`TaskDependency`** - Advanced task dependencies (Phase 2E-D)
- **`Project`** - Project management with deadlines
- **`TeamMember`** - Team profiles and role assignments
- **`User`** - Authentication with COPPA compliance

### Current Development Status
Phase 2E-D is **100% COMPLETE**. Service layer, web layer, UI layer, and all functionality are fully operational:

**✅ COMPLETED (Production Ready):**
- **TaskDependencyService** - Complete enterprise implementation (~25KB)
- **TaskDependencyRepository** - Specialized queries and operations (17.7KB)
- **TaskDependencyServiceImpl** - Full business logic with critical path analysis (31.3KB)
- **Domain Models** - TaskDependency, DependencyType entities fully operational
- **Database Schema** - task_dependencies table created and functional

**✅ PHASE 2E-D: FULLY COMPLETE**

**Core Phase 2E-D Status:**
- ✅ **TaskDependencyController** - Complete REST API implementation (49.6KB)
- ✅ **TaskDependencyDto, CriticalPathDto** - API response objects operational
- ✅ **UI Templates** - Complete dependency management interfaces:
  - `src/main/resources/templates/tasks/dependencies.html` (1,128 lines)
  - `src/main/resources/templates/tasks/critical-path.html` (1,279 lines)
- ✅ **WebSocket Integration** - Real-time dependency updates functional

**🚧 REMAINING WORK: Future Enhancements Only**

**Future Enhancements (Now in Current Phase):**
- **QR Code Attendance System** - QR code generation and scanning for quick attendance
- **Component Usage Analytics** - Detailed analytics and reporting on component utilization
- **Project List Multi-Select** - Shift/ctrl multi-select and multi-delete functionality
- **Enhanced Report Generation** - PDF and CSV export functionality (beyond placeholders)
- **Competition Checklist Tool** - Pre-competition readiness verification
- **Mentor Dashboard** - Advanced insights and team performance analytics
- **Part Ordering API Integration** - Integration with suppliers for automated ordering
- **Custom Task Templates** - FRC-specific workflow templates
- **Interactive Task Progress Overlays** - Enhanced visualization features
- **Burndown Charts** - Velocity and progress tracking reports
- **Mobile UI Panels** - Shop-floor optimized status displays

## Key Features

### Implemented Features
- **Task Management**: Complete CRUD with Kanban board
- **Real-time Updates**: WebSocket integration for live collaboration
- **Security**: Role-based access (Student/Mentor/Admin/Parent)
- **COPPA Compliance**: Data protection for students under 13
- **Task Dependencies**: Advanced service layer with critical path analysis (✅ Complete)
- **Critical Path Analysis**: Enterprise-grade CPM implementation (✅ Complete)

### Technology Stack
- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA, Spring WebSocket
- **Frontend**: Thymeleaf templates, Bootstrap 5, WebSocket (SockJS/STOMP)
- **Database**: H2 (dev), SQLite (prod), HikariCP connection pooling
- **Build**: Maven with Spring Boot plugin
- **Security**: BCrypt password hashing, MFA for mentors
- **Testing**: Spring Boot Test, @SpringBootTest, @WebMvcTest, @DataJpaTest

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
| QR code attendance          | 🚧 _(in current phase)_ |
| Shop-floor mobile panels    | 🚧 _(in current phase)_ |

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
| QR code attendance system            | 🚧 In Progress |
| Component usage analytics            | 🚧 In Progress |
| Advanced reporting (PDF/CSV)         | 🚧 In Progress |

## Development Patterns

### Spring Controller Pattern
Follow `TaskController.java` (93.6KB) for REST endpoint patterns:
- Use `@RestController` for API endpoints
- Apply `@PreAuthorize` for role-based security
- Use Spring's `@RequestMapping`, `@GetMapping`, `@PostMapping`
- Integrate WebSocket notifications for real-time updates
- Handle validation with `@Valid` and error responses consistently
- Use Spring's ResponseEntity for proper HTTP responses

### Spring Service Layer Pattern
Follow `TaskDependencyServiceImpl.java` (31.3KB) for business logic:
- Use `@Service` annotation for Spring component scanning
- Interface-based design with Spring dependency injection
- `@Transactional` operations with proper error handling
- Comprehensive validation and business rule enforcement
- Use `@Autowired` or constructor injection for dependencies
- Async operations with `@Async` for performance-critical tasks

### Thymeleaf Template Pattern
Follow `tasks/kanban.html` (78.6KB) for UI development:
- Extend `layout/base.html` for consistent styling
- Use Bootstrap 5 for responsive design
- Integrate WebSocket for real-time updates
- Apply Spring Security contexts (`sec:authorize`)
- Use Thymeleaf expressions for Spring model binding

### Spring Repository Pattern
All repositories extend Spring Data JPA interfaces:
- Use `@Repository` annotation
- Extend `JpaRepository<Entity, ID>`
- Custom queries with `@Query` annotation
- Method naming conventions for automatic query generation

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
qr_attendance_codes  -- QR code generation for attendance
component_analytics  -- Component usage tracking and analytics
```

### Key Relationships
- Tasks belong to Projects and are assigned to TeamMembers
- TaskDependencies create predecessor/successor relationships
- Users can have multiple TeamMember profiles across projects
- All changes are tracked in audit_logs for compliance
- QR codes linked to specific meetings for attendance tracking
- Component analytics track usage patterns and lifecycle

## 🗃️ Data Lifecycle Rules

| Entity      | Actionable Rules                                      |
|-------------|--------------------------------------------------------|
| Projects    | Can be archived, not deleted                          |
| Tasks       | Deletable if not linked via dependencies              |
| Components  | Deletable if unused; archived if used in any project  |
| TeamMembers | Archived per project upon departure                   |
| QR Codes    | Expire after meeting time + 1 hour                   |
| Analytics   | Aggregated monthly, raw data purged after 2 years    |

## Configuration

### Application Profiles
- **development**: H2 database, debug logging, hot reload, default admin credentials
- **production**: SQLite database, optimized for deployment, **REQUIRES ADMIN SETUP**
- **test**: In-memory H2, isolated test environment

### Important Configuration Files
- **`application.yml`** - Main configuration with Spring profiles
- **`application-development.yml`** - Development-specific overrides
- **`application-production.yml`** - Production deployment settings

### Environment Variables
```bash
# Database
SPRING_PROFILES_ACTIVE=development

# Security (DEVELOPMENT ONLY - DO NOT USE IN PRODUCTION)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure_password

# OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# QR Code Service
QR_CODE_BASE_URL=https://your-domain.com
QR_CODE_EXPIRY_HOURS=24
```

**⚠️ CRITICAL SECURITY NOTE**: The default admin credentials (admin/secure_password) are **DEVELOPMENT ONLY**. Production deployment **MUST** implement secure admin account creation during first-time setup. The application will force admin password change on first production login.

## 🔐 Security & Authentication

### Spring Security Configuration
- **Mentor login** via Google OAuth2 using Spring Security OAuth2 client
- Standard login for other roles (COPPA-compliant) using Spring Security
- Role-based access using Spring Security's authorization framework

### Role-Based Access Control
```java
@PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")  // Management functions
@PreAuthorize("isAuthenticated()")              // Basic access
@PreAuthorize("hasRole('ADMIN')")               // Admin-only operations
@Secured("ROLE_MENTOR")                         // Method-level security
```

### Production Security Requirements
- **First-time Setup**: Force admin password change with Spring Security
- **Password Policy**: BCrypt with minimum complexity requirements
- **Session Management**: Spring Security session handling with timeout
- **CSRF Protection**: Enabled by default with Spring Security
- **MFA Integration**: TOTP-based multi-factor authentication for mentors

### COPPA Compliance
- Automatic age verification for student accounts
- Parental consent workflow for users under 13
- Data minimization and retention policies
- Comprehensive audit logging for compliance using Spring's Application Events

## Testing Strategy & Structure

### Spring Boot Testing Architecture
The project follows Spring Boot testing best practices with comprehensive coverage:

### Test Structure Overview
```
src/test/java/org/frcpm/
├── repositories/           # @DataJpaTest - Repository layer testing
│   ├── *RepositoryIntegrationTest.java
│   └── spring/
│       └── *RepositoryTest.java
├── services/impl/         # @SpringBootTest - Service layer testing
│   ├── *ServiceTest.java
│   └── *ServiceImplTest.java
├── security/              # @SpringBootTest - Security testing
│   ├── *SecurityTest.java
│   ├── MFAServiceTest.java
│   └── SecurityIntegrationTest.java
└── web/controllers/       # @WebMvcTest - Controller layer testing
    └── *ControllerTest.java
```

### Testing Categories & Coverage

**1. Repository Layer Testing (✅ Complete)**
- **Technology**: `@DataJpaTest` with H2 test database
- **Coverage**: All Spring Data JPA repositories
- **Tests**: 
  - TaskRepositoryIntegrationTest.java
  - ComponentRepositoryIntegrationTest.java
  - UserRepositoryTest.java
  - AttendanceRepositoryIntegrationTest.java
  - ProjectRepositoryIntegrationTest.java
  - MilestoneRepositoryIntegrationTest.java
  - TeamMemberRepositoryIntegrationTest.java

**2. Service Layer Testing (✅ Complete)**
- **Technology**: `@SpringBootTest` with mocked dependencies
- **Coverage**: All service implementations with business logic validation
- **Tests**:
  - TaskServiceTest.java
  - ComponentServiceTest.java
  - ProjectServiceTest.java
  - AttendanceServiceTest.java
  - GanttDataServiceTest.java
  - MeetingServiceTest.java
  - MilestoneServiceTest.java
  - TeamMemberServiceTest.java
  - AuditServiceImplTest.java
  - COPPAComplianceServiceImplTest.java

**3. Security Testing (✅ Complete)**
- **Technology**: `@SpringBootTest` with Spring Security test framework
- **Coverage**: Authentication, authorization, COPPA compliance, MFA
- **Tests**:
  - SecurityIntegrationTest.java - Full security integration
  - MFAServiceTest.java - Multi-factor authentication
  - TOTPServiceTest.java - Time-based OTP validation

**4. Controller Layer Testing (🚧 Partial Coverage)**
- **Technology**: `@WebMvcTest` with MockMvc
- **Current Coverage**: Most controllers implemented and tested
- **Missing**: TaskDependencyController tests (controller exists at 49.6KB, tests needed)

### Testing Patterns & Annotations

**Spring Boot Test Annotations Used:**
```java
@SpringBootTest                    // Full integration testing
@DataJpaTest                       // Repository layer testing
@WebMvcTest(ControllerClass.class) // Controller layer testing
@TestPropertySource               // Test-specific properties
@ActiveProfiles("test")           // Test profile activation
@Transactional                    // Transaction rollback for tests
@MockBean                         // Mock Spring beans
@TestConfiguration               // Test-specific configuration
```

**Testing Best Practices:**
- All tests use Spring's dependency injection
- Integration tests use embedded H2 database
- Service tests mock external dependencies
- Security tests verify authentication and authorization
- Controller tests use MockMvc for web layer testing

### Test Execution & Coverage
```bash
# Run all tests
mvn test

# Run specific test categories
mvn test -Dtest=*RepositoryTest
mvn test -Dtest=*ServiceTest
mvn test -Dtest=*SecurityTest
mvn test -Dtest=*ControllerTest

# Generate test coverage report
mvn test jacoco:report

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

## 🌐 URLs & UI

| Resource             | URL Example                             |
|----------------------|------------------------------------------|
| H2 Console (Dev)     | `http://localhost:8080/h2-console`       |
| Gantt Chart UI       | `http://localhost:8080/projects/{id}/gantt` |
| Health Check         | `http://localhost:8080/actuator/health` |
| QR Code Generator    | `http://localhost:8080/meetings/{id}/qr` |
| Analytics Dashboard  | `http://localhost:8080/analytics/components` |

## Common Issues and Solutions

### Compilation Issues
- Ensure Java 21 is configured in IDE and Maven
- Check for excluded files in `pom.xml` (JavaFX remnants)
- Verify Spring Boot version compatibility
- Ensure all Spring annotations are properly imported

### Database Issues
- Delete H2 database files to reset schema
- Check application.yml for correct profile settings
- Verify HikariCP connection pool configuration
- Ensure @Entity classes are properly annotated

### WebSocket Issues
- Ensure WebSocket configuration is properly loaded
- Check CORS settings for local development
- Verify SockJS/STOMP client integration
- Ensure Spring WebSocket dependencies are included

### Spring Configuration Issues
- Verify @ComponentScan covers all packages
- Check @EnableJpaRepositories configuration
- Ensure @Configuration classes are properly structured
- Validate @Profile usage for environment-specific beans

## 🧹 Removed Features

| Feature                      | Status |
|------------------------------|--------|
| Competition/match tracking   | ❌     |
| Blue Alliance / FIRST APIs   | ❌     |
| GitHub integration           | ❌     |
| Messaging & chat             | ❌     |
| JavaFX/TestFX Architecture   | ❌     |

## Reference Files

### Large Implementation Files (>10KB)
- **`TaskController.java`** (93.6KB) - Complete Spring MVC controller pattern
- **`TaskDependencyController.java`** (49.6KB) - Complete dependency management controller
- **`TaskDependencyServiceImpl.java`** (31.3KB) - Enterprise service layer with Spring
- **`TaskDependencyRepository.java`** (17.7KB) - Spring Data JPA specialized queries
- **`tasks/kanban.html`** (78.6KB) - Production-ready Thymeleaf UI

### Spring Configuration Examples
- **`SecurityConfig.java`** - Complete Spring Security setup
- **`WebSocketConfig.java`** - Spring WebSocket configuration
- **`DatabaseConfig.java`** - Spring Data JPA and connection pooling setup

## Support Resources

- **H2 Console**: http://localhost:8080/h2-console (development)
- **Application Health**: http://localhost:8080/actuator/health
- **WebSocket Test**: http://localhost:8080/tasks/kanban (real-time features)
- **API Documentation**: Follow Spring Boot actuator and OpenAPI patterns

## 🎯 IMMEDIATE NEXT STEPS

Phase 2E-D Complete! Now implementing Future Enhancements:

**Core Dependency Management (✅ COMPLETE):**
1. ✅ **TaskDependencyController.java** - Spring MVC controller operational (49.6KB)
2. ✅ **TaskDependencyDto.java** and **CriticalPathDto.java** - API response objects functional
3. ✅ **Dependency management UI templates** - Production-ready interfaces:
   - `tasks/dependencies.html` (1,128 lines)
   - `tasks/critical-path.html` (1,279 lines)
4. ✅ **WebSocket real-time updates** - Integrated and functional
5. 🚧 **Add comprehensive @WebMvcTest controller tests** - Only remaining task

**Enhanced Features:**
6. **QR Code Attendance System** - Spring service with QR generation and validation
7. **Component Usage Analytics** - Spring Data JPA queries and reporting service
8. **Advanced Report Generation** - PDF/CSV export using Spring's resource handling
9. **Project Multi-Select Operations** - Enhanced UI with Spring MVC endpoints
10. **Competition Checklist Tool** - Spring service with validation framework
11. **Mentor Dashboard Analytics** - Spring Data aggregation queries and Thymeleaf templates

The service layer foundation uses enterprise-grade Spring architecture with dependency management and critical path analysis fully implemented. The remaining work focuses on Spring MVC controllers, Thymeleaf UI templates, and enhanced Spring-based feature implementations.