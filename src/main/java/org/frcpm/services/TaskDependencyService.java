// src/main/java/org/frcpm/services/TaskDependencyService.java

package org.frcpm.services;

import org.frcpm.models.DependencyType;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TaskDependency;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for advanced task dependency management.
 * 
 * Provides comprehensive business logic for managing task dependencies,
 * including critical path analysis, cycle detection, and build season
 * optimization features.
 * 
 * Key Features:
 * - Dependency creation and validation
 * - Cycle detection and prevention
 * - Critical path analysis algorithms
 * - Bulk dependency operations
 * - Build season scheduling optimization
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-Phase2E-D
 * @since Phase 2E-D - Advanced Task Management
 */
public interface TaskDependencyService {
    
    // =========================================================================
    // BASIC DEPENDENCY MANAGEMENT
    // =========================================================================
    
    /**
     * Creates a new task dependency with validation.
     * 
     * @param dependentTask the task that depends on another
     * @param prerequisiteTask the task that must be completed first
     * @param dependencyType the type of dependency relationship
     * @return the created dependency
     * @throws IllegalArgumentException if dependency would create a cycle or is invalid
     */
    TaskDependency createDependency(Task dependentTask, Task prerequisiteTask, DependencyType dependencyType);
    
    /**
     * Creates a new task dependency with lag time.
     * 
     * @param dependentTask the task that depends on another
     * @param prerequisiteTask the task that must be completed first
     * @param dependencyType the type of dependency relationship
     * @param lagHours lag time in hours (positive = delay, negative = lead time)
     * @param notes optional notes explaining the dependency
     * @return the created dependency
     * @throws IllegalArgumentException if dependency would create a cycle or is invalid
     */
    TaskDependency createDependency(Task dependentTask, Task prerequisiteTask, DependencyType dependencyType, 
                                   Integer lagHours, String notes);
    
    /**
     * Updates an existing dependency.
     * 
     * @param dependencyId the dependency ID
     * @param dependencyType new dependency type
     * @param lagHours new lag time
     * @param notes new notes
     * @return the updated dependency
     * @throws IllegalArgumentException if dependency not found or update is invalid
     */
    TaskDependency updateDependency(Long dependencyId, DependencyType dependencyType, Integer lagHours, String notes);
    
    /**
     * Removes a task dependency.
     * 
     * @param dependencyId the dependency ID to remove
     * @return true if dependency was removed, false if not found
     */
    boolean removeDependency(Long dependencyId);
    
    /**
     * Removes a dependency between specific tasks.
     * 
     * @param dependentTask the dependent task
     * @param prerequisiteTask the prerequisite task
     * @return true if dependency was removed, false if not found
     */
    boolean removeDependency(Task dependentTask, Task prerequisiteTask);
    
    /**
     * Finds a dependency by ID.
     * 
     * @param dependencyId the dependency ID
     * @return optional dependency
     */
    Optional<TaskDependency> findDependencyById(Long dependencyId);
    
    // =========================================================================
    // DEPENDENCY ANALYSIS AND QUERYING
    // =========================================================================
    
    /**
     * Gets all dependencies for a task (where task is dependent).
     * 
     * @param task the task
     * @return list of dependencies where this task depends on others
     */
    List<TaskDependency> getTaskDependencies(Task task);
    
    /**
     * Gets all dependencies that depend on a task (where task is prerequisite).
     * 
     * @param task the task
     * @return list of dependencies where other tasks depend on this one
     */
    List<TaskDependency> getTaskDependents(Task task);
    
    /**
     * Gets all direct prerequisite tasks for a given task.
     * 
     * @param task the task
     * @return list of tasks that this task directly depends on
     */
    List<Task> getDirectPrerequisites(Task task);
    
    /**
     * Gets all direct dependent tasks for a given task.
     * 
     * @param task the task
     * @return list of tasks that directly depend on this task
     */
    List<Task> getDirectDependents(Task task);
    
    /**
     * Gets all transitive prerequisites for a task (entire dependency chain).
     * 
     * @param task the task
     * @return set of all tasks in the dependency chain
     */
    Set<Task> getAllPrerequisites(Task task);
    
    /**
     * Gets all transitive dependents for a task (entire dependency chain).
     * 
     * @param task the task
     * @return set of all tasks that depend on this task through any chain
     */
    Set<Task> getAllDependents(Task task);
    
    /**
     * Checks if a task can start based on its dependencies.
     * 
     * @param task the task to check
     * @return true if all dependencies are satisfied and task can start
     */
    boolean canTaskStart(Task task);
    
    /**
     * Gets the blocking dependencies for a task.
     * 
     * @param task the task
     * @return list of dependencies that are currently blocking this task
     */
    List<TaskDependency> getBlockingDependencies(Task task);
    
    // =========================================================================
    // CYCLE DETECTION AND PREVENTION
    // =========================================================================
    
