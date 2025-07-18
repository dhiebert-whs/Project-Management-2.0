// src/main/java/org/frcpm/models/TaskTemplate.java
// Phase 4A: Task Template for Robot Subsystem Projects

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a reusable task template within project templates.
 * 
 * Task templates define standard tasks that are typically required
 * for specific robot subsystems, including dependencies, estimated
 * effort, and required skills.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
@Entity
@Table(name = "task_templates")
public class TaskTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_template_id", nullable = false)
    private ProjectTemplate projectTemplate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Task.Priority priority = Task.Priority.MEDIUM;
    
    @Min(0)
    @Max(100)
    @Column(name = "estimated_hours", nullable = false)
    private double estimatedHours;
    
    @Min(0)
    @Max(20)
    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_category", nullable = false)
    private TaskCategory taskCategory = TaskCategory.DESIGN;
    
    @ElementCollection
    @CollectionTable(name = "task_template_skills", joinColumns = @JoinColumn(name = "task_template_id"))
    @Column(name = "required_skill")
    @Enumerated(EnumType.STRING)
    private List<RequiredSkill> requiredSkills = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "task_template_dependencies", joinColumns = @JoinColumn(name = "task_template_id"))
    @Column(name = "dependency_title")
    private List<String> dependencyTitles = new ArrayList<>();
    
    @Column(name = "is_milestone", nullable = false)
    private boolean isMilestone = false;
    
    @Column(name = "is_critical_path", nullable = false)
    private boolean isCriticalPath = false;
    
    @Column(name = "can_be_parallel", nullable = false)
    private boolean canBeParallel = true;
    
    @Size(max = 500)
    @Column(name = "acceptance_criteria", length = 500)
    private String acceptanceCriteria;
    
    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;
    
    // Safety and compliance
    @Column(name = "requires_mentor_approval", nullable = false)
    private boolean requiresMentorApproval = false;
    
    @Column(name = "safety_critical", nullable = false)
    private boolean safetyCritical = false;
    
    @Size(max = 200)
    @Column(name = "safety_notes", length = 200)
    private String safetyNotes;
    
    // Build season context
    @Enumerated(EnumType.STRING)
    @Column(name = "build_phase")
    private BuildPhase buildPhase = BuildPhase.DESIGN;
    
    @Column(name = "week_offset", nullable = false)
    private int weekOffset = 0; // Weeks from project start
    
    // Constructors
    
    public TaskTemplate() {
        // Default constructor required by JPA
    }
    
    public TaskTemplate(String title, ProjectTemplate projectTemplate, TaskCategory taskCategory) {
        this.title = title;
        this.projectTemplate = projectTemplate;
        this.taskCategory = taskCategory;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ProjectTemplate getProjectTemplate() {
        return projectTemplate;
    }
    
    public void setProjectTemplate(ProjectTemplate projectTemplate) {
        this.projectTemplate = projectTemplate;
    }
    
    public Task.Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Task.Priority priority) {
        this.priority = priority;
    }
    
    public double getEstimatedHours() {
        return estimatedHours;
    }
    
    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
    
    public int getSequenceOrder() {
        return sequenceOrder;
    }
    
    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
    
    public TaskCategory getTaskCategory() {
        return taskCategory;
    }
    
    public void setTaskCategory(TaskCategory taskCategory) {
        this.taskCategory = taskCategory;
    }
    
    public List<RequiredSkill> getRequiredSkills() {
        return requiredSkills;
    }
    
    public void setRequiredSkills(List<RequiredSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
    
    public List<String> getDependencyTitles() {
        return dependencyTitles;
    }
    
    public void setDependencyTitles(List<String> dependencyTitles) {
        this.dependencyTitles = dependencyTitles;
    }
    
    public boolean isMilestone() {
        return isMilestone;
    }
    
    public void setMilestone(boolean milestone) {
        isMilestone = milestone;
    }
    
    public boolean isCriticalPath() {
        return isCriticalPath;
    }
    
    public void setCriticalPath(boolean criticalPath) {
        isCriticalPath = criticalPath;
    }
    
    public boolean canBeParallel() {
        return canBeParallel;
    }
    
    public void setCanBeParallel(boolean canBeParallel) {
        this.canBeParallel = canBeParallel;
    }
    
    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }
    
    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isRequiresMentorApproval() {
        return requiresMentorApproval;
    }
    
    public void setRequiresMentorApproval(boolean requiresMentorApproval) {
        this.requiresMentorApproval = requiresMentorApproval;
    }
    
    public boolean isSafetyCritical() {
        return safetyCritical;
    }
    
    public void setSafetyCritical(boolean safetyCritical) {
        this.safetyCritical = safetyCritical;
    }
    
    public String getSafetyNotes() {
        return safetyNotes;
    }
    
    public void setSafetyNotes(String safetyNotes) {
        this.safetyNotes = safetyNotes;
    }
    
    public BuildPhase getBuildPhase() {
        return buildPhase;
    }
    
    public void setBuildPhase(BuildPhase buildPhase) {
        this.buildPhase = buildPhase;
    }
    
    public int getWeekOffset() {
        return weekOffset;
    }
    
    public void setWeekOffset(int weekOffset) {
        this.weekOffset = weekOffset;
    }
    
    // Helper methods
    
    /**
     * Add a required skill for this task.
     */
    public void addRequiredSkill(RequiredSkill skill) {
        if (!requiredSkills.contains(skill)) {
            requiredSkills.add(skill);
        }
    }
    
    /**
     * Remove a required skill from this task.
     */
    public void removeRequiredSkill(RequiredSkill skill) {
        requiredSkills.remove(skill);
    }
    
    /**
     * Add a dependency title.
     */
    public void addDependency(String dependencyTitle) {
        if (!dependencyTitles.contains(dependencyTitle)) {
            dependencyTitles.add(dependencyTitle);
        }
    }
    
    /**
     * Remove a dependency title.
     */
    public void removeDependency(String dependencyTitle) {
        dependencyTitles.remove(dependencyTitle);
    }
    
    /**
     * Check if this task has dependencies.
     */
    public boolean hasDependencies() {
        return dependencyTitles != null && !dependencyTitles.isEmpty();
    }
    
    /**
     * Check if this task requires specific skills.
     */
    public boolean requiresSkills() {
        return requiredSkills != null && !requiredSkills.isEmpty();
    }
    
    /**
     * Get formatted skill requirements for display.
     */
    public String getSkillsDisplay() {
        if (requiredSkills.isEmpty()) {
            return "No specific skills required";
        }
        return String.join(", ", requiredSkills.stream()
            .map(RequiredSkill::getDisplayName)
            .toArray(String[]::new));
    }
    
    @Override
    public String toString() {
        return title + " (" + taskCategory.getDisplayName() + ")";
    }
    
    /**
     * Task categories for organization and filtering.
     */
    public enum TaskCategory {
        DESIGN("Design", "Initial design and planning", "📐"),
        CAD("CAD", "Computer-aided design work", "💻"),
        PROTOTYPE("Prototype", "Rapid prototyping and testing", "🔧"),
        FABRICATION("Fabrication", "Manufacturing and machining", "⚙️"),
        ASSEMBLY("Assembly", "Component assembly and integration", "🔩"),
        WIRING("Wiring", "Electrical wiring and connections", "⚡"),
        PROGRAMMING("Programming", "Software development and coding", "💻"),
        TESTING("Testing", "System testing and validation", "🧪"),
        INTEGRATION("Integration", "System integration with robot", "🔗"),
        DOCUMENTATION("Documentation", "Documentation and procedures", "📝");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        TaskCategory(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public String getDisplayWithIcon() {
            return icon + " " + displayName;
        }
    }
    
    /**
     * Required skills for tasks.
     */
    public enum RequiredSkill {
        CAD_MODELING("CAD Modeling", "SolidWorks, Fusion 360, or similar"),
        MACHINING("Machining", "Mill, lathe, or CNC operation"),
        WELDING("Welding", "MIG/TIG welding certification"),
        ELECTRONICS("Electronics", "Circuit design and troubleshooting"),
        PROGRAMMING("Programming", "Java, C++, or Python experience"),
        PNEUMATICS("Pneumatics", "Air system design and assembly"),
        MECHANICAL_ASSEMBLY("Mechanical Assembly", "Mechanical fastening and assembly"),
        ELECTRICAL_WIRING("Electrical Wiring", "Robot wiring and connections"),
        SAFETY_TRAINING("Safety Training", "Shop safety certification"),
        PROJECT_MANAGEMENT("Project Management", "Planning and coordination skills");
        
        private final String displayName;
        private final String description;
        
        RequiredSkill(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Build season phases for timing.
     */
    public enum BuildPhase {
        KICKOFF("Kickoff", "Game analysis and strategy", 1),
        DESIGN("Design", "Concept design and planning", 2),
        PROTOTYPE("Prototype", "Rapid prototyping phase", 3),
        BUILD("Build", "Main construction phase", 4),
        INTEGRATION("Integration", "System integration and assembly", 5),
        TESTING("Testing", "Testing and refinement", 6),
        COMPETITION_PREP("Competition Prep", "Final preparation for competition", 7);
        
        private final String displayName;
        private final String description;
        private final int weekNumber;
        
        BuildPhase(String displayName, String description, int weekNumber) {
            this.displayName = displayName;
            this.description = description;
            this.weekNumber = weekNumber;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getWeekNumber() {
            return weekNumber;
        }
    }
}