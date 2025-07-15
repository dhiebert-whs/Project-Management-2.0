# CLAUDE.md - FRC Project Management System

## 🚀 PROJECT OVERVIEW

**System**: FRC Project Management System - Web-based project coordination for FIRST Robotics Competition teams  
**Version**: 2.0.0  
**Architecture**: Spring Boot 3.2+ with JPA/Hibernate, Thymeleaf templates, WebSocket real-time features  
**Current Phase**: Phase 2E-D (Advanced Task Management) - **85% COMPLETE**  
**Database**: H2 (development) / SQLite (production)  
**Deployment**: Oracle Cloud Always Free tier ready  

### Business Context
- **Target Users**: FRC teams (15-50 members: students, mentors, parents)
- **Primary Use**: 6-week build season project coordination under tight competition deadlines
- **Key Features**: Task dependencies, critical path analysis, real-time collaboration, COPPA compliance
- **Workshop Optimization**: Mobile-friendly for tablet use in robotics workshops

---

## 🎯 CURRENT STATUS & IMMEDIATE PRIORITIES

### **✅ COMPLETED (Production Ready)**
- **Foundation**: Complete Spring Boot migration from JavaFX
- **Security**: COPPA compliance, role-based access (Student/Mentor/Admin/Parent), MFA for mentors
- **Real-time**: WebSocket infrastructure with instant task updates across devices
- **Mobile**: PWA with 7-day offline capability and workshop-optimized interface
- **Kanban Board**: Production-ready drag-and-drop interface with real-time collaboration
- **CRUD Operations**: Complete task management with clean URL structure
- **Advanced Dependencies**: **Enterprise-grade service layer COMPLETE** - 25KB+ implementation with critical path analysis

### **🎯 IMMEDIATE PRIORITY: Phase 2E-D UI Completion**

**Current Position**: Service layer 100% complete, UI layer needed (85% → 100%)  
**Timeline**: 3-5 days remaining  

#### **NEXT STEPS (Priority Order)**

**Day 1: Controller Layer** 📋 **IMMEDIATE**
```java
// CREATE: src/main/java/org/frcpm/web/controllers/TaskDependencyController.java
// USE PATTERN: TaskController.java (15KB reference file)
// IMPLEMENT: REST endpoints for all TaskDependencyService methods
// FEATURES: CRUD dependencies, critical path analysis, bulk operations
```

**Day 1: DTO Classes** 📋 **IMMEDIATE**
```java
// CREATE: src/main/java/org/frcpm/web/dto/TaskDependencyDto.java
// CREATE: src/main/java/org/frcpm/web/dto/CriticalPathDto.java
// USE PATTERN: TaskDto.java (existing DTO patterns)
```

**Days 2-3: Basic UI Templates** 📋 **HIGH PRIORITY**
```html
<!-- CREATE: src/main/resources/templates/tasks/dependencies.html -->
<!-- USE PATTERN: tasks/form.html (8KB) and tasks/list.html (10KB) -->
<!-- IMPLEMENT: Dependency creation, editing, listing interfaces -->
```

**Day 4: Advanced Visualization** 📋 **ENHANCEMENT**
```html
<!-- CREATE: src/main/resources/templates/tasks/critical-path.html -->
<!-- IMPLEMENT: Critical path visualization, dependency graphs -->
```

**Day 5: Testing & Integration** 📋 **COMPLETION**
```java
// CREATE: src/test/java/org/frcpm/services/impl/TaskDependencyServiceTest.java
// CREATE: src/test/java/org/frcpm/web/controllers/TaskDependencyControllerTest.java
```

---

## 📁 FILE INVENTORY & PATTERNS

### **🏆 REFERENCE FILES (Use as Patterns)**

#### **Controller Pattern Reference**
```
📊 src/main/java/org/frcpm/web/controllers/TaskController.java (~15KB)
   - REST endpoint patterns
   - WebSocket integration  
   - Error handling
   - Security annotations
```

