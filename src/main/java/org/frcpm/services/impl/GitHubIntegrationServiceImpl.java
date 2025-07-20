// src/main/java/org/frcpm/services/impl/GitHubIntegrationServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.GitHubIntegration;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.GitHubIntegrationRepository;
import org.frcpm.services.GitHubIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of GitHubIntegrationService.
 * 
 * Provides comprehensive GitHub integration services including repository
 * management, development analytics, team performance tracking, webhook
 * processing, and sophisticated business logic for FRC programming teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-ExternalIntegrations
 * @since Phase 4C.1 GitHub Integration for Code Teams
 */
@Service
@Transactional
public class GitHubIntegrationServiceImpl implements GitHubIntegrationService {

    @Autowired
    private GitHubIntegrationRepository integrationRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public GitHubIntegration create(GitHubIntegration integration) {
        return createIntegration(integration);
    }

    @Override
    public GitHubIntegration update(Long id, GitHubIntegration integration) {
        return updateIntegration(id, integration);
    }

    @Override
    public void delete(Long id) {
        archiveIntegration(id, "Deleted by user");
    }

    @Override
    public Optional<GitHubIntegration> findById(Long id) {
        return integrationRepository.findById(id);
    }

    @Override
    public List<GitHubIntegration> findAll() {
        return integrationRepository.findAll().stream()
                .filter(GitHubIntegration::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return integrationRepository.existsById(id);
    }

    @Override
    public long count() {
        return integrationRepository.count();
    }

    // =========================================================================
    // INTEGRATION MANAGEMENT
    // =========================================================================

    @Override
    public GitHubIntegration createIntegration(GitHubIntegration integration) {
        List<String> validationErrors = validateIntegration(integration);
        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Integration validation failed: " + String.join(", ", validationErrors));
        }
        
        // Set defaults
        if (integration.getCreatedAt() == null) {
            integration.setCreatedAt(LocalDateTime.now());
        }
        if (integration.getUpdatedAt() == null) {
            integration.setUpdatedAt(LocalDateTime.now());
        }
        if (integration.getIsActive() == null) {
            integration.setIsActive(true);
        }
        if (integration.getIntegrationStatus() == null) {
            integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.PENDING);
        }
        
        // Initialize metrics
        integration = initializeMetrics(integration);
        
        GitHubIntegration saved = integrationRepository.save(integration);
        
        // Test connectivity if access token is provided
        if (integration.getAccessToken() != null && !integration.getAccessToken().isEmpty()) {
            testIntegrationConnectivity(saved.getId());
        }
        
