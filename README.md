# **FRC Project Management System**

## **Overview & Technical Specification (JavaFX Version)**

### **1\. Executive Summary**

The FRC Project Management System is a desktop application purpose-built to support FIRST Robotics Competition (FRC) teams as they plan, organize, and execute their build season. This tool focuses on enabling effective time and resource management for student-driven teams under fixed seasonal deadlines. Designed with a modular, maintainable JavaFX MVVM architecture, the system aims to support detailed task planning, subteam coordination, attendance tracking, and project visualization through an intuitive, single-user interface.

---

### **2\. Current Status and Transition Overview**

The system has been fully transitioned away from the prior Django-based web application architecture. It is now implemented as a **single-user, desktop-only** JavaFX application. Python, Django, and related technologies are no longer part of the stack.

**Key differences from the previous plan:**

* Entirely desktop-based, single-user system (multi-user collaboration deferred)

* Architecture now adheres to the **Model-View-ViewModel (MVVM)** pattern

* UI built using **JavaFX FXML**, styled with **custom CSS**, and editable via **Scene Builder**

* Gantt chart and other visualizations will be rendered using **Chart.js** in an embedded WebView

* **H2 database** with **HikariCP** connection pooling and **JPA** for persistence

* Manual repository implementations (dependency injection not yet implemented)

---

### **3\. Target Users**

* Mentors and coaches

* Student team leads and subteam captains

* Team members tracking tasks and schedules

---

### **4\. Key Features (Current Scope)**

* **Project Configuration**: Name, start/end dates, competition deadline, notes

* **Milestone Management**: Milestone creation, dependency tracking, timeline visualization

* **Meeting Schedule**: Recurring and one-time meeting definitions with holiday exclusions

* **Subteam and Subsystem Setup**: Define teams, assign responsibilities, link subsystems

* **Member Management**: Track members, subteam roles, skills, and assignments

* **Task Management**: Create tasks with durations, dependencies, and resource needs

* **Progress Tracking**: Track completion, effort, blockers, and critical path

* **Attendance Logging**: Meeting attendance, real-time impact on tasks

* **Visualization**: Gantt chart (via Chart.js), dashboards, task filtering

* **Data Export/Import**: JSON-based full-project backups and templates

---

### **5\. System Architecture**

#### **5.1 MVVM Structure**

* **Model**: JPA entities representing domain data (Project, Task, TeamMember, etc.)

* **View**: FXML-defined UIs styled with CSS; managed by JavaFX controllers

* **ViewModel**: Handles UI logic, state management, observable bindings, command actions

* **Service Layer** (planned): Business logic and coordination between ViewModel and Repository

* **Repository Layer**: Manual JPA repository implementations (HikariCP \+ H2)

#### **5.2 Technologies**

* **UI**: JavaFX (FXML \+ CSS), Scene Builder

* **Visualization**: Chart.js embedded in WebView (Gantt chart, dashboards)

* **Persistence**: JPA \+ H2 \+ HikariCP

* **Language**: Java 17+

* **Packaging**: JavaFX self-contained bundle (executable JAR or native image)

#### **5.3 Dependency Injection (Planned)**

* Currently not using a DI framework

* **Spring Framework** is the preferred candidate for DI:

  * *Pros*: Industry standard, powerful lifecycle management, integrates well with JPA

  * *Cons*: Larger footprint and steeper learning curve compared to lightweight DI frameworks

  * *Integration Goal*: Use Spring context to manage service and repository beans; evaluate integration strategies with JavaFX lifecycle

---

### **6\. Deployment Focus**

* **Primary**: Windows desktop application (JavaFX-based)

* **Packaging Options**:

  * JavaFX application packaged via jlink for native deployment

  * WebStart-style installer for easy updates (e.g., via Getdown or FXLauncher)

---

### **7\. Near-Term Goals**

* Implement core modules (task tracking, subteam management, meeting scheduling)

* Add project deletion functionality

* Finalize Chart.js Gantt integration

* Evaluate Spring for DI integration and maintainability

* Add portable mode (no install, USB-friendly)

---

### **8\. Future Development Possibilities**

#### **Multi-User Enhancements**

* Local server mode to support multiple collaborators on same LAN

* Cloud sync option using Firebase or custom backend

#### **Feature Additions**

* Competition checklist tool

* Mentor dashboard with team insights

* QR-code or barcode support for quick task/part updates

* Part ordering API integration

* Custom task templates for common FRC workflows

#### **Visualization Enhancements**

* Interactive task progress overlays

* Burndown charts and velocity reports

* Mobile-friendly UI panels for shop-floor status checks

---

This overview serves as a living reference for current system direction and future planning. All implementation is guided by clarity, testability, and maintainability in a modern JavaFX environment.

