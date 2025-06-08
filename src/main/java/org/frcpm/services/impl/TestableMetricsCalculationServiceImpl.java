// src/main/java/org/frcpm/services/impl/TestableMetricsCalculationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.MetricsCalculationService;

import java.time.LocalDate;
import java.util.Map;

/**
 * Testable implementation of the MetricsCalculationService interface.
 * This implementation allows for constructor-based dependency injection for testing.
 */
public class TestableMetricsCalculationServiceImpl extends MetricsCalculationServiceImpl {
    
    /**
     * Constructor with repository injection for testing.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param teamMemberRepository the team member repository
     * @param milestoneRepository the milestone repository
     * @param attendanceRepository the attendance repository 
     * @param meetingRepository the meeting repository
     * @param subsystemRepository the subsystem repository
     */
    public TestableMetricsCalculationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            SubsystemRepository subsystemRepository) {
        super(projectRepository, taskRepository, teamMemberRepository, milestoneRepository,
              attendanceRepository, meetingRepository, subsystemRepository);
    }
    
    /**
     * Default constructor for MVVMFx dependency injection.
     */
    public TestableMetricsCalculationServiceImpl() {
        // Default constructor for MVVMFx
    }
}