        return saved;
    }

    @Override
    public GitHubIntegration createIntegration(Integer teamNumber, Integer season, String repositoryName,
                                              String repositoryUrl, GitHubIntegration.RepositoryType repositoryType,
                                              TeamMember configuredBy) {
        GitHubIntegration integration = new GitHubIntegration();
        integration.setTeamNumber(teamNumber);
        integration.setSeason(season);
        integration.setRepositoryName(repositoryName);
        integration.setRepositoryUrl(repositoryUrl);
        integration.setRepositoryType(repositoryType);
        integration.setConfiguredBy(configuredBy);
        
        return createIntegration(integration);
    }

    @Override
    public GitHubIntegration createIntegrationWithProject(Integer teamNumber, Integer season, String repositoryName,
                                                         String repositoryUrl, GitHubIntegration.RepositoryType repositoryType,
                                                         Project project, TeamMember configuredBy) {
        GitHubIntegration integration = createIntegration(teamNumber, season, repositoryName, repositoryUrl, 
                                                        repositoryType, configuredBy);
        integration.setProject(project);
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateIntegration(Long integrationId, GitHubIntegration integration) {
        GitHubIntegration existing = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Update fields
        updateIntegrationFields(existing, integration);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Revalidate if critical fields changed
        if (hasUrlOrTokenChanged(existing, integration)) {
            testIntegrationConnectivity(integrationId);
        }
        
        return integrationRepository.save(existing);
    }

    @Override
    public void archiveIntegration(Long integrationId, String reason) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setIsActive(false);
        integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.INACTIVE);
        integration.setIntegrationNotes("Archived: " + reason);
        integration.setUpdatedAt(LocalDateTime.now());
        
        integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration restoreIntegration(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setIsActive(true);
        integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.PENDING);
        integration.setUpdatedAt(LocalDateTime.now());
        
        GitHubIntegration restored = integrationRepository.save(integration);
        
        // Test connectivity after restoration
        testIntegrationConnectivity(integrationId);
        
        return restored;
    }

    @Override
    public List<GitHubIntegration> findActiveIntegrations(Integer teamNumber, Integer season) {
        return integrationRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findByRepositoryType(Integer teamNumber, Integer season,
                                                       GitHubIntegration.RepositoryType repositoryType) {
        return integrationRepository.findByTeamNumberAndSeasonAndRepositoryTypeAndIsActiveTrue(
                teamNumber, season, repositoryType);
    }

    @Override
    public Optional<GitHubIntegration> findByRepositoryName(Integer teamNumber, Integer season, String repositoryName) {
        return integrationRepository.findByTeamNumberAndSeasonAndRepositoryNameAndIsActiveTrue(
                teamNumber, season, repositoryName);
    }

    @Override
    public Optional<GitHubIntegration> findByRepositoryUrl(String repositoryUrl) {
        return integrationRepository.findByRepositoryUrlAndIsActiveTrue(repositoryUrl);
    }

    // =========================================================================
    // REPOSITORY CONFIGURATION MANAGEMENT
    // =========================================================================

    @Override
    public GitHubIntegration configureWebhook(Long integrationId, String webhookUrl) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setWebhookUrl(webhookUrl);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration activateWebhook(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        if (integration.getWebhookUrl() == null || integration.getWebhookUrl().isEmpty()) {
            throw new RuntimeException("Webhook URL must be configured before activation");
        }
        
        integration.setIsWebhookActive(true);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration deactivateWebhook(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setIsWebhookActive(false);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateAccessToken(Long integrationId, String accessToken) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Encrypt token in production implementation
        integration.setAccessToken(encryptAccessToken(accessToken));
        integration.setUpdatedAt(LocalDateTime.now());
        
        GitHubIntegration updated = integrationRepository.save(integration);
        
        // Test connectivity with new token
        testIntegrationConnectivity(integrationId);
        
        return updated;
    }

    @Override
    public GitHubIntegration configureSyncSettings(Long integrationId, boolean syncCommits, boolean syncPullRequests,
                                                  boolean syncIssues, boolean syncReleases) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setSyncCommits(syncCommits);
        integration.setSyncPullRequests(syncPullRequests);
        integration.setSyncIssues(syncIssues);
        integration.setSyncReleases(syncReleases);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateRepositoryConfiguration(Long integrationId, boolean hasCodeReviewRequired,
                                                          boolean hasContinuousIntegration, boolean hasAutomatedTesting,
                                                          boolean hasDeploymentPipeline) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setHasCodeReviewRequired(hasCodeReviewRequired);
        integration.setHasContinuousIntegration(hasContinuousIntegration);
        integration.setHasAutomatedTesting(hasAutomatedTesting);
        integration.setHasDeploymentPipeline(hasDeploymentPipeline);
        integration.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate health score
        calculateRepositoryHealthScore(integrationId);
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration setDefaultBranch(Long integrationId, String defaultBranch) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setDefaultBranch(defaultBranch);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration associateWithProject(Long integrationId, Project project) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setProject(project);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration removeProjectAssociation(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setProject(null);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    // =========================================================================
    // TEAM MEMBER MANAGEMENT
    // =========================================================================

    @Override
    public GitHubIntegration addTeamMember(Long integrationId, String githubUsername, Long teamMemberId, String role) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.addTeamMember(githubUsername, teamMemberId, role);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration removeTeamMember(Long integrationId, String githubUsername) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.removeTeamMember(githubUsername);
        integration.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate contribution percentages
        integration.calculateContributionPercentages();
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateTeamMemberRole(Long integrationId, String githubUsername, String role) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.getTeamMembers().stream()
                .filter(member -> member.getGithubUsername().equals(githubUsername))
                .findFirst()
                .ifPresent(member -> member.setRole(role));
        
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateTeamMemberStatistics(Long integrationId, String githubUsername, int commits,
                                                       int pullRequests, long linesAdded, long linesDeleted) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.updateTeamMemberStats(githubUsername, commits, pullRequests, linesAdded, linesDeleted);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateContributionPercentages(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.calculateContributionPercentages();
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration syncTeamMemberData(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // This would implement actual GitHub API calls to sync member data
        // For now, update timestamps and recalculate percentages
        integration.calculateContributionPercentages();
        integration.setLastSyncTime(LocalDateTime.now());
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    // =========================================================================
    // SYNC AND DATA REFRESH OPERATIONS
    // =========================================================================

    @Override
    public GitHubIntegration performFullSync(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        try {
            // Sync all enabled data types
            if (integration.getSyncCommits()) {
                syncCommits(integrationId);
            }
            if (integration.getSyncPullRequests()) {
                syncPullRequests(integrationId);
            }
            if (integration.getSyncIssues()) {
                syncIssues(integrationId);
            }
            if (integration.getSyncReleases()) {
                syncReleases(integrationId);
            }
            
            // Update repository statistics
            updateRepositoryStatistics(integrationId);
            updateDevelopmentMetrics(integrationId);
            
            // Update health and timestamps
            integration.setLastSyncTime(LocalDateTime.now());
            integration.setLastSyncStatus("SUCCESS: Full sync completed");
            integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.ACTIVE);
            integration.setLastSyncError(null);
            integration.setUpdatedAt(LocalDateTime.now());
            
        } catch (Exception e) {
            recordSyncError(integrationId, e.getMessage());
        }
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration syncCommits(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        try {
            // This would implement actual GitHub API calls to sync commit data
            // For now, simulate sync and update metrics
            
            // Update commit-related metrics
            integration.setTotalCommits(integration.getTotalCommits() + generateRandomCommitCount());
            integration.setCommitFrequency(calculateCommitFrequency(integration));
            integration.updateSyncTimestamp("commits");
            
        } catch (Exception e) {
            recordSyncError(integrationId, "Commit sync failed: " + e.getMessage());
        }
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration syncPullRequests(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        try {
            // This would implement actual GitHub API calls to sync PR data
            // For now, simulate sync and update metrics
            
            integration.setTotalPullRequests(integration.getTotalPullRequests() + generateRandomPRCount());
            integration.updateSyncTimestamp("pullrequests");
            
        } catch (Exception e) {
            recordSyncError(integrationId, "Pull request sync failed: " + e.getMessage());
        }
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration syncIssues(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        try {
            // This would implement actual GitHub API calls to sync issue data
            // For now, simulate sync and update metrics
            
            Map<String, Integer> issueData = generateRandomIssueData();
            integration.setOpenIssues(issueData.get("open"));
            integration.setClosedIssues(issueData.get("closed"));
            integration.updateSyncTimestamp("issues");
            
        } catch (Exception e) {
            recordSyncError(integrationId, "Issue sync failed: " + e.getMessage());
        }
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration syncReleases(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        try {
            // This would implement actual GitHub API calls to sync release data
            // For now, simulate sync and update metrics
            
            integration.setTotalReleases(integration.getTotalReleases() + generateRandomReleaseCount());
            integration.updateSyncTimestamp("releases");
            
        } catch (Exception e) {
            recordSyncError(integrationId, "Release sync failed: " + e.getMessage());
        }
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateRepositoryStatistics(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // This would implement actual GitHub API calls to get repository statistics
        // For now, simulate updates
        
        integration.setTotalContributors(Math.max(integration.getTeamMembers().size(), integration.getTotalContributors()));
        integration.setTotalBranches(integration.getTotalBranches() + generateRandomBranchCount());
        integration.setCodebaseSize(integration.getCodebaseSize() + generateRandomCodebaseGrowth());
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateDevelopmentMetrics(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Calculate and update various development metrics
        integration.setCommitFrequency(calculateCommitFrequency(integration));
        integration.setAveragePullRequestSize(calculateAveragePullRequestSize(integration));
        
        // Update quality metrics if data is available
        if (integration.getHasAutomatedTesting()) {
            integration.setTestCoverage(Math.min(integration.getTestCoverage() + 1.0, 100.0));
        }
        if (integration.getHasCodeReviewRequired()) {
            integration.setCodeReviewCoverage(Math.min(integration.getCodeReviewCoverage() + 0.5, 100.0));
        }
        
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration recordSyncError(Long integrationId, String error) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.recordSyncError(error);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration clearSyncError(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.ACTIVE);
        integration.setLastSyncError(null);
        integration.setLastSyncStatus("ERROR CLEARED");
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateHealthStatus(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Update health status based on sync times and errors
        if (integration.isHealthy()) {
            integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.ACTIVE);
        } else if (integration.isSyncOverdue()) {
            integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.CONFIGURATION_NEEDED);
        }
        
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    // =========================================================================
    // STATUS AND HEALTH MONITORING
    // =========================================================================

    @Override
    public List<GitHubIntegration> findActiveAndHealthyIntegrations(Integer teamNumber, Integer season) {
        LocalDateTime healthyThreshold = LocalDateTime.now().minusHours(24);
        return integrationRepository.findActiveAndHealthyIntegrations(teamNumber, season, healthyThreshold);
    }

    @Override
    public List<GitHubIntegration> findIntegrationsWithErrors(Integer teamNumber, Integer season) {
        return integrationRepository.findIntegrationsWithErrors(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findIntegrationsRequiringAttention(Integer teamNumber, Integer season) {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(6);
        return integrationRepository.findIntegrationsRequiringAttention(teamNumber, season, overdueThreshold);
    }

    @Override
    public List<GitHubIntegration> findOverdueSyncIntegrations(Integer teamNumber, Integer season) {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(6);
        return integrationRepository.findOverdueSyncIntegrations(teamNumber, season, overdueThreshold);
    }

    @Override
    public List<GitHubIntegration> findPendingConfigurationIntegrations(Integer teamNumber, Integer season) {
        return integrationRepository.findPendingConfigurationIntegrations(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findIntegrationsWithWebhookIssues(Integer teamNumber, Integer season) {
        return integrationRepository.findIntegrationsWithWebhookIssues(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findWithAccessTokenIssues(Integer teamNumber, Integer season) {
        return integrationRepository.findWithAccessTokenIssues(teamNumber, season);
    }

    @Override
    public GitHubIntegration updateIntegrationStatus(Long integrationId, GitHubIntegration.IntegrationStatus status) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setIntegrationStatus(status);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public List<String> validateIntegrationConfiguration(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        List<String> issues = new ArrayList<>();
        
        if (integration.getRepositoryUrl() == null || integration.getRepositoryUrl().isEmpty()) {
            issues.add("Repository URL is required");
        }
        
        if (integration.getAccessToken() == null || integration.getAccessToken().isEmpty()) {
            issues.add("Access token is required for API access");
        }
        
        if (integration.getRepositoryType() == null) {
            issues.add("Repository type must be specified");
        }
        
        if (integration.getIsWebhookActive() && 
            (integration.getWebhookUrl() == null || integration.getWebhookUrl().isEmpty())) {
            issues.add("Webhook URL required when webhook is active");
        }
        
        return issues;
    }

    @Override
    public boolean testIntegrationConnectivity(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        try {
            // This would implement actual GitHub API connectivity test
            // For now, simulate test and update status
            
            if (integration.getAccessToken() != null && !integration.getAccessToken().isEmpty()) {
                integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.ACTIVE);
                integration.setLastSyncStatus("Connectivity test passed");
                integration.setLastSyncError(null);
                integrationRepository.save(integration);
                return true;
            } else {
                integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.CONFIGURATION_NEEDED);
                integration.setLastSyncStatus("Access token required");
                integrationRepository.save(integration);
                return false;
            }
            
        } catch (Exception e) {
            recordSyncError(integrationId, "Connectivity test failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // DEVELOPMENT ACTIVITY TRACKING
    // =========================================================================

    @Override
    public List<GitHubIntegration> findMostActiveRepositories(Integer teamNumber, Integer season, Double minFrequency) {
        return integrationRepository.findMostActiveRepositories(teamNumber, season, minFrequency);
    }

    @Override
    public List<GitHubIntegration> findRecentlyActiveRepositories(Integer teamNumber, Integer season, Integer days) {
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(days);
        return integrationRepository.findRecentlyActiveRepositories(teamNumber, season, recentThreshold);
    }

    @Override
    public List<GitHubIntegration> findInactiveRepositories(Integer teamNumber, Integer season, Integer days) {
        LocalDateTime inactiveThreshold = LocalDateTime.now().minusDays(days);
        return integrationRepository.findInactiveRepositories(teamNumber, season, inactiveThreshold);
    }

    @Override
    public List<GitHubIntegration> findHighVelocityRepositories(Integer teamNumber, Integer season,
                                                               Integer minCommits, Double minFrequency) {
        return integrationRepository.findHighVelocityRepositories(teamNumber, season, minCommits, minFrequency);
    }

    @Override
    public List<GitHubIntegration> findByContributorRange(Integer teamNumber, Integer season,
                                                         Integer minContributors, Integer maxContributors) {
        return integrationRepository.findByContributorRange(teamNumber, season, minContributors, maxContributors);
    }

    @Override
    public GitHubIntegration updateCommitFrequency(Long integrationId, Double frequency) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setCommitFrequency(frequency);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateAveragePullRequestSize(Long integrationId, Double averageSize) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setAveragePullRequestSize(averageSize);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration calculateDevelopmentVelocity(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Calculate velocity based on recent activity
        double velocity = calculateCommitFrequency(integration);
        integration.setCommitFrequency(velocity);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    // =========================================================================
    // CODE QUALITY AND HEALTH MANAGEMENT
    // =========================================================================

    @Override
    public List<GitHubIntegration> findHighQualityRepositories(Integer teamNumber, Integer season,
                                                              Double minCoverage, Double minTestCoverage) {
        return integrationRepository.findHighQualityRepositories(teamNumber, season, minCoverage, minTestCoverage);
    }

    @Override
    public List<GitHubIntegration> findLowQualityRepositories(Integer teamNumber, Integer season,
                                                             Double maxCoverage, Double maxTestCoverage) {
        return integrationRepository.findLowQualityRepositories(teamNumber, season, maxCoverage, maxTestCoverage);
    }

    @Override
    public List<GitHubIntegration> findRepositoriesWithBestPractices(Integer teamNumber, Integer season) {
        return integrationRepository.findRepositoriesWithBestPractices(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findRepositoriesMissingCICD(Integer teamNumber, Integer season) {
        return integrationRepository.findRepositoriesMissingCICD(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findHighBugFixRateRepositories(Integer teamNumber, Integer season, Double minFixRate) {
        return integrationRepository.findHighBugFixRateRepositories(teamNumber, season, minFixRate);
    }

    @Override
    public GitHubIntegration updateCodeReviewCoverage(Long integrationId, Double coverage) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setCodeReviewCoverage(Math.max(0.0, Math.min(100.0, coverage)));
        integration.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate health score
        calculateRepositoryHealthScore(integrationId);
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateTestCoverage(Long integrationId, Double coverage) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setTestCoverage(Math.max(0.0, Math.min(100.0, coverage)));
        integration.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate health score
        calculateRepositoryHealthScore(integrationId);
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateBugStatistics(Long integrationId, Integer bugsFound, Integer bugsFixed) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setBugsFound(bugsFound);
        integration.setBugsFixed(bugsFixed);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration calculateRepositoryHealthScore(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // The health score is calculated by the model's business method
        double healthScore = integration.getRepositoryHealthScore();
        
        // Update any health-related status based on score
        if (healthScore >= 80.0) {
            integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.ACTIVE);
        } else if (healthScore < 50.0) {
            integration.setIntegrationStatus(GitHubIntegration.IntegrationStatus.CONFIGURATION_NEEDED);
        }
        
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public Map<String, Object> analyzeCodeQualityTrends(Long integrationId, Integer days) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        Map<String, Object> trends = new HashMap<>();
        
        // This would implement trend analysis over time
        // For now, return current metrics
        trends.put("currentHealthScore", integration.getRepositoryHealthScore());
        trends.put("codeReviewCoverage", integration.getCodeReviewCoverage());
        trends.put("testCoverage", integration.getTestCoverage());
        trends.put("bugFixRate", integration.getBugFixRate());
        trends.put("followsBestPractices", integration.followsBestPractices());
        trends.put("analysisDate", LocalDateTime.now());
        
        return trends;
    }

    // =========================================================================
    // TEAM PERFORMANCE ANALYSIS
    // =========================================================================

    @Override
    public List<GitHubIntegration> findHighStudentInvolvementRepositories(Integer teamNumber, Integer season,
                                                                          Double minStudentPercentage) {
        return integrationRepository.findHighStudentInvolvementRepositories(teamNumber, season, minStudentPercentage);
    }

    @Override
    public List<GitHubIntegration> findMentorHeavyRepositories(Integer teamNumber, Integer season,
                                                              Double minMentorPercentage) {
        return integrationRepository.findMentorHeavyRepositories(teamNumber, season, minMentorPercentage);
    }

    @Override
    public List<GitHubIntegration> findBalancedContributionRepositories(Integer teamNumber, Integer season,
                                                                        Double minStudent, Double maxStudent,
                                                                        Double minMentor, Double maxMentor) {
        return integrationRepository.findBalancedContributionRepositories(teamNumber, season, 
                minStudent, maxStudent, minMentor, maxMentor);
    }

    @Override
    public List<GitHubIntegration> findByActiveDeveloperCount(Integer teamNumber, Integer season, Integer minDevelopers) {
        return integrationRepository.findByActiveDeveloperCount(teamNumber, season, minDevelopers);
    }

    @Override
    public GitHubIntegration updateActiveDeveloperCounts(Long integrationId, Integer studentDevelopers,
                                                        Integer mentorDevelopers) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setActiveStudentDevelopers(studentDevelopers);
        integration.setActiveMentorDevelopers(mentorDevelopers);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public Map<String, Object> analyzeTeamPerformanceTrends(Integer teamNumber, Integer season, Integer days) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        Map<String, Object> trends = new HashMap<>();
        
        // Calculate aggregate metrics
        double avgStudentContribution = integrations.stream()
                .mapToDouble(GitHubIntegration::getStudentContributionPercentage)
                .average()
                .orElse(0.0);
        
        double avgCommitFrequency = integrations.stream()
                .mapToDouble(GitHubIntegration::getCommitFrequency)
                .average()
                .orElse(0.0);
        
        int totalActiveDevelopers = integrations.stream()
                .mapToInt(i -> i.getActiveStudentDevelopers() + i.getActiveMentorDevelopers())
                .sum();
        
        trends.put("averageStudentContribution", avgStudentContribution);
        trends.put("averageCommitFrequency", avgCommitFrequency);
        trends.put("totalActiveDevelopers", totalActiveDevelopers);
        trends.put("repositoryCount", integrations.size());
        trends.put("analysisDate", LocalDateTime.now());
        
        return trends;
    }

    @Override
    public Map<String, Object> generateTeamCollaborationReport(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        Map<String, Object> report = new HashMap<>();
        
        // Team collaboration metrics
        int totalTeamMembers = integrations.stream()
                .mapToInt(i -> i.getTeamMembers().size())
                .sum();
        
        int totalStudents = integrations.stream()
                .mapToInt(GitHubIntegration::getActiveStudentDevelopers)
                .sum();
        
        int totalMentors = integrations.stream()
                .mapToInt(GitHubIntegration::getActiveMentorDevelopers)
                .sum();
        
        double avgHealthScore = integrations.stream()
                .mapToDouble(GitHubIntegration::getRepositoryHealthScore)
                .average()
                .orElse(0.0);
        
        report.put("totalTeamMembers", totalTeamMembers);
        report.put("activeStudentDevelopers", totalStudents);
        report.put("activeMentorDevelopers", totalMentors);
        report.put("averageRepositoryHealth", avgHealthScore);
        report.put("repositoriesWithBestPractices", findRepositoriesWithBestPractices(teamNumber, season).size());
        report.put("highQualityRepositories", findHighQualityRepositories(teamNumber, season, 80.0, 70.0).size());
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    public Double calculateTeamPerformanceScore(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        if (integrations.isEmpty()) {
            return 0.0;
        }
        
        // Calculate weighted performance score
        double totalScore = 0.0;
        double weightSum = 0.0;
        
        for (GitHubIntegration integration : integrations) {
            double weight = 1.0; // Base weight
            
            // Weight by repository importance
            if (integration.getRepositoryType() == GitHubIntegration.RepositoryType.ROBOT_CODE) {
                weight = 2.0; // Robot code is more important
            }
            
            double score = integration.getTeamPerformanceRating().equals("EXCELLENT") ? 100.0 :
                          integration.getTeamPerformanceRating().equals("GOOD") ? 80.0 :
                          integration.getTeamPerformanceRating().equals("AVERAGE") ? 60.0 :
                          integration.getTeamPerformanceRating().equals("BELOW_AVERAGE") ? 40.0 : 20.0;
            
            totalScore += score * weight;
            weightSum += weight;
        }
        
        return weightSum > 0 ? totalScore / weightSum : 0.0;
    }

    // =========================================================================
    // REPOSITORY SIZE AND COMPLEXITY ANALYSIS
    // =========================================================================

    @Override
    public List<GitHubIntegration> findByCodebaseSizeRange(Integer teamNumber, Integer season,
                                                          Long minSize, Long maxSize) {
        return integrationRepository.findByCodebaseSizeRange(teamNumber, season, minSize, maxSize);
    }

    @Override
    public List<GitHubIntegration> findLargestRepositories(Integer teamNumber, Integer season, Long minSize) {
        return integrationRepository.findLargestRepositories(teamNumber, season, minSize);
    }

    @Override
    public List<GitHubIntegration> findHighPullRequestRepositories(Integer teamNumber, Integer season, Integer minPRs) {
        return integrationRepository.findHighPullRequestRepositories(teamNumber, season, minPRs);
    }

    @Override
    public List<GitHubIntegration> findActiveReleaseRepositories(Integer teamNumber, Integer season, Integer minReleases) {
        return integrationRepository.findActiveReleaseRepositories(teamNumber, season, minReleases);
    }

    @Override
    public List<GitHubIntegration> findHighBranchActivityRepositories(Integer teamNumber, Integer season,
                                                                     Integer minBranches) {
        return integrationRepository.findHighBranchActivityRepositories(teamNumber, season, minBranches);
    }

    @Override
    public GitHubIntegration updateCodebaseSize(Long integrationId, Long codebaseSize) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setCodebaseSize(codebaseSize);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public GitHubIntegration updateRepositoryStatistics(Long integrationId, Integer totalCommits, Integer totalPullRequests,
                                                       Integer openIssues, Integer closedIssues, Integer totalContributors,
                                                       Integer totalBranches, Integer totalReleases) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setTotalCommits(totalCommits);
        integration.setTotalPullRequests(totalPullRequests);
        integration.setOpenIssues(openIssues);
        integration.setClosedIssues(closedIssues);
        integration.setTotalContributors(totalContributors);
        integration.setTotalBranches(totalBranches);
        integration.setTotalReleases(totalReleases);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public Map<String, Object> analyzeRepositoryComplexity(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        Map<String, Object> analysis = new HashMap<>();
        
        // Repository complexity metrics
        analysis.put("codebaseSize", integration.getCodebaseSize());
        analysis.put("totalCommits", integration.getTotalCommits());
        analysis.put("totalBranches", integration.getTotalBranches());
        analysis.put("totalContributors", integration.getTotalContributors());
        analysis.put("repositoryMaturity", integration.getRepositoryMaturity());
        analysis.put("complexityScore", calculateComplexityScore(integration));
        analysis.put("analysisDate", LocalDateTime.now());
        
        return analysis;
    }

    // =========================================================================
    // ISSUE AND PROJECT MANAGEMENT
    // =========================================================================

    @Override
    public List<GitHubIntegration> findRepositoriesWithManyOpenIssues(Integer teamNumber, Integer season,
                                                                     Integer minOpenIssues) {
        return integrationRepository.findRepositoriesWithManyOpenIssues(teamNumber, season, minOpenIssues);
    }

    @Override
    public List<GitHubIntegration> findGoodIssueResolutionRepositories(Integer teamNumber, Integer season,
                                                                      Double minResolutionRate) {
        return integrationRepository.findGoodIssueResolutionRepositories(teamNumber, season, minResolutionRate);
    }

    @Override
    public List<GitHubIntegration> findRepositoriesWithIssueBacklog(Integer teamNumber, Integer season,
                                                                   Integer issueThreshold) {
        return integrationRepository.findRepositoriesWithIssueBacklog(teamNumber, season, issueThreshold);
    }

    @Override
    public GitHubIntegration updateIssueStatistics(Long integrationId, Integer openIssues, Integer closedIssues) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        integration.setOpenIssues(openIssues);
        integration.setClosedIssues(closedIssues);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public Map<String, Object> analyzeIssueManagementEffectiveness(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        Map<String, Object> analysis = new HashMap<>();
        
        int totalIssues = integration.getOpenIssues() + integration.getClosedIssues();
        double resolutionRate = totalIssues > 0 ? 
                (double) integration.getClosedIssues() / totalIssues * 100.0 : 0.0;
        
        analysis.put("openIssues", integration.getOpenIssues());
        analysis.put("closedIssues", integration.getClosedIssues());
        analysis.put("totalIssues", totalIssues);
        analysis.put("resolutionRate", resolutionRate);
        analysis.put("effectivenessRating", resolutionRate >= 80.0 ? "EXCELLENT" :
                                          resolutionRate >= 60.0 ? "GOOD" :
                                          resolutionRate >= 40.0 ? "AVERAGE" : "POOR");
        analysis.put("analysisDate", LocalDateTime.now());
        
        return analysis;
    }

    @Override
    public Map<String, Object> generateIssueResolutionReport(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        Map<String, Object> report = new HashMap<>();
        
        int totalOpenIssues = integrations.stream().mapToInt(GitHubIntegration::getOpenIssues).sum();
        int totalClosedIssues = integrations.stream().mapToInt(GitHubIntegration::getClosedIssues).sum();
        int totalIssues = totalOpenIssues + totalClosedIssues;
        
        double overallResolutionRate = totalIssues > 0 ? 
                (double) totalClosedIssues / totalIssues * 100.0 : 0.0;
        
        report.put("totalOpenIssues", totalOpenIssues);
        report.put("totalClosedIssues", totalClosedIssues);
        report.put("overallResolutionRate", overallResolutionRate);
        report.put("repositoriesWithIssueBacklog", findRepositoriesWithIssueBacklog(teamNumber, season, 5).size());
        report.put("goodResolutionRepositories", findGoodIssueResolutionRepositories(teamNumber, season, 80.0).size());
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================

    @Override
    public Map<GitHubIntegration.RepositoryType, Long> countByRepositoryType(Integer teamNumber, Integer season) {
        List<Object[]> results = integrationRepository.countByRepositoryType(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (GitHubIntegration.RepositoryType) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public Map<GitHubIntegration.IntegrationStatus, Long> countByIntegrationStatus(Integer teamNumber, Integer season) {
        List<Object[]> results = integrationRepository.countByIntegrationStatus(teamNumber, season);
        return results.stream().collect(Collectors.toMap(
            r -> (GitHubIntegration.IntegrationStatus) r[0],
            r -> (Long) r[1]
        ));
    }

    @Override
    public Long calculateTotalCommits(Integer teamNumber, Integer season) {
        return integrationRepository.calculateTotalCommits(teamNumber, season);
    }

    @Override
    public Long calculateTotalCodebaseSize(Integer teamNumber, Integer season) {
        return integrationRepository.calculateTotalCodebaseSize(teamNumber, season);
    }

    @Override
    public Double calculateAverageCodeReviewCoverage(Integer teamNumber, Integer season) {
        return integrationRepository.calculateAverageCodeReviewCoverage(teamNumber, season);
    }

    @Override
    public Double calculateAverageTestCoverage(Integer teamNumber, Integer season) {
        return integrationRepository.calculateAverageTestCoverage(teamNumber, season);
    }

    @Override
    public Double calculateAverageCommitFrequency(Integer teamNumber, Integer season) {
        return integrationRepository.calculateAverageCommitFrequency(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findMostProductiveRepositories(Integer teamNumber, Integer season) {
        return integrationRepository.findMostProductiveRepositories(teamNumber, season);
    }

    @Override
    public Map<String, Object> generateGitHubAnalyticsReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        // Basic statistics
        report.put("totalRepositories", countActiveIntegrations(teamNumber, season));
        report.put("repositoryTypes", countByRepositoryType(teamNumber, season));
        report.put("integrationStatuses", countByIntegrationStatus(teamNumber, season));
        
        // Development metrics
        report.put("totalCommits", calculateTotalCommits(teamNumber, season));
        report.put("totalCodebaseSize", calculateTotalCodebaseSize(teamNumber, season));
        report.put("averageCommitFrequency", calculateAverageCommitFrequency(teamNumber, season));
        
        // Quality metrics
        report.put("averageCodeReviewCoverage", calculateAverageCodeReviewCoverage(teamNumber, season));
        report.put("averageTestCoverage", calculateAverageTestCoverage(teamNumber, season));
        report.put("repositoriesWithBestPractices", findRepositoriesWithBestPractices(teamNumber, season).size());
        
        // Health status
        report.put("healthyIntegrations", findActiveAndHealthyIntegrations(teamNumber, season).size());
        report.put("integrationsWithErrors", findIntegrationsWithErrors(teamNumber, season).size());
        report.put("integrationsRequiringAttention", findIntegrationsRequiringAttention(teamNumber, season).size());
        
        // Team performance
        report.put("teamPerformanceScore", calculateTeamPerformanceScore(teamNumber, season));
        
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    public Map<String, Object> generateGitHubDashboardData(Integer teamNumber, Integer season) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Key metrics
        dashboard.put("totalRepositories", countActiveIntegrations(teamNumber, season));
        dashboard.put("activeIntegrations", findActiveAndHealthyIntegrations(teamNumber, season).size());
        dashboard.put("repositoriesRequiringAttention", findIntegrationsRequiringAttention(teamNumber, season).size());
        dashboard.put("teamPerformanceScore", calculateTeamPerformanceScore(teamNumber, season));
        
        // Recent activity
        dashboard.put("recentlyActiveRepositories", findRecentlyActiveRepositories(teamNumber, season, 7).size());
        dashboard.put("inactiveRepositories", findInactiveRepositories(teamNumber, season, 30).size());
        
        // Quality indicators
        dashboard.put("averageHealthScore", calculateAverageRepositoryHealthScore(teamNumber, season));
        dashboard.put("highQualityRepositories", findHighQualityRepositories(teamNumber, season, 80.0, 70.0).size());
        
        dashboard.put("lastUpdated", LocalDateTime.now());
        
        return dashboard;
    }

    @Override
    public Map<String, Object> generateDevelopmentVelocityReport(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        Map<String, Object> report = new HashMap<>();
        
        // Velocity metrics
        double avgCommitFrequency = calculateAverageCommitFrequency(teamNumber, season);
        List<GitHubIntegration> highVelocity = findHighVelocityRepositories(teamNumber, season, 50, 1.0);
        List<GitHubIntegration> mostActive = findMostActiveRepositories(teamNumber, season, 0.5);
        
        report.put("averageCommitFrequency", avgCommitFrequency);
        report.put("highVelocityRepositories", highVelocity.size());
        report.put("mostActiveRepositories", mostActive);
        report.put("totalCommits", calculateTotalCommits(teamNumber, season));
        report.put("velocityTrend", calculateVelocityTrend(integrations));
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    public Map<String, Object> generateCodeQualityAssessmentReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        // Quality metrics
        Double avgCodeReview = calculateAverageCodeReviewCoverage(teamNumber, season);
        Double avgTestCoverage = calculateAverageTestCoverage(teamNumber, season);
        List<GitHubIntegration> bestPractices = findRepositoriesWithBestPractices(teamNumber, season);
        List<GitHubIntegration> missingCICD = findRepositoriesMissingCICD(teamNumber, season);
        
        report.put("averageCodeReviewCoverage", avgCodeReview);
        report.put("averageTestCoverage", avgTestCoverage);
        report.put("repositoriesWithBestPractices", bestPractices.size());
        report.put("repositoriesMissingCICD", missingCICD.size());
        report.put("overallQualityScore", calculateOverallQualityScore(teamNumber, season));
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================

    @Override
    public List<GitHubIntegration> searchByRepositoryName(Integer teamNumber, Integer season, String searchTerm) {
        return integrationRepository.searchByRepositoryName(teamNumber, season, searchTerm);
    }

    @Override
    public List<GitHubIntegration> searchByDescription(Integer teamNumber, Integer season, String searchTerm) {
        return integrationRepository.searchByDescription(teamNumber, season, searchTerm);
    }

    @Override
    public List<GitHubIntegration> fullTextSearch(Integer teamNumber, Integer season, String searchTerm) {
        return integrationRepository.fullTextSearch(teamNumber, season, searchTerm);
    }

    @Override
    public List<GitHubIntegration> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria) {
        // This would implement complex search logic based on multiple criteria
        // For now, return all active integrations
        return findActiveIntegrations(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findByOrganization(String organizationName) {
        return integrationRepository.findByOrganizationNameAndIsActiveTrue(organizationName);
    }

    @Override
    public List<GitHubIntegration> findByConfiguredBy(Integer teamNumber, Integer season, Long teamMemberId) {
        return integrationRepository.findByConfiguredBy(teamNumber, season, teamMemberId);
    }

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    @Override
    public List<GitHubIntegration> findMultiSeasonIntegrations(Integer teamNumber, List<Integer> seasons) {
        return integrationRepository.findMultiSeasonIntegrations(teamNumber, seasons);
    }

    @Override
    public List<GitHubIntegration> findRepositoryEvolution(Integer teamNumber, String repositoryName) {
        return integrationRepository.findRepositoryEvolution(teamNumber, repositoryName);
    }

    @Override
    public Map<String, Object> compareGitHubActivityAcrossSeasons(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> comparison = new HashMap<>();
        
        for (Integer season : seasons) {
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("totalRepositories", countActiveIntegrations(teamNumber, season));
            seasonData.put("totalCommits", calculateTotalCommits(teamNumber, season));
            seasonData.put("averageCommitFrequency", calculateAverageCommitFrequency(teamNumber, season));
            seasonData.put("teamPerformanceScore", calculateTeamPerformanceScore(teamNumber, season));
            comparison.put("season" + season, seasonData);
        }
        
        return comparison;
    }

    @Override
    public Map<String, Object> analyzeTeamDevelopmentEvolution(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> evolution = new HashMap<>();
        
        evolution.put("seasonComparison", compareGitHubActivityAcrossSeasons(teamNumber, seasons));
        evolution.put("developmentTrends", calculateDevelopmentTrends(teamNumber, seasons));
        evolution.put("improvementAreas", identifyImprovementAreas(teamNumber, seasons));
        
        return evolution;
    }

    @Override
    public Map<String, Object> generateMultiSeasonComparisonReport(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("teamNumber", teamNumber);
        report.put("seasonsAnalyzed", seasons);
        report.put("activityComparison", compareGitHubActivityAcrossSeasons(teamNumber, seasons));
        report.put("evolutionAnalysis", analyzeTeamDevelopmentEvolution(teamNumber, seasons));
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public Long countActiveIntegrations(Integer teamNumber, Integer season) {
        return integrationRepository.countByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> findAllActiveIntegrations(Integer teamNumber, Integer season) {
        return integrationRepository.findAllActiveIntegrations(teamNumber, season);
    }

    @Override
    public List<GitHubIntegration> createBulkIntegrations(List<GitHubIntegration> integrations) {
        List<GitHubIntegration> created = new ArrayList<>();
        
        for (GitHubIntegration integration : integrations) {
            try {
                created.add(createIntegration(integration));
            } catch (Exception e) {
                // Log error and continue with next integration
                System.err.println("Failed to create integration: " + e.getMessage());
            }
        }
        
        return created;
    }

    @Override
    public List<GitHubIntegration> updateBulkIntegrations(Map<Long, GitHubIntegration> integrationUpdates) {
        List<GitHubIntegration> updated = new ArrayList<>();
        
        for (Map.Entry<Long, GitHubIntegration> entry : integrationUpdates.entrySet()) {
            try {
                updated.add(updateIntegration(entry.getKey(), entry.getValue()));
            } catch (Exception e) {
                // Log error and continue with next integration
                System.err.println("Failed to update integration " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        return updated;
    }

    @Override
    public void bulkArchiveIntegrations(List<Long> integrationIds, String reason) {
        for (Long integrationId : integrationIds) {
            try {
                archiveIntegration(integrationId, reason);
            } catch (Exception e) {
                // Log error and continue with next integration
                System.err.println("Failed to archive integration " + integrationId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void bulkSyncAllIntegrations(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        for (GitHubIntegration integration : integrations) {
            try {
                performFullSync(integration.getId());
            } catch (Exception e) {
                // Log error and continue with next integration
                System.err.println("Failed to sync integration " + integration.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateAllIntegrationHealthStatuses(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        for (GitHubIntegration integration : integrations) {
            try {
                updateHealthStatus(integration.getId());
            } catch (Exception e) {
                // Log error and continue
                System.err.println("Failed to update health status for integration " + integration.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void bulkUpdateRepositoryStatistics(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        for (GitHubIntegration integration : integrations) {
            try {
                updateRepositoryStatistics(integration.getId());
            } catch (Exception e) {
                // Log error and continue
                System.err.println("Failed to update statistics for integration " + integration.getId() + ": " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    @Override
    public List<GitHubIntegration> findArchivedIntegrations(Integer teamNumber, Integer season) {
        return integrationRepository.findByTeamNumberAndSeasonAndIsActiveFalse(teamNumber, season);
    }

    @Override
    public void permanentlyDeleteIntegration(Long integrationId) {
        integrationRepository.deleteById(integrationId);
    }

    @Override
    public List<GitHubIntegration> findCreatedInDateRange(Integer teamNumber, Integer season,
                                                         LocalDateTime startDate, LocalDateTime endDate) {
        return integrationRepository.findCreatedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<GitHubIntegration> findUpdatedInDateRange(Integer teamNumber, Integer season,
                                                         LocalDateTime startDate, LocalDateTime endDate) {
        return integrationRepository.findUpdatedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public Map<String, Object> exportIntegrationHistory(Integer teamNumber, Integer season) {
        Map<String, Object> history = new HashMap<>();
        
        List<GitHubIntegration> active = findActiveIntegrations(teamNumber, season);
        List<GitHubIntegration> archived = findArchivedIntegrations(teamNumber, season);
        
        history.put("activeIntegrations", active);
        history.put("archivedIntegrations", archived);
        history.put("totalCount", active.size() + archived.size());
        history.put("exportDate", LocalDateTime.now());
        
        return history;
    }

    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================

    @Override
    public Map<String, Object> exportToExternalFormat(Long integrationId, String format) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("repositoryName", integration.getRepositoryName());
        exportData.put("repositoryUrl", integration.getRepositoryUrl());
        exportData.put("repositoryType", integration.getRepositoryType());
        exportData.put("totalCommits", integration.getTotalCommits());
        exportData.put("totalContributors", integration.getTotalContributors());
        exportData.put("healthScore", integration.getRepositoryHealthScore());
        exportData.put("exportFormat", format);
        exportData.put("exportDate", LocalDateTime.now());
        
        return exportData;
    }

    @Override
    public GitHubIntegration importFromExternalSource(Map<String, Object> integrationData, String sourceType) {
        GitHubIntegration integration = new GitHubIntegration();
        
        // Map external data to internal structure
        integration.setRepositoryName((String) integrationData.get("repositoryName"));
        integration.setRepositoryUrl((String) integrationData.get("repositoryUrl"));
        integration.setDescription((String) integrationData.get("description"));
        
        if (integrationData.get("totalCommits") != null) {
            integration.setTotalCommits(((Number) integrationData.get("totalCommits")).intValue());
        }
        
        return createIntegration(integration);
    }

    @Override
    public GitHubIntegration syncWithExternalTools(Long integrationId, String toolType) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Placeholder for external tool integration
        integration.setLastSyncTime(LocalDateTime.now());
        integration.setLastSyncStatus("SYNCED_WITH_" + toolType);
        integration.setUpdatedAt(LocalDateTime.now());
        
        return integrationRepository.save(integration);
    }

    @Override
    public Map<String, Object> generateIntegrationBackup(Integer teamNumber, Integer season) {
        Map<String, Object> backup = new HashMap<>();
        
        List<GitHubIntegration> integrations = findAllActiveIntegrations(teamNumber, season);
        
        backup.put("teamNumber", teamNumber);
        backup.put("season", season);
        backup.put("integrations", integrations);
        backup.put("backupDate", LocalDateTime.now());
        backup.put("version", "1.0");
        
        return backup;
    }

    @Override
    public List<GitHubIntegration> restoreFromBackup(Map<String, Object> backupData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> integrationData = (List<Map<String, Object>>) backupData.get("integrations");
        
        List<GitHubIntegration> restored = new ArrayList<>();
        
        for (Map<String, Object> data : integrationData) {
            try {
                GitHubIntegration integration = importFromExternalSource(data, "BACKUP");
                restored.add(integration);
            } catch (Exception e) {
                // Log error and continue
                System.err.println("Failed to restore integration: " + e.getMessage());
            }
        }
        
        return restored;
    }

    // =========================================================================
    // WEBHOOK AND REAL-TIME INTEGRATION
    // =========================================================================

    @Override
    public void processWebhookPayload(String payload, String eventType) {
        // This would implement webhook payload processing
        // Parse payload and route to appropriate handler
        
        try {
            // Parse JSON payload (would use actual JSON library)
            // Determine integration based on repository info
            // Route to specific event handler
            
            System.out.println("Processing webhook: " + eventType);
            
        } catch (Exception e) {
            System.err.println("Failed to process webhook: " + e.getMessage());
        }
    }

    @Override
    public void handleCommitWebhook(Long integrationId, Map<String, Object> commitData) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Update commit-related metrics from webhook data
        integration.setTotalCommits(integration.getTotalCommits() + 1);
        integration.setLastCommitSync(LocalDateTime.now());
        integration.setUpdatedAt(LocalDateTime.now());
        
        integrationRepository.save(integration);
    }

    @Override
    public void handlePullRequestWebhook(Long integrationId, Map<String, Object> pullRequestData) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Update PR-related metrics from webhook data
        String action = (String) pullRequestData.get("action");
        if ("opened".equals(action)) {
            integration.setTotalPullRequests(integration.getTotalPullRequests() + 1);
        }
        
        integration.setLastPullRequestSync(LocalDateTime.now());
        integration.setUpdatedAt(LocalDateTime.now());
        
        integrationRepository.save(integration);
    }

    @Override
    public void handleIssueWebhook(Long integrationId, Map<String, Object> issueData) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Update issue-related metrics from webhook data
        String action = (String) issueData.get("action");
        if ("opened".equals(action)) {
            integration.setOpenIssues(integration.getOpenIssues() + 1);
        } else if ("closed".equals(action)) {
            integration.setOpenIssues(Math.max(0, integration.getOpenIssues() - 1));
            integration.setClosedIssues(integration.getClosedIssues() + 1);
        }
        
        integration.setLastIssueSync(LocalDateTime.now());
        integration.setUpdatedAt(LocalDateTime.now());
        
        integrationRepository.save(integration);
    }

    @Override
    public void handleReleaseWebhook(Long integrationId, Map<String, Object> releaseData) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // Update release-related metrics from webhook data
        String action = (String) releaseData.get("action");
        if ("published".equals(action)) {
            integration.setTotalReleases(integration.getTotalReleases() + 1);
        }
        
        integration.setUpdatedAt(LocalDateTime.now());
        
        integrationRepository.save(integration);
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        // This would implement HMAC SHA256 signature validation
        // For now, return true for demonstration
        return signature != null && !signature.isEmpty();
    }

    @Override
    public List<GitHubIntegration> findWithWebhookConfiguration(Integer teamNumber, Integer season) {
        return integrationRepository.findWithWebhookConfiguration(teamNumber, season);
    }

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    @Override
    public List<String> validateIntegration(GitHubIntegration integration) {
        List<String> errors = new ArrayList<>();
        
        if (integration.getTeamNumber() == null) {
            errors.add("Team number is required");
        }
        if (integration.getSeason() == null) {
            errors.add("Season is required");
        }
        if (integration.getRepositoryName() == null || integration.getRepositoryName().trim().isEmpty()) {
            errors.add("Repository name is required");
        }
        if (integration.getRepositoryUrl() == null || integration.getRepositoryUrl().trim().isEmpty()) {
            errors.add("Repository URL is required");
        }
        if (integration.getRepositoryType() == null) {
            errors.add("Repository type is required");
        }
        
        // Check for duplicate repository URL
        if (integration.getRepositoryUrl() != null) {
            Optional<GitHubIntegration> existing = findByRepositoryUrl(integration.getRepositoryUrl());
            if (existing.isPresent() && !existing.get().getId().equals(integration.getId())) {
                errors.add("Repository URL already exists in another integration");
            }
        }
        
        return errors;
    }

    @Override
    public boolean validateRepositoryUrl(String repositoryUrl) {
        // This would implement URL validation and GitHub API accessibility check
        return repositoryUrl != null && 
               (repositoryUrl.startsWith("https://github.com/") || repositoryUrl.startsWith("git@github.com:"));
    }

    @Override
    public boolean validateUserPermissions(Long integrationId, Long userId, String operation) {
        // Placeholder for permission validation
        // In a full implementation, this would check user roles and permissions
        return true;
    }

    @Override
    public Map<String, Object> checkIntegrationDataQuality(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        Map<String, Object> quality = new HashMap<>();
        
        int qualityScore = 0;
        List<String> issues = new ArrayList<>();
        
        // Check configuration completeness
        if (integration.getAccessToken() != null && !integration.getAccessToken().isEmpty()) {
            qualityScore += 25;
        } else {
            issues.add("Missing access token");
        }
        
        if (integration.getWebhookUrl() != null && !integration.getWebhookUrl().isEmpty()) {
            qualityScore += 20;
        } else {
            issues.add("No webhook configured");
        }
        
        if (integration.getTeamMembers().size() > 0) {
            qualityScore += 20;
        } else {
            issues.add("No team members configured");
        }
        
        if (integration.getLastSyncTime() != null && 
            integration.getLastSyncTime().isAfter(LocalDateTime.now().minusHours(24))) {
            qualityScore += 20;
        } else {
            issues.add("Sync is stale or never performed");
        }
        
        if (integration.followsBestPractices()) {
            qualityScore += 15;
        } else {
            issues.add("Repository missing best practices");
        }
        
        quality.put("qualityScore", qualityScore);
        quality.put("issues", issues);
        quality.put("qualityLevel", qualityScore >= 80 ? "HIGH" : qualityScore >= 60 ? "MEDIUM" : "LOW");
        
        return quality;
    }

    @Override
    public List<String> suggestIntegrationImprovements(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        List<String> suggestions = new ArrayList<>();
        
        if (integration.getAccessToken() == null || integration.getAccessToken().isEmpty()) {
            suggestions.add("Configure access token for API access");
        }
        
        if (integration.getWebhookUrl() == null || integration.getWebhookUrl().isEmpty()) {
            suggestions.add("Set up webhook for real-time updates");
        }
        
        if (!integration.getHasCodeReviewRequired()) {
            suggestions.add("Enable required code reviews for better quality");
        }
        
        if (!integration.getHasContinuousIntegration()) {
            suggestions.add("Set up continuous integration for automated testing");
        }
        
        if (integration.getCodeReviewCoverage() < 80.0) {
            suggestions.add("Increase code review coverage to improve quality");
        }
        
        if (integration.getTeamMembers().isEmpty()) {
            suggestions.add("Add team members to track individual contributions");
        }
        
        return suggestions;
    }

    @Override
    public boolean validateGitHubApiAccess(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        // This would implement actual GitHub API access validation
        // For now, check if access token is present
        return integration.getAccessToken() != null && !integration.getAccessToken().isEmpty();
    }

    @Override
    public Map<String, Object> checkRepositoryPermissions(Long integrationId) {
        GitHubIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("GitHub integration not found: " + integrationId));
        
        Map<String, Object> permissions = new HashMap<>();
        
        // This would implement actual permission checking via GitHub API
        // For now, return simulated data
        permissions.put("canRead", true);
        permissions.put("canWrite", integration.getAccessToken() != null);
        permissions.put("canAdmin", false);
        permissions.put("canCreateWebhooks", integration.getAccessToken() != null);
        permissions.put("checkedAt", LocalDateTime.now());
        
        return permissions;
    }

    @Override
    public boolean validateConfigurationCompleteness(Long integrationId) {
        List<String> issues = validateIntegrationConfiguration(integrationId);
        return issues.isEmpty();
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private GitHubIntegration initializeMetrics(GitHubIntegration integration) {
        // Initialize default metric values
        if (integration.getTotalCommits() == null) integration.setTotalCommits(0);
        if (integration.getTotalPullRequests() == null) integration.setTotalPullRequests(0);
        if (integration.getOpenIssues() == null) integration.setOpenIssues(0);
        if (integration.getClosedIssues() == null) integration.setClosedIssues(0);
        if (integration.getTotalContributors() == null) integration.setTotalContributors(0);
        if (integration.getTotalBranches() == null) integration.setTotalBranches(1); // At least main branch
        if (integration.getTotalReleases() == null) integration.setTotalReleases(0);
        if (integration.getCodebaseSize() == null) integration.setCodebaseSize(0L);
        if (integration.getCommitFrequency() == null) integration.setCommitFrequency(0.0);
        if (integration.getAveragePullRequestSize() == null) integration.setAveragePullRequestSize(0.0);
        if (integration.getCodeReviewCoverage() == null) integration.setCodeReviewCoverage(0.0);
        if (integration.getTestCoverage() == null) integration.setTestCoverage(0.0);
        if (integration.getBugsFound() == null) integration.setBugsFound(0);
        if (integration.getBugsFixed() == null) integration.setBugsFixed(0);
        if (integration.getActiveStudentDevelopers() == null) integration.setActiveStudentDevelopers(0);
        if (integration.getActiveMentorDevelopers() == null) integration.setActiveMentorDevelopers(0);
        if (integration.getStudentContributionPercentage() == null) integration.setStudentContributionPercentage(0.0);
        if (integration.getMentorContributionPercentage() == null) integration.setMentorContributionPercentage(0.0);
        
        return integration;
    }

    private void updateIntegrationFields(GitHubIntegration existing, GitHubIntegration update) {
        if (update.getRepositoryName() != null) {
            existing.setRepositoryName(update.getRepositoryName());
        }
        if (update.getRepositoryUrl() != null) {
            existing.setRepositoryUrl(update.getRepositoryUrl());
        }
        if (update.getOrganizationName() != null) {
            existing.setOrganizationName(update.getOrganizationName());
        }
        if (update.getOwnerUsername() != null) {
            existing.setOwnerUsername(update.getOwnerUsername());
        }
        if (update.getRepositoryType() != null) {
            existing.setRepositoryType(update.getRepositoryType());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getDefaultBranch() != null) {
            existing.setDefaultBranch(update.getDefaultBranch());
        }
    }

    private boolean hasUrlOrTokenChanged(GitHubIntegration existing, GitHubIntegration update) {
        return (update.getRepositoryUrl() != null && !update.getRepositoryUrl().equals(existing.getRepositoryUrl())) ||
               (update.getAccessToken() != null && !update.getAccessToken().equals(existing.getAccessToken()));
    }

    private String encryptAccessToken(String accessToken) {
        // In production, this would implement proper encryption
        // For now, return as-is
        return accessToken;
    }

    private Double calculateCommitFrequency(GitHubIntegration integration) {
        // Calculate commits per day based on total commits and repository age
        if (integration.getCreatedAt() == null) {
            return 0.0;
        }
        
        long daysSinceCreated = ChronoUnit.DAYS.between(integration.getCreatedAt(), LocalDateTime.now());
        if (daysSinceCreated <= 0) {
            return 0.0;
        }
        
        return (double) integration.getTotalCommits() / daysSinceCreated;
    }

    private Double calculateAveragePullRequestSize(GitHubIntegration integration) {
        // This would calculate based on actual PR data
        // For now, return a reasonable estimate
        if (integration.getTotalPullRequests() == 0) {
            return 0.0;
        }
        
        // Estimate based on codebase size and PRs
        return (double) integration.getCodebaseSize() / integration.getTotalPullRequests() / 10;
    }

    private int generateRandomCommitCount() {
        // Simulate new commits (0-5)
        return (int) (Math.random() * 6);
    }

    private int generateRandomPRCount() {
        // Simulate new PRs (0-2)
        return (int) (Math.random() * 3);
    }

    private Map<String, Integer> generateRandomIssueData() {
        Map<String, Integer> issues = new HashMap<>();
        issues.put("open", (int) (Math.random() * 10));
        issues.put("closed", (int) (Math.random() * 20));
        return issues;
    }

    private int generateRandomReleaseCount() {
        // Simulate new releases (0-1)
        return Math.random() > 0.8 ? 1 : 0;
    }

    private int generateRandomBranchCount() {
        // Simulate new branches (0-3)
        return (int) (Math.random() * 4);
    }

    private long generateRandomCodebaseGrowth() {
        // Simulate codebase growth (0-1000 lines)
        return (long) (Math.random() * 1001);
    }

    private double calculateComplexityScore(GitHubIntegration integration) {
        double score = 0.0;
        
        // Factor in various complexity indicators
        score += Math.min(integration.getCodebaseSize() / 10000.0 * 30, 30); // Max 30 points for size
        score += Math.min(integration.getTotalContributors() * 5, 25); // Max 25 points for contributors
        score += Math.min(integration.getTotalBranches() * 2, 20); // Max 20 points for branches
        score += Math.min(integration.getTotalCommits() / 100.0 * 25, 25); // Max 25 points for commits
        
        return Math.min(score, 100.0);
    }

    private double calculateAverageRepositoryHealthScore(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        if (integrations.isEmpty()) {
            return 0.0;
        }
        
        return integrations.stream()
                .mapToDouble(GitHubIntegration::getRepositoryHealthScore)
                .average()
                .orElse(0.0);
    }

    private String calculateVelocityTrend(List<GitHubIntegration> integrations) {
        // This would implement trend calculation over time
        // For now, return simple assessment
        double avgFrequency = integrations.stream()
                .mapToDouble(GitHubIntegration::getCommitFrequency)
                .average()
                .orElse(0.0);
        
        if (avgFrequency >= 2.0) return "HIGH";
        if (avgFrequency >= 1.0) return "MODERATE";
        if (avgFrequency >= 0.5) return "LOW";
        return "MINIMAL";
    }

    private double calculateOverallQualityScore(Integer teamNumber, Integer season) {
        List<GitHubIntegration> integrations = findActiveIntegrations(teamNumber, season);
        
        if (integrations.isEmpty()) {
            return 0.0;
        }
        
        return integrations.stream()
                .mapToDouble(GitHubIntegration::getRepositoryHealthScore)
                .average()
                .orElse(0.0);
    }

    private Map<String, Object> calculateDevelopmentTrends(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> trends = new HashMap<>();
        
        // This would implement sophisticated trend analysis
        // For now, return basic metrics
        for (Integer season : seasons) {
            Double avgFrequency = calculateAverageCommitFrequency(teamNumber, season);
            trends.put("commitFrequency_" + season, avgFrequency);
        }
        
        return trends;
    }

    private List<String> identifyImprovementAreas(Integer teamNumber, List<Integer> seasons) {
        List<String> areas = new ArrayList<>();
        
        // Analyze trends and identify areas needing improvement
        // This would be more sophisticated in a real implementation
        
        Double latestFrequency = calculateAverageCommitFrequency(teamNumber, seasons.get(seasons.size() - 1));
        if (latestFrequency < 1.0) {
            areas.add("Increase development activity and commit frequency");
        }
        
        Double latestCoverage = calculateAverageCodeReviewCoverage(teamNumber, seasons.get(seasons.size() - 1));
        if (latestCoverage < 80.0) {
            areas.add("Improve code review coverage and quality processes");
        }
        
        return areas;
    }
}