    /**
     * Checks if adding a dependency would create a cycle.
     * 
     * @param dependentTask the task that would depend on another
     * @param prerequisiteTask the task that would be depended upon
     * @return true if adding this dependency would create a cycle
     */
    boolean wouldCreateCycle(Task dependentTask, Task prerequisiteTask);
    
    /**
     * Detects all cycles in a project's dependency graph.
     * 
     * @param project the project to analyze
     * @return list of cycles, where each cycle is a list of tasks in the cycle
     */
    List<List<Task>> detectCycles(Project project);
    
    /**
     * Validates the dependency graph for a project.
     * 
     * @param project the project to validate
     * @return validation result with any issues found
     */
    DependencyValidationResult validateDependencyGraph(Project project);
    
    /**
     * Finds the shortest dependency path between two tasks.
     * 
     * @param fromTask the starting task
     * @param toTask the target task
     * @return list of tasks representing the shortest path, or empty if no path exists
     */
    List<Task> findShortestDependencyPath(Task fromTask, Task toTask);
    
    // =========================================================================
    // CRITICAL PATH ANALYSIS
    // =========================================================================
    
    /**
     * Calculates the critical path for a project.
     * 
     * @param project the project to analyze
     * @return critical path analysis result
     */
    CriticalPathResult calculateCriticalPath(Project project);
    
    /**
     * Gets tasks on the critical path for a project.
     * 
     * @param project the project
     * @return list of tasks on the critical path, ordered by sequence
     */
    List<Task> getCriticalPathTasks(Project project);
    
    /**
     * Gets dependencies on the critical path for a project.
     * 
     * @param project the project
     * @return list of dependencies on the critical path
     */
    List<TaskDependency> getCriticalPathDependencies(Project project);
    
    /**
     * Calculates the total float (slack) for a task.
     * 
     * @param task the task
     * @return total float in hours, or null if cannot be calculated
     */
    Double calculateTaskFloat(Task task);
    
    /**
     * Updates critical path markers for all dependencies in a project.
     * This should be called after any significant changes to the project.
     * 
     * @param project the project to update
     * @return number of dependencies updated
     */
    int updateCriticalPathMarkers(Project project);
    
    // =========================================================================
    // PROJECT-LEVEL OPERATIONS
    // =========================================================================
    
    /**
     * Gets all dependencies within a project.
     * 
     * @param project the project
     * @param activeOnly whether to include only active dependencies
     * @return list of dependencies in the project
     */
    List<TaskDependency> getProjectDependencies(Project project, boolean activeOnly);
    
    /**
     * Gets dependency statistics for a project.
     * 
     * @param project the project
     * @return map of dependency types to their counts
     */
    Map<DependencyType, Long> getDependencyStatistics(Project project);
    
    /**
     * Removes all dependencies for a task.
     * Used when deleting a task or isolating it from the dependency graph.
     * 
     * @param task the task
     * @return number of dependencies removed
     */
    int removeAllDependenciesForTask(Task task);
    
    /**
     * Deactivates all dependencies for a task without deleting them.
     * 
     * @param task the task
     * @return number of dependencies deactivated
     */
    int deactivateDependenciesForTask(Task task);
    
    /**
     * Reactivates all dependencies for a task.
     * 
     * @param task the task
     * @return number of dependencies reactivated
     */
    int reactivateDependenciesForTask(Task task);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Creates multiple dependencies at once.
     * 
     * @param dependencies list of dependency specifications
     * @return list of created dependencies
     * @throws IllegalArgumentException if any dependency would create a cycle
     */
    List<TaskDependency> createBulkDependencies(List<DependencySpec> dependencies);
    
    /**
     * Updates dependency types for multiple dependencies.
     * 
     * @param dependencyIds list of dependency IDs to update
     * @param newType the new dependency type
     * @return number of dependencies updated
     */
    int updateDependencyTypes(List<Long> dependencyIds, DependencyType newType);
    
    /**
     * Removes multiple dependencies at once.
     * 
     * @param dependencyIds list of dependency IDs to remove
     * @return number of dependencies removed
     */
    int removeBulkDependencies(List<Long> dependencyIds);
    
    // =========================================================================
    // BUILD SEASON OPTIMIZATION
    // =========================================================================
    
    /**
     * Identifies external constraints (dependencies with significant lag time).
     * These often represent supplier deliveries or external processes.
     * 
     * @param project the project
     * @param minLagHours minimum lag time to consider external
     * @return list of external constraint dependencies
     */
    List<TaskDependency> identifyExternalConstraints(Project project, int minLagHours);
    
    /**
     * Gets tasks that are ready to start (no blocking dependencies).
     * 
     * @param project the project
     * @return list of tasks that can be started immediately
     */
    List<Task> getTasksReadyToStart(Project project);
    
    /**
     * Gets tasks that are currently blocked by dependencies.
     * 
     * @param project the project
     * @return list of blocked tasks with their blocking dependencies
     */
    Map<Task, List<TaskDependency>> getBlockedTasks(Project project);
    
