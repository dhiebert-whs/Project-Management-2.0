// src/main/java/org/frcpm/models/GitHubIntegration.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Integration model for FRC teams.
 * 
 * Provides comprehensive GitHub integration for software teams including
 * repository management, commit tracking, pull request workflows, and
 * development analytics for FRC programming teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-ExternalIntegrations
 * @since Phase 4C.1 GitHub Integration for Code Teams
 */
@Entity
@Table(name = "github_integrations")
public class GitHubIntegration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    @Column(nullable = false, length = 100)
    private String repositoryName;
    
    @Column(nullable = false, length = 200)
    private String repositoryUrl;
    
    @Column(length = 100)
    private String organizationName;
    
    @Column(length = 100)
    private String ownerUsername;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryType repositoryType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationStatus integrationStatus;
    
    // Repository Statistics
    @Column(nullable = false)
    private Integer totalCommits = 0;
    
    @Column(nullable = false)
    private Integer totalPullRequests = 0;
    
    @Column(nullable = false)
    private Integer openIssues = 0;
    
    @Column(nullable = false)
    private Integer closedIssues = 0;
    
    @Column(nullable = false)
    private Integer totalContributors = 0;
    
    @Column(nullable = false)
    private Integer totalBranches = 0;
    
    @Column(nullable = false)
    private Integer totalReleases = 0;
    
    @Column(nullable = false)
    private Long codebaseSize = 0L; // Lines of code
    
    // Development Metrics
    @Column(nullable = false)
    private Double commitFrequency = 0.0; // Commits per day
    
    @Column(nullable = false)
    private Double averagePullRequestSize = 0.0; // Average lines changed
    
    @Column(nullable = false)
    private Double codeReviewCoverage = 0.0; // Percentage of PRs reviewed
    
    @Column(nullable = false)
    private Double testCoverage = 0.0; // Code test coverage percentage
    
    @Column(nullable = false)
    private Integer bugsFound = 0;
    
    @Column(nullable = false)
    private Integer bugsFixed = 0;
    
    // Team Development Metrics
    @Column(nullable = false)
    private Integer activeStudentDevelopers = 0;
    
    @Column(nullable = false)
    private Integer activeMentorDevelopers = 0;
    
    @Column(nullable = false)
    private Double studentContributionPercentage = 0.0;
    
    @Column(nullable = false)
    private Double mentorContributionPercentage = 0.0;
    
    // Repository Configuration
    @Column(length = 100)
    private String defaultBranch = "main";
    
    @Column(nullable = false)
    private Boolean hasCodeReviewRequired = false;
    
    @Column(nullable = false)
    private Boolean hasContinuousIntegration = false;
    
    @Column(nullable = false)
    private Boolean hasAutomatedTesting = false;
    
    @Column(nullable = false)
    private Boolean hasDeploymentPipeline = false;
    
    // Integration Settings
    @Column(length = 500)
    private String webhookUrl;
    
    @Column(length = 1000)
    private String accessToken; // Encrypted
    
    @Column(nullable = false)
    private Boolean isWebhookActive = false;
    
    @Column(nullable = false)
    private Boolean syncCommits = true;
    
    @Column(nullable = false)
    private Boolean syncPullRequests = true;
    
    @Column(nullable = false)
    private Boolean syncIssues = true;
    
    @Column(nullable = false)
    private Boolean syncReleases = true;
    
    // Last Sync Information
    @Column
    private LocalDateTime lastSyncTime;
    
    @Column
    private LocalDateTime lastCommitSync;
    
    @Column
    private LocalDateTime lastPullRequestSync;
    
    @Column
    private LocalDateTime lastIssueSync;
    
    @Column(length = 500)
    private String lastSyncStatus;
    
    @Column(length = 1000)
    private String lastSyncError;
    
    // Project Association
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    
    // Team Members with GitHub Access
    @ElementCollection
    @CollectionTable(name = "github_team_members", joinColumns = @JoinColumn(name = "integration_id"))
    @Embedded
    private List<GitHubTeamMember> teamMembers = new ArrayList<>();
    
    // Administrative Fields
    @Column(length = 1000)
    private String description;
    
    @Column(length = 2000)
    private String integrationNotes;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configured_by_id")
    private TeamMember configuredBy;
    
    /**
     * Types of GitHub repositories
     */
    public enum RepositoryType {
        ROBOT_CODE("Robot Code", "Main robot programming repository"),
        SCOUTING_APP("Scouting App", "Scouting application repository"),
        DASHBOARD("Dashboard", "Driver dashboard repository"),
        SIMULATION("Simulation", "Robot simulation repository"),
        TOOLS("Tools", "Development tools and utilities"),
        WEBSITE("Website", "Team website repository"),
        DOCUMENTATION("Documentation", "Team documentation repository"),
        LIBRARY("Library", "Reusable code library"),
        PROTOTYPE("Prototype", "Prototype and experimental code"),
        ARCHIVE("Archive", "Archived or legacy code"),
        OTHER("Other", "Other repository type");
        
        private final String displayName;
        private final String description;
        
        RepositoryType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Integration status with GitHub
     */
    public enum IntegrationStatus {
        ACTIVE("Active", "Integration is working properly"),
        INACTIVE("Inactive", "Integration is disabled"),
        ERROR("Error", "Integration has errors"),
        PENDING("Pending", "Integration setup in progress"),
        EXPIRED("Expired", "Access token expired"),
        RATE_LIMITED("Rate Limited", "GitHub API rate limit exceeded"),
        UNAUTHORIZED("Unauthorized", "Access denied"),
        REPOSITORY_NOT_FOUND("Repository Not Found", "Repository no longer exists"),
        CONFIGURATION_NEEDED("Configuration Needed", "Additional setup required");
        
        private final String displayName;
        private final String description;
        
        IntegrationStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Embedded class for GitHub team member information
     */
    @Embeddable
    public static class GitHubTeamMember {
        @Column(name = "github_username", length = 50)
        private String githubUsername;
        
        @Column(name = "team_member_id")
        private Long teamMemberId;
        
        @Column(name = "role", length = 20)
        private String role; // admin, write, read
        
        @Column(name = "commits_count")
        private Integer commitsCount = 0;
        
        @Column(name = "pull_requests_count")
        private Integer pullRequestsCount = 0;
        
        @Column(name = "lines_added")
        private Long linesAdded = 0L;
        
        @Column(name = "lines_deleted")
        private Long linesDeleted = 0L;
        
        @Column(name = "last_commit_date")
        private LocalDateTime lastCommitDate;
        
        // Constructors
        public GitHubTeamMember() {}
        
        public GitHubTeamMember(String githubUsername, Long teamMemberId, String role) {
            this.githubUsername = githubUsername;
            this.teamMemberId = teamMemberId;
            this.role = role;
        }
        
        // Getters and Setters
        public String getGithubUsername() { return githubUsername; }
        public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
        
        public Long getTeamMemberId() { return teamMemberId; }
        public void setTeamMemberId(Long teamMemberId) { this.teamMemberId = teamMemberId; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public Integer getCommitsCount() { return commitsCount; }
        public void setCommitsCount(Integer commitsCount) { this.commitsCount = commitsCount; }
        
        public Integer getPullRequestsCount() { return pullRequestsCount; }
        public void setPullRequestsCount(Integer pullRequestsCount) { this.pullRequestsCount = pullRequestsCount; }
        
        public Long getLinesAdded() { return linesAdded; }
        public void setLinesAdded(Long linesAdded) { this.linesAdded = linesAdded; }
        
        public Long getLinesDeleted() { return linesDeleted; }
        public void setLinesDeleted(Long linesDeleted) { this.linesDeleted = linesDeleted; }
        
        public LocalDateTime getLastCommitDate() { return lastCommitDate; }
        public void setLastCommitDate(LocalDateTime lastCommitDate) { this.lastCommitDate = lastCommitDate; }
    }
    
    // Constructors
    public GitHubIntegration() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public GitHubIntegration(Integer teamNumber, Integer season, String repositoryName, 
                           String repositoryUrl, RepositoryType repositoryType) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.repositoryName = repositoryName;
        this.repositoryUrl = repositoryUrl;
        this.repositoryType = repositoryType;
        this.integrationStatus = IntegrationStatus.PENDING;
    }
    
    // Business Methods
    
    /**
     * Checks if integration is healthy and working.
     */
    public boolean isHealthy() {
        return integrationStatus == IntegrationStatus.ACTIVE && 
               lastSyncTime != null && 
               lastSyncTime.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    /**
     * Checks if sync is overdue.
     */
    public boolean isSyncOverdue() {
        return lastSyncTime == null || 
               lastSyncTime.isBefore(LocalDateTime.now().minusHours(6));
    }
    
    /**
     * Calculates development velocity (commits per day over last 30 days).
     */
    public double getDevelopmentVelocity() {
        // This would calculate based on recent commits
        // For now, return the stored commit frequency
        return commitFrequency;
    }
    
    /**
     * Calculates bug fix rate.
     */
    public double getBugFixRate() {
        if (bugsFound == 0) return 0.0;
        return (double) bugsFixed / bugsFound * 100.0;
    }
    
    /**
     * Gets repository health score.
     */
    public double getRepositoryHealthScore() {
        double score = 0.0;
        
        // Code review coverage (25 points)
        score += codeReviewCoverage * 0.25;
        
        // Test coverage (25 points)
        score += testCoverage * 0.25;
        
        // Development activity (20 points)
        score += Math.min(commitFrequency * 5, 20.0);
        
        // Bug fix rate (15 points)
        score += getBugFixRate() * 0.15;
        
        // CI/CD setup (15 points)
        if (hasContinuousIntegration) score += 5.0;
        if (hasAutomatedTesting) score += 5.0;
        if (hasDeploymentPipeline) score += 5.0;
        
        return Math.min(score, 100.0);
    }
    
    /**
     * Checks if repository follows best practices.
     */
    public boolean followsBestPractices() {
        return hasCodeReviewRequired && 
               hasContinuousIntegration && 
               hasAutomatedTesting &&
               codeReviewCoverage >= 80.0;
    }
    
    /**
     * Gets student involvement level.
     */
    public String getStudentInvolvementLevel() {
        if (studentContributionPercentage >= 80.0) return "HIGH";
        if (studentContributionPercentage >= 60.0) return "GOOD";
        if (studentContributionPercentage >= 40.0) return "MODERATE";
        if (studentContributionPercentage >= 20.0) return "LOW";
        return "MINIMAL";
    }
    
    /**
     * Checks if repository is active (commits in last 7 days).
     */
    public boolean isActiveRepository() {
        return lastCommitSync != null && 
               lastCommitSync.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * Gets repository maturity level.
     */
    public String getRepositoryMaturity() {
        if (totalCommits >= 500 && totalContributors >= 5 && followsBestPractices()) {
            return "MATURE";
        }
        if (totalCommits >= 200 && totalContributors >= 3) {
            return "DEVELOPING";
        }
        if (totalCommits >= 50) {
            return "BASIC";
        }
        return "STARTING";
    }
    
    /**
     * Calculates average pull request review time.
     */
    public double getAveragePullRequestReviewTime() {
        // This would be calculated from actual PR data
        // For now, return estimated based on coverage
        if (codeReviewCoverage >= 90.0) return 4.0; // 4 hours
        if (codeReviewCoverage >= 70.0) return 8.0; // 8 hours
        if (codeReviewCoverage >= 50.0) return 24.0; // 1 day
        return 72.0; // 3 days
    }
    
    /**
     * Gets development team performance rating.
     */
    public String getTeamPerformanceRating() {
        double score = 0.0;
        
        // Commit frequency
        score += Math.min(commitFrequency * 10, 25.0);
        
        // Student involvement
        score += studentContributionPercentage * 0.25;
        
        // Code quality
        score += getRepositoryHealthScore() * 0.25;
        
        // Team collaboration
        score += Math.min(totalContributors * 5, 25.0);
        
        if (score >= 80.0) return "EXCELLENT";
        if (score >= 65.0) return "GOOD";
        if (score >= 50.0) return "AVERAGE";
        if (score >= 35.0) return "BELOW_AVERAGE";
        return "POOR";
    }
    
    /**
     * Updates sync timestamp for specific sync type.
     */
    public void updateSyncTimestamp(String syncType) {
        LocalDateTime now = LocalDateTime.now();
        this.lastSyncTime = now;
        
        switch (syncType.toLowerCase()) {
            case "commits":
                this.lastCommitSync = now;
                break;
            case "pullrequests":
                this.lastPullRequestSync = now;
                break;
            case "issues":
                this.lastIssueSync = now;
                break;
        }
        
        // Clear any previous error if sync succeeds
        if (this.integrationStatus == IntegrationStatus.ERROR) {
            this.integrationStatus = IntegrationStatus.ACTIVE;
            this.lastSyncError = null;
        }
    }
    
    /**
     * Records sync error.
     */
    public void recordSyncError(String error) {
        this.integrationStatus = IntegrationStatus.ERROR;
        this.lastSyncError = error;
        this.lastSyncStatus = "ERROR: " + error;
    }
    
    /**
     * Adds a team member to the GitHub integration.
     */
    public void addTeamMember(String githubUsername, Long teamMemberId, String role) {
        GitHubTeamMember member = new GitHubTeamMember(githubUsername, teamMemberId, role);
        this.teamMembers.add(member);
    }
    
    /**
     * Removes a team member from the GitHub integration.
     */
    public void removeTeamMember(String githubUsername) {
        this.teamMembers.removeIf(member -> member.getGithubUsername().equals(githubUsername));
    }
    
    /**
     * Updates team member statistics.
     */
    public void updateTeamMemberStats(String githubUsername, int commits, int pullRequests, 
                                     long linesAdded, long linesDeleted) {
        this.teamMembers.stream()
                .filter(member -> member.getGithubUsername().equals(githubUsername))
                .findFirst()
                .ifPresent(member -> {
                    member.setCommitsCount(commits);
                    member.setPullRequestsCount(pullRequests);
                    member.setLinesAdded(linesAdded);
                    member.setLinesDeleted(linesDeleted);
                    member.setLastCommitDate(LocalDateTime.now());
                });
    }
    
    /**
     * Calculates contribution percentages for students vs mentors.
     */
    public void calculateContributionPercentages() {
        int totalStudentCommits = 0;
        int totalMentorCommits = 0;
        int totalCommitsFromMembers = 0;
        
        for (GitHubTeamMember member : teamMembers) {
            totalCommitsFromMembers += member.getCommitsCount();
            // This would need to check if team member is student or mentor
            // For now, assume based on role
            if ("write".equals(member.getRole()) || "read".equals(member.getRole())) {
                totalStudentCommits += member.getCommitsCount();
            } else {
                totalMentorCommits += member.getCommitsCount();
            }
        }
        
        if (totalCommitsFromMembers > 0) {
            this.studentContributionPercentage = (double) totalStudentCommits / totalCommitsFromMembers * 100.0;
            this.mentorContributionPercentage = (double) totalMentorCommits / totalCommitsFromMembers * 100.0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }
    
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    
    public RepositoryType getRepositoryType() { return repositoryType; }
    public void setRepositoryType(RepositoryType repositoryType) { this.repositoryType = repositoryType; }
    
    public IntegrationStatus getIntegrationStatus() { return integrationStatus; }
    public void setIntegrationStatus(IntegrationStatus integrationStatus) { this.integrationStatus = integrationStatus; }
    
    public Integer getTotalCommits() { return totalCommits; }
    public void setTotalCommits(Integer totalCommits) { this.totalCommits = totalCommits; }
    
    public Integer getTotalPullRequests() { return totalPullRequests; }
    public void setTotalPullRequests(Integer totalPullRequests) { this.totalPullRequests = totalPullRequests; }
    
    public Integer getOpenIssues() { return openIssues; }
    public void setOpenIssues(Integer openIssues) { this.openIssues = openIssues; }
    
    public Integer getClosedIssues() { return closedIssues; }
    public void setClosedIssues(Integer closedIssues) { this.closedIssues = closedIssues; }
    
    public Integer getTotalContributors() { return totalContributors; }
    public void setTotalContributors(Integer totalContributors) { this.totalContributors = totalContributors; }
    
    public Integer getTotalBranches() { return totalBranches; }
    public void setTotalBranches(Integer totalBranches) { this.totalBranches = totalBranches; }
    
    public Integer getTotalReleases() { return totalReleases; }
    public void setTotalReleases(Integer totalReleases) { this.totalReleases = totalReleases; }
    
    public Long getCodebaseSize() { return codebaseSize; }
    public void setCodebaseSize(Long codebaseSize) { this.codebaseSize = codebaseSize; }
    
    public Double getCommitFrequency() { return commitFrequency; }
    public void setCommitFrequency(Double commitFrequency) { this.commitFrequency = commitFrequency; }
    
    public Double getAveragePullRequestSize() { return averagePullRequestSize; }
    public void setAveragePullRequestSize(Double averagePullRequestSize) { this.averagePullRequestSize = averagePullRequestSize; }
    
    public Double getCodeReviewCoverage() { return codeReviewCoverage; }
    public void setCodeReviewCoverage(Double codeReviewCoverage) { this.codeReviewCoverage = codeReviewCoverage; }
    
    public Double getTestCoverage() { return testCoverage; }
    public void setTestCoverage(Double testCoverage) { this.testCoverage = testCoverage; }
    
    public Integer getBugsFound() { return bugsFound; }
    public void setBugsFound(Integer bugsFound) { this.bugsFound = bugsFound; }
    
    public Integer getBugsFixed() { return bugsFixed; }
    public void setBugsFixed(Integer bugsFixed) { this.bugsFixed = bugsFixed; }
    
    public Integer getActiveStudentDevelopers() { return activeStudentDevelopers; }
    public void setActiveStudentDevelopers(Integer activeStudentDevelopers) { this.activeStudentDevelopers = activeStudentDevelopers; }
    
    public Integer getActiveMentorDevelopers() { return activeMentorDevelopers; }
    public void setActiveMentorDevelopers(Integer activeMentorDevelopers) { this.activeMentorDevelopers = activeMentorDevelopers; }
    
    public Double getStudentContributionPercentage() { return studentContributionPercentage; }
    public void setStudentContributionPercentage(Double studentContributionPercentage) { this.studentContributionPercentage = studentContributionPercentage; }
    
    public Double getMentorContributionPercentage() { return mentorContributionPercentage; }
    public void setMentorContributionPercentage(Double mentorContributionPercentage) { this.mentorContributionPercentage = mentorContributionPercentage; }
    
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    
    public Boolean getHasCodeReviewRequired() { return hasCodeReviewRequired; }
    public void setHasCodeReviewRequired(Boolean hasCodeReviewRequired) { this.hasCodeReviewRequired = hasCodeReviewRequired; }
    
    public Boolean getHasContinuousIntegration() { return hasContinuousIntegration; }
    public void setHasContinuousIntegration(Boolean hasContinuousIntegration) { this.hasContinuousIntegration = hasContinuousIntegration; }
    
    public Boolean getHasAutomatedTesting() { return hasAutomatedTesting; }
    public void setHasAutomatedTesting(Boolean hasAutomatedTesting) { this.hasAutomatedTesting = hasAutomatedTesting; }
    
    public Boolean getHasDeploymentPipeline() { return hasDeploymentPipeline; }
    public void setHasDeploymentPipeline(Boolean hasDeploymentPipeline) { this.hasDeploymentPipeline = hasDeploymentPipeline; }
    
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public Boolean getIsWebhookActive() { return isWebhookActive; }
    public void setIsWebhookActive(Boolean isWebhookActive) { this.isWebhookActive = isWebhookActive; }
    
    public Boolean getSyncCommits() { return syncCommits; }
    public void setSyncCommits(Boolean syncCommits) { this.syncCommits = syncCommits; }
    
    public Boolean getSyncPullRequests() { return syncPullRequests; }
    public void setSyncPullRequests(Boolean syncPullRequests) { this.syncPullRequests = syncPullRequests; }
    
    public Boolean getSyncIssues() { return syncIssues; }
    public void setSyncIssues(Boolean syncIssues) { this.syncIssues = syncIssues; }
    
    public Boolean getSyncReleases() { return syncReleases; }
    public void setSyncReleases(Boolean syncReleases) { this.syncReleases = syncReleases; }
    
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    
    public LocalDateTime getLastCommitSync() { return lastCommitSync; }
    public void setLastCommitSync(LocalDateTime lastCommitSync) { this.lastCommitSync = lastCommitSync; }
    
    public LocalDateTime getLastPullRequestSync() { return lastPullRequestSync; }
    public void setLastPullRequestSync(LocalDateTime lastPullRequestSync) { this.lastPullRequestSync = lastPullRequestSync; }
    
    public LocalDateTime getLastIssueSync() { return lastIssueSync; }
    public void setLastIssueSync(LocalDateTime lastIssueSync) { this.lastIssueSync = lastIssueSync; }
    
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    
    public String getLastSyncError() { return lastSyncError; }
    public void setLastSyncError(String lastSyncError) { this.lastSyncError = lastSyncError; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public List<GitHubTeamMember> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<GitHubTeamMember> teamMembers) { this.teamMembers = teamMembers; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIntegrationNotes() { return integrationNotes; }
    public void setIntegrationNotes(String integrationNotes) { this.integrationNotes = integrationNotes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getConfiguredBy() { return configuredBy; }
    public void setConfiguredBy(TeamMember configuredBy) { this.configuredBy = configuredBy; }
    
    @Override
    public String toString() {
        return String.format("GitHubIntegration{id=%d, team=%d, repo='%s', type=%s, status=%s}", 
                           id, teamNumber, repositoryName, repositoryType, integrationStatus);
    }
}