#### **Service Pattern Reference**
```
📊 src/main/java/org/frcpm/services/impl/TaskDependencyServiceImpl.java (~25KB) ⭐ COMPLETE
   - Enterprise service implementation with 40+ methods
   - Critical path analysis algorithms
   - Cycle detection and prevention
   - Build season optimization
   - Bulk operations
```

#### **Template Pattern Reference**
```
📊 src/main/resources/templates/tasks/form.html (~8KB)
📊 src/main/resources/templates/tasks/list.html (~10KB)
📊 src/main/resources/templates/tasks/kanban.html (~12KB) ⭐ PRODUCTION READY
```

#### **Repository Pattern Reference**
```
📊 src/main/java/org/frcpm/repositories/spring/TaskDependencyRepository.java (~15KB) ⭐ COMPLETE
   - 30+ specialized dependency queries
   - Critical path analysis queries
   - Performance-optimized bulk operations
```

### **✅ FULLY OPERATIONAL CORE FILES**

#### **Application Infrastructure**
```
✅ src/main/java/org/frcpm/FrcProjectManagementApplication.java - Main Spring Boot app
✅ src/main/java/org/frcpm/config/ - Complete configuration (Security, Database, WebSocket)
✅ src/main/java/org/frcpm/security/ - COPPA compliance + authentication
✅ src/main/resources/application.yml - Enhanced production-ready configuration
```

#### **Domain Models (All Operational)**
```
✅ src/main/java/org/frcpm/models/Task.java (~3KB) - Core task with Kanban status
✅ src/main/java/org/frcpm/models/TaskDependency.java (~8.7KB) ⭐ NEW - Advanced dependencies
✅ src/main/java/org/frcpm/models/DependencyType.java (~4.2KB) ⭐ NEW - 6 dependency types
✅ src/main/java/org/frcpm/models/Project.java (~3.2KB) - Project management
✅ src/main/java/org/frcpm/models/TeamMember.java (~4.5KB) - Team profiles
✅ src/main/java/org/frcpm/models/User.java - Authentication with COPPA support
```

#### **Service Layer (All Operational)**
```
✅ src/main/java/org/frcpm/services/TaskService.java (~3.8KB) - Interface with async
✅ src/main/java/org/frcpm/services/impl/TaskServiceImpl.java (~8KB) - Complete implementation
✅ src/main/java/org/frcpm/services/TaskDependencyService.java (~12KB) ⭐ NEW - Complete interface
✅ src/main/java/org/frcpm/services/impl/TaskDependencyServiceImpl.java (~25KB) ⭐ NEW - Enterprise implementation
```

#### **Web Layer (Operational)**
```
✅ src/main/java/org/frcpm/web/controllers/TaskController.java (~15KB) - Complete with Kanban API
✅ src/main/java/org/frcpm/web/controllers/DashboardController.java - Project dashboard
✅ src/main/java/org/frcpm/web/dto/ - All DTO classes
✅ src/main/java/org/frcpm/web/websocket/ - Complete WebSocket infrastructure
```

#### **Templates (All Working)**
```
✅ src/main/resources/templates/layout/base.html (~7KB) - Base template with PWA
✅ src/main/resources/templates/tasks/kanban.html (~12KB) ⭐ PRODUCTION-READY
✅ src/main/resources/templates/tasks/list.html (~10KB) - Enhanced with real-time
✅ src/main/resources/templates/tasks/form.html (~8KB) - Comprehensive task form
✅ src/main/resources/templates/tasks/detail.html (~6KB) - Detailed task view
✅ src/main/resources/templates/tasks/create.html (~1.1KB) - Redirect template
```

---

## 💾 DATABASE SCHEMA

### **Core Tables (All Operational)**
```sql
✅ users                 - Authentication with COPPA compliance
✅ team_members         - Team profiles and role assignments
✅ projects             - Project management with deadlines
✅ tasks                - Task management with Kanban workflow
✅ task_dependencies    - ⭐ NEW - Advanced dependency relationships
✅ meetings             - Meeting coordination
✅ components           - Component tracking
✅ audit_logs           - Security and compliance tracking
```

