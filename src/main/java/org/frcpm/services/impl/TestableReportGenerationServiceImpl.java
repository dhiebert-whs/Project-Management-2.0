// src/main/java/org/frcpm/services/impl/TestableReportGenerationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.MetricsCalculationService;
import org.frcpm.services.ReportGenerationService;
import org.frcpm.services.VisualizationService;

import java.time.LocalDate;
import java.util.Map;

/**
 * Testable implementation of the ReportGenerationService interface.
 * This implementation allows for constructor-based dependency injection for testing.
 */
public class TestableReportGenerationServiceImpl extends ReportGenerationServiceImpl {
    
    /**
     * Constructor with repository and service injection for testing.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param teamMemberRepository the team member repository
     * @param milestoneRepository the milestone repository
     * @param attendanceRepository the attendance repository
     * @param meetingRepository the meeting repository
     * @param subsystemRepository the subsystem repository
     * @param metricsService the metrics calculation service
     * @param ganttDataService the Gantt data service
     * @param visualizationService the visualization service
     */
    public TestableReportGenerationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            SubsystemRepository subsystemRepository,
            MetricsCalculationService metricsService,
            GanttDataService ganttDataService,
            VisualizationService visualizationService) {
        super(projectRepository, taskRepository, teamMemberRepository, milestoneRepository,
              attendanceRepository, meetingRepository, subsystemRepository, metricsService,
              ganttDataService, visualizationService);
    }
    
    /**
     * Default constructor for MVVMFx dependency injection.
     */
    public TestableReportGenerationServiceImpl() {
        // Default constructor for MVVMFx
    }
}