# CLAUDE.md

## FRC Project Visualization & Management System

This Spring Boot application supports FIRST Robotics Competition (FRC) teams in visualizing build projects through interactive Gantt charts and managing team responsibilities via daily to-do lists. This document guides Claude Code (claude.ai/code) and developers working within this repository.

---

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

---

## 🔧 Architecture & Tech Stack (Current Best Practices)

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

---

## 📁 Project Structure

```
src/main/java/org/frcpm/
│
├── config/           # Spring configs: security, db, websocket, OAuth
├── models/           # JPA entities: Project, Task, Component, TeamMember
├── repositories/     # Spring Data JPA repositories
├── services/         # Business logic interfaces + impl
├── web/controllers/  # REST API endpoints
├── web/dto/          # Data transfer objects
└── web/websocket/    # Real-time communication layer
```

---

## 📊 Gantt Chart Integration

- Library: `dhtmlxGantt` Standard Edition
- Features:
  - Drag-and-drop task scheduling
  - Visual dependencies
  - Critical path visualization
  - Inline editing
  - **PDF/Image export** using `html2canvas` + `jsPDF`

---

## 📱 Mobile-Friendly Features

| Feature                    | Status |
|-----------------------------|--------|
| Responsive UI (Bootstrap)   | ✅     |
| Installable as PWA          | ✅     |
| Offline fallback (basic)    | ✅     |
| Touch-friendly controls     | ✅     |
| Optional QR code attendance | 🔲 _(future)_ |

---

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

---

## ✅ Testing Best Practices

- Unit: `@DataJpaTest`, `@WebMvcTest`, Mockito for isolation
- Integration: `@SpringBootTest` with test profile
- Coverage: `jacoco:report`, aim 80%+
- Clean DB state via `@DirtiesContext` or transactional rollback

---

## 🧪 Testing Commands

```bash
# Run all tests
mvn test

# Run with code coverage
mvn test jacoco:report

# Run only integration tests
mvn test -Dtest=*IntegrationTest
```

---

## 🗃️ Data Lifecycle Rules

| Entity      | Actionable Rules                                      |
|-------------|--------------------------------------------------------|
| Projects    | Can be archived, not deleted                          |
| Tasks       | Deletable if not linked via dependencies              |
| Components  | Deletable if unused; archived if used in any project  |
| TeamMembers | Archived per project upon departure                   |

---

## 🔐 Security & Authentication

- **Mentor login** via Google OAuth
- Standard login for other roles (COPPA-compliant)
- Role-based access:
  - `ADMIN`: All features
  - `MENTOR`: Full project/task management
  - `STUDENT`: View + task completion
  - `PARENT`: Optional, read-only

---

## 🔄 Common Dev Commands

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=development
mvn clean compile
mvn clean package -Pprod
```

---

## 🌐 URLs & UI

| Resource             | URL Example                             |
|----------------------|------------------------------------------|
| H2 Console (Dev)     | `http://localhost:8080/h2-console`       |
| Gantt Chart UI       | `http://localhost:8080/projects/{id}/gantt` |
| Health Check         | `http://localhost:8080/actuator/health` |

---

## 🔧 Configuration Highlights

```yaml
# application.yml
spring:
  profiles:
    active: development
  datasource:
    url: jdbc:h2:./db/frc-project-dev
    driverClassName: org.h2.Driver

# OAuth
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
```

---

## 🧹 Removed Features

| Feature                      | Status |
|------------------------------|--------|
| Competition/match tracking   | ❌     |
| Blue Alliance / FIRST APIs   | ❌     |
| GitHub integration           | ❌     |
| Messaging & chat             | ❌     |

---

## 🔗 Optional Future Enhancements

- Calendar/task reminder sync (Google Calendar)
- Component usage analytics
- QR-code based workshop attendance

---