### **Task Dependencies Schema (New in Phase 2E-D)**
```sql
-- OPERATIONAL TABLE: task_dependencies
CREATE TABLE task_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    predecessor_task_id BIGINT NOT NULL,
    successor_task_id BIGINT NOT NULL,
    dependency_type VARCHAR(20) NOT NULL,  -- 6 types supported
    lag_days INTEGER DEFAULT 0,            -- Scheduling delays
    is_active BOOLEAN DEFAULT true,        -- Enable/disable dependencies
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    project_id BIGINT NOT NULL,            -- Cross-reference validation
    
    FOREIGN KEY (predecessor_task_id) REFERENCES tasks(id),
    FOREIGN KEY (successor_task_id) REFERENCES tasks(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    
    UNIQUE(predecessor_task_id, successor_task_id)  -- Prevent duplicates
);
```

### **Database URLs**
```yaml
# Development
spring.datasource.url: jdbc:h2:./db/frc-project-dev
# H2 Console: http://localhost:8080/h2-console
# Credentials: username=sa, password=(empty)

# Production  
spring.datasource.url: jdbc:sqlite:/app/data/frc-project.db
```

---

## 🔧 DEVELOPMENT ENVIRONMENT

### **✅ VERIFIED WORKING SETUP**
```bash
# Current development workflow (ALL WORKING)
git checkout main              # ✅ Latest code with Phase 2E-D service layer
mvn clean compile              # ✅ SUCCEEDS - All compilation errors resolved
mvn spring-boot:run            # ✅ SUCCEEDS - Application starts successfully

# Application URLs (all operational):
http://localhost:8080                    # ✅ Main dashboard
http://localhost:8080/tasks/kanban      # ✅ Production-ready Kanban board  
http://localhost:8080/tasks             # ✅ Enhanced task list with real-time
http://localhost:8080/tasks/new         # ✅ Comprehensive task creation
http://localhost:8080/h2-console        # ✅ Database with task_dependencies table
```

### **Prerequisites**
- **Java 21** ✅ Required and working
- **Maven 3.9+** ✅ Required and working
- **Modern IDE** (IntelliJ IDEA, VS Code with Java extensions)
- **Git** for version control

### **IDE Configuration**
```json
// VS Code settings.json recommendations
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "spring-boot.ls.problem.application-properties.enabled": true,
    "files.associations": {
        "*.html": "html",
        "*.yml": "yaml"
    }
}
```

---

## 🚀 API PATTERNS & CONVENTIONS

### **Controller Pattern (Follow TaskController.java)**
```java
@RestController
@RequestMapping("/api/dependencies")
@PreAuthorize("isAuthenticated()")
public class TaskDependencyController {
    
    private final TaskDependencyService dependencyService;
    private final WebSocketController webSocketController; // For real-time updates
    
    @GetMapping
    public ResponseEntity<List<TaskDependencyDto>> getDependencies(@RequestParam Long projectId);
    
    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<TaskDependencyDto> createDependency(@RequestBody TaskDependencyDto dto);
    
    @GetMapping("/critical-path")
    public ResponseEntity<CriticalPathDto> getCriticalPath(@RequestParam Long projectId);
    
    // WebSocket integration for real-time updates
    // Send notifications via webSocketController.notifyDependencyUpdate()
}
```

### **Service Integration Pattern**
```java
// TaskDependencyService methods available (40+ methods):
dependencyService.createDependency(TaskDependency dependency);
dependencyService.findByProject(Long projectId);
dependencyService.calculateCriticalPath(Long projectId);
dependencyService.findBlockedTasks(Long projectId);
dependencyService.optimizeSchedule(Long projectId);
dependencyService.detectCycles(Long projectId);
// ... and 30+ more specialized methods
```

