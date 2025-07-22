# FRC Project Management System 2.0

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.11+-blue.svg)](https://maven.apache.org/)
[![H2 Database](https://img.shields.io/badge/H2-Database-blue.svg)](https://www.h2database.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green.svg)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5-purple.svg)](https://getbootstrap.com/)

A comprehensive **Spring Boot web application** designed specifically for FIRST Robotics Competition (FRC) teams to manage build season projects through interactive Gantt charts, task dependencies, and real-time collaboration features.

## 🎯 Overview

The FRC Project Management System is an enterprise-grade Spring Boot application that provides FRC teams with powerful project visualization and management capabilities during the intense 6-week build season. Built on modern Spring architecture with full dependency injection, security, and real-time features.

### Key Capabilities
- **Interactive Gantt Charts** with drag-and-drop scheduling
- **Advanced Task Dependencies** with critical path analysis  
- **Real-time Collaboration** via WebSocket technology
- **COPPA-Compliant Security** for student data protection
- **Mobile-First Design** optimized for workshop tablet use
- **Component Lifecycle Management** with reuse tracking
- **Role-Based Access Control** (Student/Mentor/Admin/Parent)

## 🏗️ Architecture Overview

### Technology Stack

| **Layer** | **Technology** | **Purpose** |
|-----------|----------------|-------------|
| **Web Framework** | Spring Boot 3.2.x | Core application framework with auto-configuration |
| **Security** | Spring Security + OAuth2 | Authentication, authorization, COPPA compliance |
| **Data Access** | Spring Data JPA + Hibernate | ORM with repository pattern |
| **Database** | H2 (dev) / SQLite (prod) | Embedded databases with HikariCP pooling |
| **Web Layer** | Spring MVC + Thymeleaf | RESTful APIs and server-side rendering |
| **Real-time** | Spring WebSocket + STOMP | Live collaboration and updates |
| **Frontend** | Bootstrap 5 + Thymeleaf | Responsive UI with progressive enhancement |
| **Build** | Maven 3.11+ | Dependency management and build lifecycle |
| **Testing** | JUnit 5 + Mockito + Spring Test | Comprehensive test coverage |
| **Validation** | Bean Validation + Spring Validation | Input validation and business rules |

### Spring Framework Integration

This application leverages the full Spring ecosystem:

- **🏗️ Spring Boot**: Auto-configuration, embedded servers, production features
- **🔒 Spring Security**: Authentication, authorization, OAuth2 integration  
- **💾 Spring Data JPA**: Repository pattern, query methods, transactions
- **🌐 Spring MVC**: RESTful controllers, content negotiation, validation
- **⚡ Spring WebSocket**: Real-time bidirectional communication
- **📊 Spring Actuator**: Health checks, metrics, monitoring endpoints
- **🧪 Spring Test**: Integration testing with embedded databases

## 📁 Project Structure

```
src/main/java/org/frcpm/
├── 🚀 FrcProjectManagementApplication.java    # Spring Boot main application
│
├── ⚙️  config/                                # Spring Configuration
│   ├── DatabaseConfig.java                   # JPA + HikariCP configuration
│   ├── SecurityConfig.java                   # Spring Security setup
│   ├── WebSocketConfig.java                  # WebSocket configuration
│   └── WebConfig.java                        # MVC configuration
│
├── 🗄️  models/                               # JPA Entity Models
│   ├── Task.java                            # Core task entity with Kanban
│   ├── TaskDependency.java                  # Advanced dependency relationships
│   ├── Project.java                         # Project management
│   ├── TeamMember.java                      # Team member profiles
│   ├── User.java                            # Authentication with COPPA
│   ├── Component.java                       # Component lifecycle tracking
│   └── [50+ additional domain models]
│
├── 🏪 repositories/spring/                   # Spring Data JPA Repositories
│   ├── TaskRepository.java                  # Task data access
│   ├── TaskDependencyRepository.java        # Dependency queries (~15KB)
│   ├── ProjectRepository.java               # Project data access
│   └── [30+ additional repositories]
│
├── 🔧 services/                             # Business Logic Interfaces
│   ├── TaskService.java                     # Task management interface
│   ├── TaskDependencyService.java           # Dependency management interface
│   ├── ProjectService.java                  # Project management interface
│   └── [25+ additional service interfaces]
│
├── ⚙️  services/impl/                        # Spring Service Implementations
│   ├── TaskServiceImpl.java                 # Task business logic
│   ├── TaskDependencyServiceImpl.java       # Enterprise dependency logic (~25KB)
│   ├── ProjectServiceImpl.java              # Project business logic
│   └── [25+ additional implementations]
│
├── 🌐 web/                                  # Web Layer
│   ├── controllers/                         # Spring MVC Controllers
│   │   ├── TaskController.java              # Task REST endpoints (~15KB)
│   │   ├── ProjectController.java           # Project endpoints
│   │   ├── DashboardController.java         # Dashboard views
│   │   └── [10+ additional controllers]
│   │
│   ├── dto/                                # Data Transfer Objects
│   │   ├── TaskDependencyDto.java          # Dependency API responses
│   │   ├── CriticalPathDto.java            # Critical path data
│   │   └── [15+ additional DTOs]
│   │
│   └── websocket/                          # WebSocket Controllers
│       ├── TaskUpdateController.java       # Real-time task updates
│       └── NotificationController.java     # Live notifications
│
├── 🔐 security/                            # Security Implementation
│   ├── UserDetailsServiceImpl.java         # Spring Security integration
│   ├── MFAService.java                     # Multi-factor authentication
│   └── TOTPService.java                    # Time-based OTP
│
├── 📡 events/                              # Spring Application Events
│   └── WebSocketEventPublisher.java        # Event-driven notifications
│
└── 🛠️  utils/                               # Utility Classes
    └── TestDatabaseCleaner.java            # Test utilities
```

### Resource Structure

```
src/main/resources/
├── 📄 templates/                           # Thymeleaf Templates
│   ├── layout/
│   │   └── base.html                      # Base layout template
│   ├── tasks/
│   │   ├── kanban.html                   # Interactive Kanban board (~12KB)
│   │   ├── list.html                     # Task list view (~10KB)
│   │   ├── form.html                     # Task creation/editing (~8KB)
│   │   └── detail.html                   # Task details view
│   ├── projects/
│   │   └── gantt.html                    # Gantt chart visualization
│   └── dashboard/
│       └── index.html                    # Main dashboard
│
├── 🎨 static/                             # Static Web Assets
│   ├── css/                              # Bootstrap 5 + Custom CSS
│   ├── js/                               # JavaScript + WebSocket clients
│   └── images/                           # Application assets
│
└── ⚙️  application*.yml                    # Spring Boot Configuration
```

## 🗄️ Database Schema & Models

### Core Domain Models

#### **Task Management**
- **Task**: Core task entity with title, description, status (TODO/IN_PROGRESS/DONE/BLOCKED), assigned team member, start/due dates, estimated/actual hours, and priority. Includes audit fields for tracking creation and modification times.

#### **Advanced Task Dependencies**
- **TaskDependency**: Represents relationships between tasks with predecessor/successor links, dependency types (FINISH_TO_START, START_TO_START, etc.), lag days for scheduling delays, and active status for enabling/disabling dependencies.
- **DependencyType**: Enumeration defining six types of task relationships for flexible project scheduling.

#### **Project Management**
- **Project**: Main project container with name, description, date ranges, competition deadline, status tracking, and collections of associated tasks and team members. Includes FRC-specific fields like team number and game title.

#### **Team & User Management**
- **User**: Authentication entity with COPPA compliance features, password hashing, OAuth integration, and role-based permissions.
- **TeamMember**: Project-specific team member profiles linking users to projects with roles, skills, and participation tracking.

#### **Component Lifecycle**
- **Component**: Reusable component tracking with lifecycle management, usage analytics, cost tracking, and archive functionality for components in active use.

### Database Relationships

The system uses a comprehensive relational model:
- Tasks belong to Projects and are assigned to TeamMembers (Many-to-One relationships)
- TaskDependencies create predecessor/successor links between Tasks
- Users can have multiple TeamMember profiles across different Projects
- Components can be reused across multiple Projects (Many-to-Many)
- All changes are tracked in audit_logs for compliance and history

## 🔧 Service Layer Architecture

### Business Logic Implementation

The service layer implements enterprise-grade business logic with comprehensive Spring integration using @Service, @Transactional, and dependency injection patterns.

#### **TaskDependencyService - Critical Path Management**
**Purpose**: Enterprise-grade task dependency management with advanced project scheduling capabilities.

**Key Methods**:
- **calculateCriticalPath()**: Implements Critical Path Method (CPM) algorithm to identify the longest sequence of dependent tasks, calculate total project duration, and determine task float values
- **validateDependency()**: Prevents circular dependencies using depth-first search algorithm to detect cycles before creation
- **findReadyTasks()**: Identifies tasks with no blocking dependencies that can be started immediately
- **optimizeSchedule()**: Analyzes critical path to suggest parallelizable work, identify bottlenecks, and provide scheduling recommendations
- **calculateTaskFloat()**: Determines total and free float for each task to identify scheduling flexibility

#### **TaskService - Core Task Management**
**Purpose**: Comprehensive task lifecycle management with Kanban workflow integration.

**Key Methods**:
- **createTask()**: Creates tasks with validation, assigns team members, and publishes real-time events
- **updateTaskStatus()**: Manages Kanban status transitions with business rule enforcement
- **assignTask()**: Handles task assignment with workload balancing and notification
- **calculateProgress()**: Tracks completion percentages and actual vs estimated hours

#### **ProjectService - Project Lifecycle**
**Purpose**: Manages FRC project lifecycle from creation through competition and archiving.

**Key Methods**:
- **createProject()**: Initializes projects with FRC-specific setup (team number, game title, competition date)
- **archiveProject()**: Safely archives completed projects while preserving data integrity
- **calculateProjectHealth()**: Assesses project status using task completion, timeline adherence, and resource utilization

#### **ComponentService - Component Lifecycle Management**
**Purpose**: Tracks component reuse across projects with lifecycle and analytics.

**Key Methods**:
- **deleteComponent()**: Enforces business rule preventing deletion of components in active use
- **getUsageAnalytics()**: Calculates utilization rates, cost efficiency, and lifecycle stage analysis
- **trackComponentUsage()**: Records component usage patterns for future planning

#### **Security & Compliance Services**
- **COPPAComplianceService**: Automatically handles age verification, parental consent workflows, and data protection for users under 13
- **MFAService**: Provides time-based one-time password (TOTP) multi-factor authentication for mentor accounts
- **AuditService**: Comprehensive logging of all user actions for security and compliance tracking

## 🌐 Web Layer Architecture

### Spring MVC Controllers

#### **REST API Design**
All controllers follow Spring MVC best practices with @RestController annotations, @PreAuthorize security, comprehensive validation, and real-time WebSocket integration.

#### **TaskController - Task Management API**
**Purpose**: RESTful endpoints for complete task lifecycle management (~15KB implementation).

**Key Endpoints**:
- **GET /api/tasks**: Paginated task retrieval with filtering and sorting
- **POST /api/tasks**: Task creation with validation and real-time notifications
- **PUT /api/tasks/{id}**: Task updates with optimistic locking and conflict resolution
- **DELETE /api/tasks/{id}**: Safe deletion with dependency validation

#### **ProjectController - Project Management**
**Purpose**: Project lifecycle management with FRC-specific functionality.

**Key Endpoints**:
- **GET /api/projects**: Project listing with status filtering and search
- **POST /api/projects**: Project creation with FRC team setup
- **GET /api/projects/{id}/gantt**: Gantt chart data export for visualization

#### **WebSocket Real-time Features**
**TaskUpdateController**: Manages real-time task updates across all connected clients with project-specific subscriptions and conflict resolution.

**NotificationController**: Broadcasts system-wide notifications for important events like deadline warnings and milestone achievements.

### Thymeleaf Template Integration

#### **Interactive User Interfaces**
- **kanban.html**: Production-ready Kanban board with drag-and-drop functionality, real-time updates, and mobile optimization (~12KB)
- **gantt.html**: Interactive Gantt chart with dependency visualization, critical path highlighting, and PDF export capability
- **dashboard.html**: Comprehensive project dashboard with analytics, progress tracking, and team performance metrics

#### **Security Integration**
All templates use Spring Security's Thymeleaf integration (sec:authorize) for role-based UI rendering and COPPA-compliant data display.

## 🔐 Security Architecture

### Spring Security Configuration
**Comprehensive Security Setup**: Multi-layered security with form-based authentication, OAuth2 integration for mentors, CSRF protection, session management, and method-level security annotations.

### COPPA Compliance Implementation
**Automated Age Verification**: System automatically detects users under 13, triggers parental consent workflows, restricts account access until consent obtained, and maintains audit logs for compliance.

### Role-Based Access Control
- **ADMIN**: Full system access including user management and configuration
- **MENTOR**: Complete project and task management with team oversight
- **STUDENT**: Task completion and progress updates with view access
- **PARENT**: Read-only access to student's project involvement (COPPA requirement)

## 📊 Real-time Features

### WebSocket Integration
**Real-time Collaboration**: Full-duplex communication using Spring WebSocket with STOMP protocol for task updates, dependency changes, and team presence indicators.

**Key Features**:
- **Live Task Updates**: Instant synchronization of task status changes across all connected users
- **Dependency Notifications**: Real-time alerts when task dependencies are modified affecting user's work
- **Team Presence**: Shows which team members are actively working in the system
- **Conflict Resolution**: Handles simultaneous edits with user notification and merge capabilities

## 🧪 Testing Architecture

### Comprehensive Test Coverage

**Test Categories**:
- **Unit Tests**: Service layer business logic testing with mocked dependencies
- **Integration Tests**: Repository layer testing with embedded H2 database
- **Controller Tests**: Web layer testing using MockMvc and Spring Security test framework
- **Security Tests**: Authentication, authorization, and COPPA compliance validation

**Testing Technologies**:
- **JUnit 5**: Core testing framework with parameterized and dynamic tests
- **Mockito**: Mock object creation for service layer isolation
- **Spring Boot Test**: Integration testing with application context loading
- **TestContainers**: External service testing (when needed)

**Coverage Standards**:
- Service layer: 90%+ code coverage with business logic validation
- Repository layer: 100% method coverage with edge case testing
- Controller layer: 85%+ with security annotation verification

## 🚀 Getting Started

### Prerequisites
- **Java 21** - OpenJDK or Oracle JDK
- **Maven 3.11+** - Build and dependency management  
- **Git** - Version control
- **Modern IDE** - IntelliJ IDEA or VS Code with Java extensions

### Quick Start
```bash
# Clone repository
git clone <repository-url>
cd "Project Management 2.0"

# Start development server
mvn spring-boot:run

# Access application
open http://localhost:8080
```

### Development Workflow
```bash
# Run tests
mvn test

# Generate test coverage report
mvn test jacoco:report

# Package for production  
mvn clean package -Pprod

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=development
```

### Database Access
**Development H2 Console:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:./db/frc-project-dev`
- Username: `sa`, Password: (empty)

## 📈 Current Status & Roadmap

### ✅ Phase 2E-D: Advanced Task Management (85% Complete)

**Completed Infrastructure:**
- ✅ **TaskDependencyService** - Enterprise-grade business logic (~25KB)
- ✅ **TaskDependencyRepository** - Specialized database queries (~15KB)  
- ✅ **Critical Path Analysis** - Full CPM implementation
- ✅ **Domain Models** - TaskDependency, DependencyType entities
- ✅ **Database Schema** - Complete with relationships

**Remaining Work (15%):**
- 🚧 **TaskDependencyController** - REST API endpoints
- 🚧 **TaskDependencyDto/CriticalPathDto** - API response objects
- 🚧 **UI Templates** - Dependency management interfaces
- 🚧 **WebSocket Integration** - Real-time dependency updates

### 🎯 Enhanced Features (In Progress)

- 🚧 **QR Code Attendance System** - Workshop check-in/check-out with automatic time tracking
- 🚧 **Component Usage Analytics** - Lifecycle tracking, utilization metrics, and cost analysis
- 🚧 **Advanced Report Generation** - PDF/CSV export for project status, progress reports, and analytics
- 🚧 **Mentor Dashboard Analytics** - Team performance insights, productivity metrics, and progress visualization
- 🚧 **Competition Checklist Tool** - Pre-competition readiness verification with automated task validation
- 🚧 **Project Multi-Select Operations** - Bulk actions for project management and archiving
- 🚧 **Part Ordering API Integration** - Automated supplier integration for component ordering
- 🚧 **Custom Task Templates** - FRC-specific workflow templates for common build season tasks

## 🤝 Contributing

### Development Standards
- **Spring Framework** - Follow Spring best practices throughout
- **Code Quality** - Comprehensive test coverage with JUnit 5
- **Security** - COPPA compliance and role-based access control
- **Documentation** - Clear method documentation and architectural decisions

### Architecture Guidelines
- All services use **@Service** with **@Transactional** for data consistency
- Controllers follow **@RestController** with **@PreAuthorize** security annotations
- Repositories extend **JpaRepository** with custom **@Query** methods for complex operations
- Templates use **Thymeleaf** with **Bootstrap 5** for responsive, accessible interfaces
- Real-time features leverage **Spring WebSocket** with **STOMP** messaging protocol

## 📄 License

This project is developed for FRC teams and follows open-source principles for educational robotics communities.

---

**Built with ❤️ for FIRST Robotics Competition teams using enterprise-grade Spring Boot architecture.**