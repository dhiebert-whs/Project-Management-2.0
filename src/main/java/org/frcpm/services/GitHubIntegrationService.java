// src/main/java/org/frcpm/services/GitHubIntegrationService.java

package org.frcpm.services;

import org.frcpm.models.GitHubIntegration;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for GitHubIntegration operations.
 * 
 * Provides comprehensive GitHub integration services including repository
 * management, development analytics, team performance tracking, and external
 * integration monitoring for FRC programming teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-ExternalIntegrations
 * @since Phase 4C.1 GitHub Integration for Code Teams
 */
public interface GitHubIntegrationService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new GitHub integration.
     */
    GitHubIntegration create(GitHubIntegration integration);

    /**
     * Updates an existing GitHub integration.
     */
    GitHubIntegration update(Long id, GitHubIntegration integration);

    /**
     * Deletes a GitHub integration (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a GitHub integration by ID.
     */
    Optional<GitHubIntegration> findById(Long id);

    /**
     * Finds all active GitHub integrations.
     */
    List<GitHubIntegration> findAll();

    /**
     * Checks if GitHub integration exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of GitHub integrations.
     */
    long count();

    // =========================================================================
    // INTEGRATION MANAGEMENT
    // =========================================================================

    /**
     * Creates a new GitHub integration with validation.
     */
    GitHubIntegration createIntegration(GitHubIntegration integration);

    /**
     * Creates a GitHub integration with basic parameters.
     */
    GitHubIntegration createIntegration(Integer teamNumber, Integer season, String repositoryName,
                                       String repositoryUrl, GitHubIntegration.RepositoryType repositoryType,
                                       TeamMember configuredBy);

    /**
     * Creates a GitHub integration with project association.
     */
    GitHubIntegration createIntegrationWithProject(Integer teamNumber, Integer season, String repositoryName,
                                                  String repositoryUrl, GitHubIntegration.RepositoryType repositoryType,
                                                  Project project, TeamMember configuredBy);

    /**
     * Updates an existing GitHub integration with validation.
     */
    GitHubIntegration updateIntegration(Long integrationId, GitHubIntegration integration);

    /**
     * Archives a GitHub integration.
     */
    void archiveIntegration(Long integrationId, String reason);

    /**
     * Restores an archived GitHub integration.
     */
    GitHubIntegration restoreIntegration(Long integrationId);

    /**
     * Finds all active integrations for a team and season.
     */
    List<GitHubIntegration> findActiveIntegrations(Integer teamNumber, Integer season);

    /**
     * Finds integrations by repository type.
     */
    List<GitHubIntegration> findByRepositoryType(Integer teamNumber, Integer season,
                                                GitHubIntegration.RepositoryType repositoryType);

    /**
     * Finds integration by repository name.
     */
    Optional<GitHubIntegration> findByRepositoryName(Integer teamNumber, Integer season, String repositoryName);

    /**
     * Finds integration by repository URL.
     */
    Optional<GitHubIntegration> findByRepositoryUrl(String repositoryUrl);

    // =========================================================================
    // REPOSITORY CONFIGURATION MANAGEMENT
    // =========================================================================

    /**
     * Configures webhook for GitHub integration.
     */
    GitHubIntegration configureWebhook(Long integrationId, String webhookUrl);

    /**
     * Activates webhook for GitHub integration.
     */
    GitHubIntegration activateWebhook(Long integrationId);

    /**
     * Deactivates webhook for GitHub integration.
     */
    GitHubIntegration deactivateWebhook(Long integrationId);

    /**
     * Updates access token for GitHub integration.
     */
    GitHubIntegration updateAccessToken(Long integrationId, String accessToken);

    /**
     * Configures sync settings for GitHub integration.
     */
    GitHubIntegration configureSyncSettings(Long integrationId, boolean syncCommits, boolean syncPullRequests,
                                           boolean syncIssues, boolean syncReleases);

    /**
     * Updates repository configuration flags.
     */
    GitHubIntegration updateRepositoryConfiguration(Long integrationId, boolean hasCodeReviewRequired,
                                                   boolean hasContinuousIntegration, boolean hasAutomatedTesting,
                                                   boolean hasDeploymentPipeline);

    /**
     * Sets default branch for repository.
     */
    GitHubIntegration setDefaultBranch(Long integrationId, String defaultBranch);

    /**
     * Associates integration with project.
     */
    GitHubIntegration associateWithProject(Long integrationId, Project project);

    /**
     * Removes project association.
     */
    GitHubIntegration removeProjectAssociation(Long integrationId);

    // =========================================================================
    // TEAM MEMBER MANAGEMENT
    // =========================================================================

    /**
     * Adds team member to GitHub integration.
     */
    GitHubIntegration addTeamMember(Long integrationId, String githubUsername, Long teamMemberId, String role);

    /**
     * Removes team member from GitHub integration.
     */
    GitHubIntegration removeTeamMember(Long integrationId, String githubUsername);

    /**
     * Updates team member role in GitHub integration.
     */
    GitHubIntegration updateTeamMemberRole(Long integrationId, String githubUsername, String role);

    /**
     * Updates team member statistics.
     */
    GitHubIntegration updateTeamMemberStatistics(Long integrationId, String githubUsername, int commits,
                                                int pullRequests, long linesAdded, long linesDeleted);

    /**
     * Calculates and updates contribution percentages.
     */
    GitHubIntegration updateContributionPercentages(Long integrationId);

    /**
     * Syncs team member data from GitHub.
     */
    GitHubIntegration syncTeamMemberData(Long integrationId);

    // =========================================================================
    // SYNC AND DATA REFRESH OPERATIONS
    // =========================================================================

    /**
     * Performs full sync with GitHub repository.
     */
    GitHubIntegration performFullSync(Long integrationId);

    /**
     * Syncs commits from GitHub repository.
     */
    GitHubIntegration syncCommits(Long integrationId);

    /**
     * Syncs pull requests from GitHub repository.
     */
    GitHubIntegration syncPullRequests(Long integrationId);

    /**
     * Syncs issues from GitHub repository.
     */
    GitHubIntegration syncIssues(Long integrationId);

    /**
     * Syncs releases from GitHub repository.
     */
    GitHubIntegration syncReleases(Long integrationId);

    /**
     * Updates repository statistics from GitHub.
     */
    GitHubIntegration updateRepositoryStatistics(Long integrationId);

    /**
     * Updates development metrics from GitHub.
     */
    GitHubIntegration updateDevelopmentMetrics(Long integrationId);

    /**
     * Records sync error for integration.
     */
    GitHubIntegration recordSyncError(Long integrationId, String error);

    /**
     * Clears sync error for integration.
     */
    GitHubIntegration clearSyncError(Long integrationId);

    /**
     * Checks and updates integration health status.
     */
    GitHubIntegration updateHealthStatus(Long integrationId);

    // =========================================================================
    // STATUS AND HEALTH MONITORING
    // =========================================================================

    /**
     * Finds active and healthy integrations.
     */
    List<GitHubIntegration> findActiveAndHealthyIntegrations(Integer teamNumber, Integer season);

    /**
     * Finds integrations with sync errors.
     */
    List<GitHubIntegration> findIntegrationsWithErrors(Integer teamNumber, Integer season);

    /**
     * Finds integrations requiring attention.
     */
    List<GitHubIntegration> findIntegrationsRequiringAttention(Integer teamNumber, Integer season);

    /**
     * Finds integrations with overdue sync.
     */
    List<GitHubIntegration> findOverdueSyncIntegrations(Integer teamNumber, Integer season);

    /**
     * Finds integrations pending configuration.
     */
    List<GitHubIntegration> findPendingConfigurationIntegrations(Integer teamNumber, Integer season);

    /**
     * Finds integrations with webhook issues.
     */
    List<GitHubIntegration> findIntegrationsWithWebhookIssues(Integer teamNumber, Integer season);

    /**
     * Finds integrations with access token issues.
     */
    List<GitHubIntegration> findWithAccessTokenIssues(Integer teamNumber, Integer season);

    /**
     * Updates integration status.
     */
    GitHubIntegration updateIntegrationStatus(Long integrationId, GitHubIntegration.IntegrationStatus status);

    /**
     * Validates integration configuration.
     */
    List<String> validateIntegrationConfiguration(Long integrationId);

    /**
     * Tests integration connectivity.
     */
    boolean testIntegrationConnectivity(Long integrationId);

    // =========================================================================
    // DEVELOPMENT ACTIVITY TRACKING
    // =========================================================================

    /**
     * Finds most active repositories by commit frequency.
     */
    List<GitHubIntegration> findMostActiveRepositories(Integer teamNumber, Integer season, Double minFrequency);

    /**
     * Finds repositories with recent development activity.
     */
    List<GitHubIntegration> findRecentlyActiveRepositories(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds inactive repositories.
     */
    List<GitHubIntegration> findInactiveRepositories(Integer teamNumber, Integer season, Integer days);

    /**
     * Finds high velocity repositories.
     */
    List<GitHubIntegration> findHighVelocityRepositories(Integer teamNumber, Integer season,
                                                        Integer minCommits, Double minFrequency);

    /**
     * Finds repositories by contributor range.
     */
    List<GitHubIntegration> findByContributorRange(Integer teamNumber, Integer season,
                                                  Integer minContributors, Integer maxContributors);

    /**
     * Updates commit frequency for integration.
     */
    GitHubIntegration updateCommitFrequency(Long integrationId, Double frequency);

    /**
     * Updates average pull request size.
     */
    GitHubIntegration updateAveragePullRequestSize(Long integrationId, Double averageSize);

    /**
     * Calculates development velocity for integration.
     */
    GitHubIntegration calculateDevelopmentVelocity(Long integrationId);

    // =========================================================================
    // CODE QUALITY AND HEALTH MANAGEMENT
    // =========================================================================

    /**
     * Finds high quality repositories.
     */
    List<GitHubIntegration> findHighQualityRepositories(Integer teamNumber, Integer season,
                                                       Double minCoverage, Double minTestCoverage);

    /**
     * Finds low quality repositories requiring improvement.
     */
    List<GitHubIntegration> findLowQualityRepositories(Integer teamNumber, Integer season,
                                                      Double maxCoverage, Double maxTestCoverage);

    /**
     * Finds repositories with best practices implementation.
     */
    List<GitHubIntegration> findRepositoriesWithBestPractices(Integer teamNumber, Integer season);

    /**
     * Finds repositories missing CI/CD setup.
     */
    List<GitHubIntegration> findRepositoriesMissingCICD(Integer teamNumber, Integer season);

    /**
     * Finds high bug fix rate repositories.
     */
    List<GitHubIntegration> findHighBugFixRateRepositories(Integer teamNumber, Integer season, Double minFixRate);

    /**
     * Updates code review coverage for integration.
     */
    GitHubIntegration updateCodeReviewCoverage(Long integrationId, Double coverage);

    /**
     * Updates test coverage for integration.
     */
    GitHubIntegration updateTestCoverage(Long integrationId, Double coverage);

    /**
     * Updates bug statistics for integration.
     */
    GitHubIntegration updateBugStatistics(Long integrationId, Integer bugsFound, Integer bugsFixed);

    /**
     * Calculates repository health score.
     */
    GitHubIntegration calculateRepositoryHealthScore(Long integrationId);

    /**
     * Analyzes code quality trends.
     */
    Map<String, Object> analyzeCodeQualityTrends(Long integrationId, Integer days);

    // =========================================================================
    // TEAM PERFORMANCE ANALYSIS
    // =========================================================================

    /**
     * Finds repositories with high student involvement.
     */
    List<GitHubIntegration> findHighStudentInvolvementRepositories(Integer teamNumber, Integer season,
                                                                  Double minStudentPercentage);

    /**
     * Finds mentor-heavy repositories.
     */
    List<GitHubIntegration> findMentorHeavyRepositories(Integer teamNumber, Integer season,
                                                       Double minMentorPercentage);

    /**
     * Finds repositories with balanced team contribution.
     */
    List<GitHubIntegration> findBalancedContributionRepositories(Integer teamNumber, Integer season,
                                                               Double minStudent, Double maxStudent,
                                                               Double minMentor, Double maxMentor);

    /**
     * Finds repositories by active developer count.
     */
    List<GitHubIntegration> findByActiveDeveloperCount(Integer teamNumber, Integer season, Integer minDevelopers);

    /**
     * Updates active developer counts.
     */
    GitHubIntegration updateActiveDeveloperCounts(Long integrationId, Integer studentDevelopers,
                                                 Integer mentorDevelopers);

    /**
     * Analyzes team performance trends.
     */
    Map<String, Object> analyzeTeamPerformanceTrends(Integer teamNumber, Integer season, Integer days);

    /**
     * Generates team collaboration report.
     */
    Map<String, Object> generateTeamCollaborationReport(Integer teamNumber, Integer season);

    /**
     * Calculates team performance score.
     */
    Double calculateTeamPerformanceScore(Integer teamNumber, Integer season);

    // =========================================================================
    // REPOSITORY SIZE AND COMPLEXITY ANALYSIS
    // =========================================================================

    /**
     * Finds repositories by codebase size range.
     */
    List<GitHubIntegration> findByCodebaseSizeRange(Integer teamNumber, Integer season,
                                                   Long minSize, Long maxSize);

    /**
     * Finds largest repositories.
     */
    List<GitHubIntegration> findLargestRepositories(Integer teamNumber, Integer season, Long minSize);

    /**
     * Finds repositories with many pull requests.
     */
    List<GitHubIntegration> findHighPullRequestRepositories(Integer teamNumber, Integer season, Integer minPRs);

    /**
     * Finds repositories with many releases.
     */
    List<GitHubIntegration> findActiveReleaseRepositories(Integer teamNumber, Integer season, Integer minReleases);

    /**
     * Finds repositories with high branch activity.
     */
    List<GitHubIntegration> findHighBranchActivityRepositories(Integer teamNumber, Integer season,
                                                              Integer minBranches);

    /**
     * Updates codebase size for integration.
     */
    GitHubIntegration updateCodebaseSize(Long integrationId, Long codebaseSize);

    /**
     * Updates repository statistics.
     */
    GitHubIntegration updateRepositoryStatistics(Long integrationId, Integer totalCommits, Integer totalPullRequests,
                                                Integer openIssues, Integer closedIssues, Integer totalContributors,
                                                Integer totalBranches, Integer totalReleases);

    /**
     * Analyzes repository complexity.
     */
    Map<String, Object> analyzeRepositoryComplexity(Long integrationId);

    // =========================================================================
    // ISSUE AND PROJECT MANAGEMENT
    // =========================================================================

    /**
     * Finds repositories with many open issues.
     */
    List<GitHubIntegration> findRepositoriesWithManyOpenIssues(Integer teamNumber, Integer season,
                                                              Integer minOpenIssues);

    /**
     * Finds repositories with good issue resolution rates.
     */
    List<GitHubIntegration> findGoodIssueResolutionRepositories(Integer teamNumber, Integer season,
                                                               Double minResolutionRate);

    /**
     * Finds repositories with issue backlog problems.
     */
    List<GitHubIntegration> findRepositoriesWithIssueBacklog(Integer teamNumber, Integer season,
                                                            Integer issueThreshold);

    /**
     * Updates issue statistics for integration.
     */
    GitHubIntegration updateIssueStatistics(Long integrationId, Integer openIssues, Integer closedIssues);

    /**
     * Analyzes issue management effectiveness.
     */
    Map<String, Object> analyzeIssueManagementEffectiveness(Long integrationId);

    /**
     * Generates issue resolution report.
     */
    Map<String, Object> generateIssueResolutionReport(Integer teamNumber, Integer season);

    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================

    /**
     * Counts integrations by repository type.
     */
    Map<GitHubIntegration.RepositoryType, Long> countByRepositoryType(Integer teamNumber, Integer season);

    /**
     * Counts integrations by integration status.
     */
    Map<GitHubIntegration.IntegrationStatus, Long> countByIntegrationStatus(Integer teamNumber, Integer season);

    /**
     * Calculates total commits across all repositories.
     */
    Long calculateTotalCommits(Integer teamNumber, Integer season);

    /**
     * Calculates total codebase size across all repositories.
     */
    Long calculateTotalCodebaseSize(Integer teamNumber, Integer season);

    /**
     * Calculates average code review coverage.
     */
    Double calculateAverageCodeReviewCoverage(Integer teamNumber, Integer season);

    /**
     * Calculates average test coverage.
     */
    Double calculateAverageTestCoverage(Integer teamNumber, Integer season);

    /**
     * Calculates average commit frequency.
     */
    Double calculateAverageCommitFrequency(Integer teamNumber, Integer season);

    /**
     * Finds most productive repositories.
     */
    List<GitHubIntegration> findMostProductiveRepositories(Integer teamNumber, Integer season);

    /**
     * Generates comprehensive GitHub analytics report.
     */
    Map<String, Object> generateGitHubAnalyticsReport(Integer teamNumber, Integer season);

    /**
     * Generates GitHub dashboard data.
     */
    Map<String, Object> generateGitHubDashboardData(Integer teamNumber, Integer season);

    /**
     * Generates development velocity report.
     */
    Map<String, Object> generateDevelopmentVelocityReport(Integer teamNumber, Integer season);

    /**
     * Generates code quality assessment report.
     */
    Map<String, Object> generateCodeQualityAssessmentReport(Integer teamNumber, Integer season);

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches integrations by repository name.
     */
    List<GitHubIntegration> searchByRepositoryName(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Searches integrations by description.
     */
    List<GitHubIntegration> searchByDescription(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Full text search across integrations.
     */
    List<GitHubIntegration> fullTextSearch(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Advanced search with multiple criteria.
     */
    List<GitHubIntegration> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria);

    /**
     * Finds integrations by organization.
     */
    List<GitHubIntegration> findByOrganization(String organizationName);

    /**
     * Finds integrations configured by team member.
     */
    List<GitHubIntegration> findByConfiguredBy(Integer teamNumber, Integer season, Long teamMemberId);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds integrations across multiple seasons.
     */
    List<GitHubIntegration> findMultiSeasonIntegrations(Integer teamNumber, List<Integer> seasons);

    /**
     * Finds repository evolution across seasons.
     */
    List<GitHubIntegration> findRepositoryEvolution(Integer teamNumber, String repositoryName);

    /**
     * Compares GitHub activity across seasons.
     */
    Map<String, Object> compareGitHubActivityAcrossSeasons(Integer teamNumber, List<Integer> seasons);

    /**
     * Analyzes team development evolution.
     */
    Map<String, Object> analyzeTeamDevelopmentEvolution(Integer teamNumber, List<Integer> seasons);

    /**
     * Generates multi-season comparison report.
     */
    Map<String, Object> generateMultiSeasonComparisonReport(Integer teamNumber, List<Integer> seasons);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active integrations.
     */
    Long countActiveIntegrations(Integer teamNumber, Integer season);

    /**
     * Finds all active integrations for export.
     */
    List<GitHubIntegration> findAllActiveIntegrations(Integer teamNumber, Integer season);

    /**
     * Creates multiple GitHub integrations.
     */
    List<GitHubIntegration> createBulkIntegrations(List<GitHubIntegration> integrations);

    /**
     * Updates multiple GitHub integrations.
     */
    List<GitHubIntegration> updateBulkIntegrations(Map<Long, GitHubIntegration> integrationUpdates);

    /**
     * Archives multiple GitHub integrations.
     */
    void bulkArchiveIntegrations(List<Long> integrationIds, String reason);

    /**
     * Performs bulk sync for all team integrations.
     */
    void bulkSyncAllIntegrations(Integer teamNumber, Integer season);

    /**
     * Updates all integration health statuses.
     */
    void updateAllIntegrationHealthStatuses(Integer teamNumber, Integer season);

    /**
     * Performs bulk repository statistics update.
     */
    void bulkUpdateRepositoryStatistics(Integer teamNumber, Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived integrations.
     */
    List<GitHubIntegration> findArchivedIntegrations(Integer teamNumber, Integer season);

    /**
     * Permanently deletes integration.
     */
    void permanentlyDeleteIntegration(Long integrationId);

    /**
     * Finds integrations created within date range.
     */
    List<GitHubIntegration> findCreatedInDateRange(Integer teamNumber, Integer season,
                                                  LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds integrations updated within date range.
     */
    List<GitHubIntegration> findUpdatedInDateRange(Integer teamNumber, Integer season,
                                                  LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Exports integration history.
     */
    Map<String, Object> exportIntegrationHistory(Integer teamNumber, Integer season);

    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================

    /**
     * Exports integration to external format.
     */
    Map<String, Object> exportToExternalFormat(Long integrationId, String format);

    /**
     * Imports integration from external source.
     */
    GitHubIntegration importFromExternalSource(Map<String, Object> integrationData, String sourceType);

    /**
     * Syncs with external project management tools.
     */
    GitHubIntegration syncWithExternalTools(Long integrationId, String toolType);

    /**
     * Generates integration backup data.
     */
    Map<String, Object> generateIntegrationBackup(Integer teamNumber, Integer season);

    /**
     * Restores from integration backup.
     */
    List<GitHubIntegration> restoreFromBackup(Map<String, Object> backupData);

    // =========================================================================
    // WEBHOOK AND REAL-TIME INTEGRATION
    // =========================================================================

    /**
     * Processes webhook payload from GitHub.
     */
    void processWebhookPayload(String payload, String eventType);

    /**
     * Handles commit webhook event.
     */
    void handleCommitWebhook(Long integrationId, Map<String, Object> commitData);

    /**
     * Handles pull request webhook event.
     */
    void handlePullRequestWebhook(Long integrationId, Map<String, Object> pullRequestData);

    /**
     * Handles issue webhook event.
     */
    void handleIssueWebhook(Long integrationId, Map<String, Object> issueData);

    /**
     * Handles release webhook event.
     */
    void handleReleaseWebhook(Long integrationId, Map<String, Object> releaseData);

    /**
     * Validates webhook signature.
     */
    boolean validateWebhookSignature(String payload, String signature, String secret);

    /**
     * Finds integrations with webhook configuration.
     */
    List<GitHubIntegration> findWithWebhookConfiguration(Integer teamNumber, Integer season);

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    /**
     * Validates GitHub integration data.
     */
    List<String> validateIntegration(GitHubIntegration integration);

    /**
     * Validates repository URL accessibility.
     */
    boolean validateRepositoryUrl(String repositoryUrl);

    /**
     * Validates user permissions for integration operation.
     */
    boolean validateUserPermissions(Long integrationId, Long userId, String operation);

    /**
     * Checks integration data quality.
     */
    Map<String, Object> checkIntegrationDataQuality(Long integrationId);

    /**
     * Suggests improvements for integration setup.
     */
    List<String> suggestIntegrationImprovements(Long integrationId);

    /**
     * Validates GitHub API access.
     */
    boolean validateGitHubApiAccess(Long integrationId);

    /**
     * Checks repository permissions.
     */
    Map<String, Object> checkRepositoryPermissions(Long integrationId);

    /**
     * Validates integration configuration completeness.
     */
    boolean validateConfigurationCompleteness(Long integrationId);
}