### **DTO Pattern (Follow TaskDto.java)**
```java
public class TaskDependencyDto {
    private Long id;
    private Long predecessorTaskId;
    private String predecessorTaskTitle;  // For UI display
    private Long successorTaskId;
    private String successorTaskTitle;    // For UI display
    private DependencyType type;
    private Integer lagDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    // Include display names for clean UI presentation
}
```

### **WebSocket Integration Pattern**
```java
// Real-time update pattern (follow TaskController pattern)
@MessageMapping("/dependency/update")
@SendTo("/topic/project/{projectId}/dependencies")
public DependencyUpdateMessage updateDependency(@Payload DependencyUpdateMessage message);

// Server-side notification pattern
webSocketController.notifyDependencyUpdate(new DependencyUpdateMessage(
    projectId, dependencyId, "CREATED", userFullName
));
```

---

## 🎨 UI PATTERNS & TEMPLATES

### **Template Structure Pattern**
```html
<!-- Follow base.html pattern for consistent layout -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title>Task Dependencies - [[${project.name}]]</title>
</head>
<body>
    <div layout:fragment="content">
        <!-- Main content here -->
        <!-- Use Bootstrap 5 classes for responsive design -->
        <!-- Include WebSocket integration for real-time updates -->
    </div>
    
    <div layout:fragment="scripts">
        <!-- Page-specific JavaScript -->
        <!-- WebSocket connection and event handlers -->
    </div>
</body>
</html>
```

### **Form Pattern (Follow tasks/form.html)**
```html
<!-- Dependency creation/editing form -->
<form th:action="@{/tasks/dependencies}" th:object="${dependency}" method="post" 
      class="needs-validation" novalidate>
    
    <!-- Predecessor Task Selection -->
    <div class="mb-3">
        <label for="predecessorTask" class="form-label">Predecessor Task</label>
        <select class="form-select" th:field="*{predecessorTaskId}" required>
            <option value="">Select predecessor task...</option>
            <option th:each="task : ${availableTasks}" 
                    th:value="${task.id}" 
                    th:text="${task.title}">Task Title</option>
        </select>
    </div>
    
    <!-- Dependency Type Selection -->
    <div class="mb-3">
        <label for="dependencyType" class="form-label">Dependency Type</label>
        <select class="form-select" th:field="*{type}" required>
            <option th:each="type : ${T(org.frcpm.models.DependencyType).values()}"
                    th:value="${type}" 
                    th:text="${type.displayName}">Type</option>
        </select>
    </div>
    
    <!-- Submit with proper error handling -->
    <button type="submit" class="btn btn-primary">Create Dependency</button>
</form>
```

### **List Pattern (Follow tasks/list.html)**
```html
<!-- Dependency list with real-time updates -->
<div id="dependency-list" class="dependency-list">
    <div th:each="dependency : ${dependencies}" 
         class="list-group-item dependency-item"
         th:data-dependency-id="${dependency.id}">
        
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h6 class="mb-1">
                    <span th:text="${dependency.predecessorTaskTitle}">Predecessor</span>
                    <i class="fas fa-arrow-right mx-2"></i>
                    <span th:text="${dependency.successorTaskTitle}">Successor</span>
                </h6>
                <small class="text-muted" th:text="${dependency.type.displayName}">Type</small>
            </div>
            
            <!-- Action buttons for mentors/admins -->
            <div sec:authorize="hasAnyRole('MENTOR', 'ADMIN')">
                <button class="btn btn-sm btn-outline-danger" 
                        onclick="removeDependency([[${dependency.id}]])">
                    Remove
                </button>
            </div>
        </div>
    </div>
</div>
```

