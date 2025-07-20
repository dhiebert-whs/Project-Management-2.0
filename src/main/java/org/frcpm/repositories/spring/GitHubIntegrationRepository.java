// src/main/java/org/frcpm/repositories/spring/GitHubIntegrationRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.GitHubIntegration;
import org.frcpm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GitHubIntegration entities.
 * 
 * Provides comprehensive data access for GitHub integration management including
 * repository analytics, development metrics, team performance tracking, and
 * external integration monitoring for FRC programming teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-ExternalIntegrations
 * @since Phase 4C.1 GitHub Integration for Code Teams
 */
@Repository
public interface GitHubIntegrationRepository extends JpaRepository<GitHubIntegration, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active GitHub integrations for a specific team and season.
     */
    List<GitHubIntegration> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds GitHub integrations by repository type for a team and season.
     */
    List<GitHubIntegration> findByTeamNumberAndSeasonAndRepositoryTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, GitHubIntegration.RepositoryType repositoryType);

    /**
     * Finds GitHub integrations by integration status for a team and season.
     */
    List<GitHubIntegration> findByTeamNumberAndSeasonAndIntegrationStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, GitHubIntegration.IntegrationStatus integrationStatus);

    /**
     * Finds GitHub integration by repository name for a team and season.
     */
    Optional<GitHubIntegration> findByTeamNumberAndSeasonAndRepositoryNameAndIsActiveTrue(
        Integer teamNumber, Integer season, String repositoryName);

    /**
     * Finds GitHub integration by repository URL.
     */
    Optional<GitHubIntegration> findByRepositoryUrlAndIsActiveTrue(String repositoryUrl);

    /**
     * Finds GitHub integrations by organization name.
     */
    List<GitHubIntegration> findByOrganizationNameAndIsActiveTrue(String organizationName);

    /**
     * Finds GitHub integrations by project association.
     */
    List<GitHubIntegration> findByProjectAndIsActiveTrue(Project project);

    // =========================================================================
    // STATUS AND HEALTH QUERIES
    // =========================================================================

    /**
     * Finds active and healthy GitHub integrations.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.integrationStatus = org.frcpm.models.GitHubIntegration.IntegrationStatus.ACTIVE " +
           "AND gi.lastSyncTime > :healthyThreshold AND gi.isActive = true")
    List<GitHubIntegration> findActiveAndHealthyIntegrations(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("healthyThreshold") LocalDateTime healthyThreshold);

    /**
     * Finds GitHub integrations with sync errors.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.integrationStatus = org.frcpm.models.GitHubIntegration.IntegrationStatus.ERROR " +
           "AND gi.isActive = true ORDER BY gi.lastSyncTime DESC")
    List<GitHubIntegration> findIntegrationsWithErrors(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season);

    /**
     * Finds GitHub integrations requiring attention.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.integrationStatus IN (org.frcpm.models.GitHubIntegration.IntegrationStatus.ERROR, " +
           "org.frcpm.models.GitHubIntegration.IntegrationStatus.EXPIRED, " +
           "org.frcpm.models.GitHubIntegration.IntegrationStatus.UNAUTHORIZED) " +
           "OR gi.lastSyncTime IS NULL OR gi.lastSyncTime < :overdueThreshold) " +
           "AND gi.isActive = true ORDER BY gi.lastSyncTime ASC")
    List<GitHubIntegration> findIntegrationsRequiringAttention(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season,
                                                              @Param("overdueThreshold") LocalDateTime overdueThreshold);

    /**
     * Finds GitHub integrations with overdue sync.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.lastSyncTime IS NULL OR gi.lastSyncTime < :overdueThreshold) " +
           "AND gi.isActive = true ORDER BY gi.lastSyncTime ASC")
    List<GitHubIntegration> findOverdueSyncIntegrations(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("overdueThreshold") LocalDateTime overdueThreshold);

    /**
     * Finds GitHub integrations pending configuration.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.integrationStatus IN (org.frcpm.models.GitHubIntegration.IntegrationStatus.PENDING, " +
           "org.frcpm.models.GitHubIntegration.IntegrationStatus.CONFIGURATION_NEEDED) " +
           "AND gi.isActive = true ORDER BY gi.createdAt ASC")
    List<GitHubIntegration> findPendingConfigurationIntegrations(@Param("teamNumber") Integer teamNumber,
                                                                @Param("season") Integer season);

    /**
     * Finds GitHub integrations with webhook issues.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.isWebhookActive = false AND gi.webhookUrl IS NOT NULL " +
           "AND gi.isActive = true")
    List<GitHubIntegration> findIntegrationsWithWebhookIssues(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season);

    // =========================================================================
    // DEVELOPMENT ACTIVITY QUERIES
    // =========================================================================

    /**
     * Finds most active repositories by commit frequency.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.commitFrequency >= :minFrequency AND gi.isActive = true " +
           "ORDER BY gi.commitFrequency DESC")
    List<GitHubIntegration> findMostActiveRepositories(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("minFrequency") Double minFrequency);

    /**
     * Finds repositories with recent development activity.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.lastCommitSync >= :recentThreshold AND gi.isActive = true " +
           "ORDER BY gi.lastCommitSync DESC")
    List<GitHubIntegration> findRecentlyActiveRepositories(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("recentThreshold") LocalDateTime recentThreshold);

    /**
     * Finds inactive repositories with no recent commits.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.lastCommitSync IS NULL OR gi.lastCommitSync < :inactiveThreshold) " +
           "AND gi.isActive = true ORDER BY gi.lastCommitSync ASC")
    List<GitHubIntegration> findInactiveRepositories(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("inactiveThreshold") LocalDateTime inactiveThreshold);

    /**
     * Finds repositories with high development velocity.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.totalCommits >= :minCommits AND gi.commitFrequency >= :minFrequency " +
           "AND gi.isActive = true ORDER BY gi.commitFrequency DESC")
    List<GitHubIntegration> findHighVelocityRepositories(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("minCommits") Integer minCommits,
                                                        @Param("minFrequency") Double minFrequency);

    /**
     * Finds repositories by contributor count range.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.totalContributors BETWEEN :minContributors AND :maxContributors " +
           "AND gi.isActive = true ORDER BY gi.totalContributors DESC")
    List<GitHubIntegration> findByContributorRange(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("minContributors") Integer minContributors,
                                                  @Param("maxContributors") Integer maxContributors);

    // =========================================================================
    // CODE QUALITY AND HEALTH QUERIES
    // =========================================================================

    /**
     * Finds repositories with high code quality scores.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.codeReviewCoverage >= :minCoverage AND gi.testCoverage >= :minTestCoverage " +
           "AND gi.isActive = true ORDER BY gi.codeReviewCoverage DESC, gi.testCoverage DESC")
    List<GitHubIntegration> findHighQualityRepositories(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("minCoverage") Double minCoverage,
                                                       @Param("minTestCoverage") Double minTestCoverage);

    /**
     * Finds repositories with low code quality requiring improvement.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.codeReviewCoverage < :maxCoverage OR gi.testCoverage < :maxTestCoverage) " +
           "AND gi.isActive = true ORDER BY gi.codeReviewCoverage ASC, gi.testCoverage ASC")
    List<GitHubIntegration> findLowQualityRepositories(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("maxCoverage") Double maxCoverage,
                                                      @Param("maxTestCoverage") Double maxTestCoverage);

    /**
     * Finds repositories with best practices implementation.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.hasCodeReviewRequired = true AND gi.hasContinuousIntegration = true " +
           "AND gi.hasAutomatedTesting = true AND gi.isActive = true")
    List<GitHubIntegration> findRepositoriesWithBestPractices(@Param("teamNumber") Integer teamNumber,
                                                             @Param("season") Integer season);

    /**
     * Finds repositories missing CI/CD setup.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.hasContinuousIntegration = false OR gi.hasAutomatedTesting = false) " +
           "AND gi.isActive = true ORDER BY gi.totalCommits DESC")
    List<GitHubIntegration> findRepositoriesMissingCICD(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season);

    /**
     * Finds repositories with high bug fix rates.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.bugsFound > 0 AND gi.bugsFixed > 0 " +
           "AND (CAST(gi.bugsFixed AS double) / gi.bugsFound) >= :minFixRate " +
           "AND gi.isActive = true ORDER BY (CAST(gi.bugsFixed AS double) / gi.bugsFound) DESC")
    List<GitHubIntegration> findHighBugFixRateRepositories(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season,
                                                          @Param("minFixRate") Double minFixRate);

    // =========================================================================
    // TEAM PERFORMANCE QUERIES
    // =========================================================================

    /**
     * Finds repositories with high student involvement.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.studentContributionPercentage >= :minStudentPercentage " +
           "AND gi.isActive = true ORDER BY gi.studentContributionPercentage DESC")
    List<GitHubIntegration> findHighStudentInvolvementRepositories(@Param("teamNumber") Integer teamNumber,
                                                                  @Param("season") Integer season,
                                                                  @Param("minStudentPercentage") Double minStudentPercentage);

    /**
     * Finds repositories with mentor-heavy development.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.mentorContributionPercentage >= :minMentorPercentage " +
           "AND gi.isActive = true ORDER BY gi.mentorContributionPercentage DESC")
    List<GitHubIntegration> findMentorHeavyRepositories(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("minMentorPercentage") Double minMentorPercentage);

    /**
     * Finds repositories with balanced team contribution.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.studentContributionPercentage BETWEEN :minStudent AND :maxStudent " +
           "AND gi.mentorContributionPercentage BETWEEN :minMentor AND :maxMentor " +
           "AND gi.isActive = true ORDER BY gi.totalContributors DESC")
    List<GitHubIntegration> findBalancedContributionRepositories(@Param("teamNumber") Integer teamNumber,
                                                                @Param("season") Integer season,
                                                                @Param("minStudent") Double minStudent,
                                                                @Param("maxStudent") Double maxStudent,
                                                                @Param("minMentor") Double minMentor,
                                                                @Param("maxMentor") Double maxMentor);

    /**
     * Finds repositories by active developer count.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.activeStudentDevelopers + gi.activeMentorDevelopers) >= :minDevelopers " +
           "AND gi.isActive = true ORDER BY (gi.activeStudentDevelopers + gi.activeMentorDevelopers) DESC")
    List<GitHubIntegration> findByActiveDeveloperCount(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("minDevelopers") Integer minDevelopers);

    // =========================================================================
    // REPOSITORY SIZE AND COMPLEXITY QUERIES
    // =========================================================================

    /**
     * Finds repositories by codebase size range.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.codebaseSize BETWEEN :minSize AND :maxSize AND gi.isActive = true " +
           "ORDER BY gi.codebaseSize DESC")
    List<GitHubIntegration> findByCodebaseSizeRange(@Param("teamNumber") Integer teamNumber,
                                                   @Param("season") Integer season,
                                                   @Param("minSize") Long minSize,
                                                   @Param("maxSize") Long maxSize);

    /**
     * Finds largest repositories by lines of code.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.codebaseSize >= :minSize AND gi.isActive = true " +
           "ORDER BY gi.codebaseSize DESC")
    List<GitHubIntegration> findLargestRepositories(@Param("teamNumber") Integer teamNumber,
                                                   @Param("season") Integer season,
                                                   @Param("minSize") Long minSize);

    /**
     * Finds repositories with many pull requests.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.totalPullRequests >= :minPRs AND gi.isActive = true " +
           "ORDER BY gi.totalPullRequests DESC")
    List<GitHubIntegration> findHighPullRequestRepositories(@Param("teamNumber") Integer teamNumber,
                                                           @Param("season") Integer season,
                                                           @Param("minPRs") Integer minPRs);

    /**
     * Finds repositories with many releases.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.totalReleases >= :minReleases AND gi.isActive = true " +
           "ORDER BY gi.totalReleases DESC")
    List<GitHubIntegration> findActiveReleaseRepositories(@Param("teamNumber") Integer teamNumber,
                                                         @Param("season") Integer season,
                                                         @Param("minReleases") Integer minReleases);

    /**
     * Finds repositories with high branch activity.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.totalBranches >= :minBranches AND gi.isActive = true " +
           "ORDER BY gi.totalBranches DESC")
    List<GitHubIntegration> findHighBranchActivityRepositories(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season,
                                                              @Param("minBranches") Integer minBranches);

    // =========================================================================
    // ISSUE AND PROJECT MANAGEMENT QUERIES
    // =========================================================================

    /**
     * Finds repositories with many open issues.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.openIssues >= :minOpenIssues AND gi.isActive = true " +
           "ORDER BY gi.openIssues DESC")
    List<GitHubIntegration> findRepositoriesWithManyOpenIssues(@Param("teamNumber") Integer teamNumber,
                                                              @Param("season") Integer season,
                                                              @Param("minOpenIssues") Integer minOpenIssues);

    /**
     * Finds repositories with good issue resolution rates.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.openIssues + gi.closedIssues) > 0 " +
           "AND (CAST(gi.closedIssues AS double) / (gi.openIssues + gi.closedIssues)) >= :minResolutionRate " +
           "AND gi.isActive = true ORDER BY (CAST(gi.closedIssues AS double) / (gi.openIssues + gi.closedIssues)) DESC")
    List<GitHubIntegration> findGoodIssueResolutionRepositories(@Param("teamNumber") Integer teamNumber,
                                                               @Param("season") Integer season,
                                                               @Param("minResolutionRate") Double minResolutionRate);

    /**
     * Finds repositories with issue tracking problems.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.openIssues > gi.closedIssues AND gi.openIssues >= :issueThreshold " +
           "AND gi.isActive = true ORDER BY gi.openIssues DESC")
    List<GitHubIntegration> findRepositoriesWithIssueBacklog(@Param("teamNumber") Integer teamNumber,
                                                            @Param("season") Integer season,
                                                            @Param("issueThreshold") Integer issueThreshold);

    // =========================================================================
    // ANALYTICS AND AGGREGATION QUERIES
    // =========================================================================

    /**
     * Counts GitHub integrations by repository type.
     */
    @Query("SELECT gi.repositoryType, COUNT(gi) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true " +
           "GROUP BY gi.repositoryType ORDER BY COUNT(gi) DESC")
    List<Object[]> countByRepositoryType(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts GitHub integrations by integration status.
     */
    @Query("SELECT gi.integrationStatus, COUNT(gi) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true " +
           "GROUP BY gi.integrationStatus ORDER BY COUNT(gi) DESC")
    List<Object[]> countByIntegrationStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total commits across all repositories.
     */
    @Query("SELECT SUM(gi.totalCommits) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true")
    Long calculateTotalCommits(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total codebase size across all repositories.
     */
    @Query("SELECT SUM(gi.codebaseSize) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true")
    Long calculateTotalCodebaseSize(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average code review coverage.
     */
    @Query("SELECT AVG(gi.codeReviewCoverage) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true")
    Double calculateAverageCodeReviewCoverage(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average test coverage.
     */
    @Query("SELECT AVG(gi.testCoverage) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true")
    Double calculateAverageTestCoverage(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average commit frequency.
     */
    @Query("SELECT AVG(gi.commitFrequency) FROM GitHubIntegration gi " +
           "WHERE gi.teamNumber = :teamNumber AND gi.season = :season AND gi.isActive = true")
    Double calculateAverageCommitFrequency(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most productive repositories by multiple metrics.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.isActive = true ORDER BY (gi.totalCommits * 0.3 + gi.totalPullRequests * 0.3 + " +
           "gi.totalContributors * 0.2 + gi.commitFrequency * 0.2) DESC")
    List<GitHubIntegration> findMostProductiveRepositories(@Param("teamNumber") Integer teamNumber,
                                                          @Param("season") Integer season);

    // =========================================================================
    // SEARCH AND FILTERING QUERIES
    // =========================================================================

    /**
     * Searches GitHub integrations by repository name.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND LOWER(gi.repositoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND gi.isActive = true " +
           "ORDER BY gi.repositoryName ASC")
    List<GitHubIntegration> searchByRepositoryName(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("searchTerm") String searchTerm);

    /**
     * Searches GitHub integrations by description.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND LOWER(gi.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND gi.isActive = true " +
           "ORDER BY gi.repositoryName ASC")
    List<GitHubIntegration> searchByDescription(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("searchTerm") String searchTerm);

    /**
     * Full text search across GitHub integrations.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (LOWER(gi.repositoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(gi.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(gi.organizationName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND gi.isActive = true ORDER BY gi.repositoryName ASC")
    List<GitHubIntegration> fullTextSearch(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season,
                                          @Param("searchTerm") String searchTerm);

    // =========================================================================
    // CROSS-SEASON ANALYSIS QUERIES
    // =========================================================================

    /**
     * Finds GitHub integrations across multiple seasons for comparison.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber " +
           "AND gi.season IN :seasons AND gi.isActive = true " +
           "ORDER BY gi.season DESC, gi.totalCommits DESC")
    List<GitHubIntegration> findMultiSeasonIntegrations(@Param("teamNumber") Integer teamNumber,
                                                       @Param("seasons") List<Integer> seasons);

    /**
     * Finds repository evolution across seasons by name.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber " +
           "AND gi.repositoryName = :repositoryName AND gi.isActive = true " +
           "ORDER BY gi.season ASC")
    List<GitHubIntegration> findRepositoryEvolution(@Param("teamNumber") Integer teamNumber,
                                                   @Param("repositoryName") String repositoryName);

    /**
     * Finds teams with consistent repository management across seasons.
     */
    @Query("SELECT gi.teamNumber, COUNT(DISTINCT gi.season) as seasonCount FROM GitHubIntegration gi " +
           "WHERE gi.season IN :seasons AND gi.isActive = true " +
           "GROUP BY gi.teamNumber HAVING COUNT(DISTINCT gi.season) >= :minSeasons " +
           "ORDER BY seasonCount DESC")
    List<Object[]> findConsistentTeamsAcrossSeasons(@Param("seasons") List<Integer> seasons,
                                                   @Param("minSeasons") Integer minSeasons);

    // =========================================================================
    // BULK OPERATIONS QUERIES
    // =========================================================================

    /**
     * Counts total active GitHub integrations for a team and season.
     */
    Long countByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds all active GitHub integrations for bulk export.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.isActive = true ORDER BY gi.repositoryName ASC")
    List<GitHubIntegration> findAllActiveIntegrations(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season);

    /**
     * Finds GitHub integrations created within date range.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.createdAt BETWEEN :startDate AND :endDate AND gi.isActive = true " +
           "ORDER BY gi.createdAt DESC")
    List<GitHubIntegration> findCreatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Finds GitHub integrations updated within date range.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.updatedAt BETWEEN :startDate AND :endDate AND gi.isActive = true " +
           "ORDER BY gi.updatedAt DESC")
    List<GitHubIntegration> findUpdatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Finds archived GitHub integrations.
     */
    List<GitHubIntegration> findByTeamNumberAndSeasonAndIsActiveFalse(Integer teamNumber, Integer season);

    // =========================================================================
    // CONFIGURATION AND SETUP QUERIES
    // =========================================================================

    /**
     * Finds GitHub integrations with webhook configuration.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.webhookUrl IS NOT NULL AND gi.isActive = true")
    List<GitHubIntegration> findWithWebhookConfiguration(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season);

    /**
     * Finds GitHub integrations with access token issues.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.integrationStatus IN (org.frcpm.models.GitHubIntegration.IntegrationStatus.EXPIRED, " +
           "org.frcpm.models.GitHubIntegration.IntegrationStatus.UNAUTHORIZED) " +
           "AND gi.isActive = true ORDER BY gi.lastSyncTime ASC")
    List<GitHubIntegration> findWithAccessTokenIssues(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season);

    /**
     * Finds GitHub integrations with sync configuration enabled.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND (gi.syncCommits = true OR gi.syncPullRequests = true OR gi.syncIssues = true OR gi.syncReleases = true) " +
           "AND gi.isActive = true")
    List<GitHubIntegration> findWithSyncEnabled(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season);

    /**
     * Finds GitHub integrations by configured team member.
     */
    @Query("SELECT gi FROM GitHubIntegration gi WHERE gi.teamNumber = :teamNumber AND gi.season = :season " +
           "AND gi.configuredBy.id = :teamMemberId AND gi.isActive = true " +
           "ORDER BY gi.createdAt DESC")
    List<GitHubIntegration> findByConfiguredBy(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("teamMemberId") Long teamMemberId);
}