    /**
     * Calculates project completion risk based on critical path and dependencies.
     * 
     * @param project the project
     * @return risk assessment result
     */
    ProjectRiskAssessment assessProjectRisk(Project project);
    
    /**
     * Optimizes task scheduling based on dependencies and constraints.
     * 
     * @param project the project
     * @return optimization recommendations
     */
    ScheduleOptimizationResult optimizeSchedule(Project project);
    
    /**
     * Gets the most connected tasks (potential bottlenecks).
     * 
     * @param project the project
     * @param limit maximum number of tasks to return
     * @return list of tasks with the most dependencies
     */
    List<Task> getMostConnectedTasks(Project project, int limit);
    
    // =========================================================================
    // HELPER CLASSES AND ENUMS
    // =========================================================================
    
    /**
     * Specification for creating a dependency in bulk operations.
     */
    class DependencySpec {
        private final Task dependentTask;
        private final Task prerequisiteTask;
        private final DependencyType dependencyType;
        private final Integer lagHours;
        private final String notes;
        
        public DependencySpec(Task dependentTask, Task prerequisiteTask, DependencyType dependencyType) {
            this(dependentTask, prerequisiteTask, dependencyType, null, null);
        }
        
        public DependencySpec(Task dependentTask, Task prerequisiteTask, DependencyType dependencyType, 
                             Integer lagHours, String notes) {
            this.dependentTask = dependentTask;
            this.prerequisiteTask = prerequisiteTask;
            this.dependencyType = dependencyType;
            this.lagHours = lagHours;
            this.notes = notes;
        }
        
        // Getters
        public Task getDependentTask() { return dependentTask; }
        public Task getPrerequisiteTask() { return prerequisiteTask; }
        public DependencyType getDependencyType() { return dependencyType; }
        public Integer getLagHours() { return lagHours; }
        public String getNotes() { return notes; }
    }
    
    /**
     * Result of dependency graph validation.
     */
    class DependencyValidationResult {
        private final boolean valid;
        private final List<String> issues;
        private final List<List<Task>> cycles;
        
        public DependencyValidationResult(boolean valid, List<String> issues, List<List<Task>> cycles) {
            this.valid = valid;
            this.issues = issues;
            this.cycles = cycles;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getIssues() { return issues; }
        public List<List<Task>> getCycles() { return cycles; }
    }
    
    /**
     * Result of critical path analysis.
     */
    class CriticalPathResult {
        private final List<Task> criticalPath;
        private final double totalDuration;
        private final Map<Task, Double> taskFloats;
        private final List<TaskDependency> criticalDependencies;
        
        public CriticalPathResult(List<Task> criticalPath, double totalDuration, 
                                 Map<Task, Double> taskFloats, List<TaskDependency> criticalDependencies) {
            this.criticalPath = criticalPath;
            this.totalDuration = totalDuration;
            this.taskFloats = taskFloats;
            this.criticalDependencies = criticalDependencies;
        }
        
        public List<Task> getCriticalPath() { return criticalPath; }
        public double getTotalDuration() { return totalDuration; }
        public Map<Task, Double> getTaskFloats() { return taskFloats; }
        public List<TaskDependency> getCriticalDependencies() { return criticalDependencies; }
    }
    
    /**
     * Project risk assessment result.
     */
    class ProjectRiskAssessment {
        private final RiskLevel overallRisk;
        private final List<String> riskFactors;
        private final List<Task> highRiskTasks;
        private final Map<String, Object> metrics;
        
        public ProjectRiskAssessment(RiskLevel overallRisk, List<String> riskFactors, 
                                   List<Task> highRiskTasks, Map<String, Object> metrics) {
            this.overallRisk = overallRisk;
            this.riskFactors = riskFactors;
            this.highRiskTasks = highRiskTasks;
            this.metrics = metrics;
        }
        
        public RiskLevel getOverallRisk() { return overallRisk; }
        public List<String> getRiskFactors() { return riskFactors; }
        public List<Task> getHighRiskTasks() { return highRiskTasks; }
        public Map<String, Object> getMetrics() { return metrics; }
    }
    
    /**
     * Schedule optimization result.
     */
    class ScheduleOptimizationResult {
        private final List<String> recommendations;
        private final Map<Task, Integer> suggestedAdjustments;
        private final double potentialTimeReduction;
        
        public ScheduleOptimizationResult(List<String> recommendations, 
                                        Map<Task, Integer> suggestedAdjustments, 
                                        double potentialTimeReduction) {
            this.recommendations = recommendations;
            this.suggestedAdjustments = suggestedAdjustments;
            this.potentialTimeReduction = potentialTimeReduction;
        }
        
        public List<String> getRecommendations() { return recommendations; }
        public Map<Task, Integer> getSuggestedAdjustments() { return suggestedAdjustments; }
        public double getPotentialTimeReduction() { return potentialTimeReduction; }
    }
    
    /**
     * Risk levels for project assessment.
     */
    enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}