### **WebSocket JavaScript Pattern**
```javascript
// WebSocket integration for real-time dependency updates
let stompClient = null;
let projectId = /*[[${project.id}]]*/ null;

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket
    });
    
    stompClient.onConnect = function(frame) {
        // Subscribe to dependency updates
        stompClient.subscribe(`/topic/project/${projectId}/dependencies`, function(message) {
            const update = JSON.parse(message.body);
            handleDependencyUpdate(update);
        });
    };
    
    stompClient.activate();
}

function handleDependencyUpdate(update) {
    // Update UI based on dependency changes
    // Add visual feedback for real-time updates
    // Refresh critical path visualization if needed
}
```

---

## 🧪 TESTING PATTERNS

### **Service Test Pattern**
```java
@SpringBootTest
@Transactional
class TaskDependencyServiceTest {
    
    @Autowired
    private TaskDependencyService dependencyService;
    
    @Test
    void shouldCalculateCriticalPath() {
        // Create test project with dependencies
        // Verify critical path calculation
        // Assert expected critical tasks and float values
    }
    
    @Test  
    void shouldDetectCycles() {
        // Create circular dependency
        // Verify cycle detection prevents creation
        // Assert appropriate error handling
    }
}
```

### **Controller Test Pattern**
```java
@WebMvcTest(TaskDependencyController.class)
class TaskDependencyControllerTest {
    
    @MockBean
    private TaskDependencyService dependencyService;
    
    @Test
    @WithMockUser(roles = "MENTOR")
    void shouldCreateDependency() throws Exception {
        mockMvc.perform(post("/api/dependencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dependencyDto)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.id").exists());
    }
}
```

---

## 🔒 SECURITY & COMPLIANCE

### **Role-Based Access Control**
```java
// Security annotations for dependency management
@PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")  // Dependency creation/deletion
@PreAuthorize("isAuthenticated()")              // View dependencies
@PreAuthorize("hasRole('ADMIN')")               // Bulk operations

// COPPA compliance for students under 13
// Automatic data minimization and parental consent tracking
// Audit logging for all dependency-related actions
```

### **COPPA Compliance Integration**
```java
// All dependency actions automatically logged for COPPA compliance
// Students under 13 have limited dependency modification rights
// Parental consent required for advanced project planning features
// Data minimization enforced for minor users
```

---

## 📊 BUSINESS LOGIC (TaskDependencyService)

### **Core Operations (All Implemented)**
```java
// Dependency CRUD
createDependency(TaskDependency dependency) // With cycle prevention
updateDependency(Long id, TaskDependency dependency)
deleteDependency(Long id)
findByProject(Long projectId)

// Critical Path Analysis (Enterprise-grade CPM implementation)
calculateCriticalPath(Long projectId) // Complete CPM algorithm
getCriticalTasks(Long projectId)      // Tasks on critical path
calculateTaskFloat(Long taskId)       // Total and free float

// Schedule Optimization
optimizeSchedule(Long projectId)      // Suggests schedule improvements
findParallelizableWork(Long projectId) // Identifies concurrent work opportunities
getScheduleRisk(Long projectId)       // Risk assessment with metrics

// Build Season Intelligence (FRC-specific)
findBlockedTasks(Long projectId)      // Shows blocking dependencies
findReadyTasks(Long projectId)        // Tasks ready to start
detectBottlenecks(Long projectId)     // Identifies project bottlenecks
```

### **Advanced Features (All Implemented)**
```java
// Cycle Detection and Prevention
validateDependency(TaskDependency dependency) // Prevents cycles
detectCycles(Long projectId)                  // Find existing cycles
getShortestPath(Long fromTaskId, Long toTaskId) // Dependency path analysis

// Bulk Operations (Performance optimized)
createBulkDependencies(List<TaskDependency> dependencies)
updateProjectDependencies(Long projectId, List<TaskDependency> dependencies)
validateBulkDependencies(List<TaskDependency> dependencies)

// Analytics and Reporting
getDependencyMetrics(Long projectId)    // Project complexity metrics
getConnectivityAnalysis(Long projectId) // Task interconnection analysis
```

---

## 🚀 DEPLOYMENT CONFIGURATION

