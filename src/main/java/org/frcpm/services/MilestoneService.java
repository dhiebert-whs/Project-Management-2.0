package org.frcpm.services;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing Milestone entities.
 */
public interface MilestoneService extends Service<Milestone, Long> {

    /**
     * Finds all milestones for a specific project.
     *
     * @param project The project to find milestones for
     * @return List of milestones for the project
     */
    List<Milestone> findByProject(Project project);

    /**
     * Finds all milestones with a deadline on or before a specific date.
     *
     * @param date The date to check against
     * @return List of milestones with deadlines on or before the date
     */
    List<Milestone> findByDeadlineBefore(LocalDate date);

    /**
     * Finds all milestones with a deadline on or after a specific date.
     *
     * @param date The date to check against
     * @return List of milestones with deadlines on or after the date
     */
    List<Milestone> findByDeadlineAfter(LocalDate date);

    /**
     * Creates a new milestone for a project.
     *
     * @param project The project to create the milestone for
     * @param name The name of the milestone
     * @param description The description of the milestone
     * @param deadline The deadline for the milestone
     * @return The created milestone
     */
    Milestone createMilestone(Project project, String name, String description, LocalDate deadline);

    /**
     * Updates the completion status of a milestone.
     *
     * @param milestone The milestone to update
     * @param completed Whether the milestone is completed
     * @return The updated milestone
     */
    Milestone updateCompletionStatus(Milestone milestone, boolean completed);

    /**
     * Updates the completion date of a milestone.
     *
     * @param milestone The milestone to update
     * @param completionDate The completion date of the milestone
     * @return The updated milestone
     */
    Milestone updateCompletionDate(Milestone milestone, LocalDate completionDate);

    /**
     * Sets a dependency between two milestones.
     *
     * @param milestone The milestone that depends on another
     * @param dependency The milestone that is depended upon
     * @return The updated milestone
     */
    Milestone setDependency(Milestone milestone, Milestone dependency);

    /**
     * Removes a dependency between two milestones.
     *
     * @param milestone The milestone that depends on another
     * @param dependency The milestone that is depended upon
     * @return The updated milestone
     */
    Milestone removeDependency(Milestone milestone, Milestone dependency);

    /**
     * Gets all upcoming milestones for a project.
     *
     * @param project The project to get milestones for
     * @return List of upcoming milestones
     */
    List<Milestone> getUpcomingMilestones(Project project);

    /**
     * Gets all completed milestones for a project.
     *
     * @param project The project to get milestones for
     * @return List of completed milestones
     */
    List<Milestone> getCompletedMilestones(Project project);

    /**
     * Gets all overdue milestones for a project.
     *
     * @param project The project to get milestones for
     * @return List of overdue milestones
     */
    List<Milestone> getOverdueMilestones(Project project);
}