### **Development Profile**
```yaml
spring:
  profiles:
    active: development
  datasource:
    url: jdbc:h2:./db/frc-project-dev
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
```

### **Production Profile (Oracle Cloud Ready)**
```yaml
spring:
  profiles:
    active: production
  datasource:
    url: jdbc:sqlite:/app/data/frc-project.db
    driver-class-name: org.sqlite.JDBC
server:
  port: 8080
```

### **Docker Deployment**
```yaml
# docker-compose.yml (Ready for Oracle Cloud)
version: '3.8'
services:
  frc-pm:
    image: frc-project-management:2.0.0-phase2ed
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
    volumes:
      - ./data:/app/data
      - ./logs:/app/logs
```

---

## 🎯 SUCCESS CRITERIA

### **Phase 2E-D Completion Checklist**
- [ ] **TaskDependencyController** - REST endpoints operational
- [ ] **TaskDependencyDto** - Clean API responses  
- [ ] **CriticalPathDto** - Critical path visualization data
- [ ] **dependencies.html** - Basic dependency management UI
- [ ] **critical-path.html** - Critical path visualization
- [ ] **WebSocket Integration** - Real-time dependency updates
- [ ] **Testing Complete** - Service and controller tests passing
- [ ] **No Regressions** - All existing functionality preserved

### **Quality Metrics**
- **Response Time**: <2 seconds for all dependency operations
- **Real-time Updates**: <500ms WebSocket update latency  
- **Mobile Compatibility**: 95%+ Lighthouse score on tablets
- **Test Coverage**: 85%+ on new controller and integration code
- **Security**: All endpoints properly secured with role-based access

---

## 📚 KEY RESOURCES

### **Essential Reference Files (Large - Share Individually)**
1. **TaskController.java** (~15KB) - Complete controller pattern with WebSocket
2. **TaskDependencyServiceImpl.java** (~25KB) - Complete business logic implementation  
3. **TaskDependencyRepository.java** (~15KB) - Comprehensive repository with specialized queries
4. **tasks/kanban.html** (~12KB) - Production-ready UI with real-time features
5. **tasks/form.html** (~8KB) - Comprehensive form pattern with validation

### **Documentation**
- **Phase 2E-C Documentation** - Complete Kanban implementation guide
- **WebSocket Integration Guide** - Real-time collaboration patterns
- **Security Implementation** - COPPA compliance and authentication
- **Database Schema** - Complete relationship documentation

### **Testing Resources**
- **Existing Service Tests** - Patterns for dependency service testing
- **Controller Test Patterns** - WebMvcTest examples with security
- **Integration Test Suite** - Full application testing patterns

---

## ⚠️ IMPORTANT NOTES

### **File Size Optimization**
- **Large files (>5KB)**: Share individually to preserve detail
- **Reference patterns**: Study TaskController.java before implementing TaskDependencyController
- **Template patterns**: Follow tasks/form.html and tasks/list.html patterns exactly

### **Development Strategy**
- **Service-first approach**: Business logic complete, focus on UI integration
- **Incremental development**: Start with basic CRUD, enhance with visualizations
- **Pattern following**: Use existing code as templates to maintain consistency
- **Real-time integration**: Every UI action should trigger WebSocket updates

### **Common Pitfalls to Avoid**
- **Don't break existing functionality** - All current features must remain operational
- **Follow security patterns** - Use existing @PreAuthorize patterns consistently  
- **Maintain WebSocket integration** - Every dependency change needs real-time updates
- **Preserve mobile optimization** - All new UI must work on workshop tablets

---

## 🏁 READY TO DEVELOP

**Current State**: Clean codebase, all services operational, clear implementation path  
**Next Step**: Create TaskDependencyController.java following TaskController.java pattern  
**Timeline**: 3-5 days to complete Phase 2E-D including advanced dependency visualization  
**Support**: Complete service layer operational, comprehensive reference files available