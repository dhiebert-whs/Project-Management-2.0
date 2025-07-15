## Implementation Timeline and Deployment Strategy

### **Phase 4 Implementation Schedule**

#### **Week 1: Infrastructure Foundation**
- **Days 1-2**: Oracle Cloud Always Free account setup and Terraform infrastructure deployment
- **Days 3-4**: Container configuration and Docker multi-stage build implementation
- **Days 5-7**: Basic Spring Boot configuration and production database setup

#### **Week 2: Security and Compliance**
- **Days 1-3**: COPPA compliance service implementation and testing
- **Days 4-5**: TOTP multi-factor authentication service development
- **Days 6-7**: Security configuration and audit logging implementation

#### **Week 3: PWA and Offline Capabilities**
- **Days 1-3**: Service worker implementation and offline data management
- **Days 4-5**: Progressive Web App features and mobile optimization
- **Days 6-7**: Offline sync testing and conflict resolution

#### **Week 4: FRC-Specific Integrations**
- **Days 1-3**: FRC Events API integration and competition milestone automation
- **Days 4-5**: GitHub integration for programming teams
- **Days 6-7**: Workshop environment features (QR codes, voice input)

#### **Week 5: Monitoring and Analytics**
- **Days 1-3**: Application health monitoring and performance metrics
- **Days 4-5**: Advanced analytics service implementation
- **Days 6-7**: Automated reporting and alerting systems

#### **Week 6: Testing and Optimization**
- **Days 1-3**: End-to-end testing and performance optimization
- **Days 4-5**: Security auditing and COPPA compliance verification
- **Days 6-7**: Load testing and deployment rehearsal

#### **Week 7: Production Deployment**
- **Days 1-2**: Production deployment and monitoring setup
- **Days 3-4**: User acceptance testing and feedback incorporation
- **Days 5-7**: Documentation completion and team training

### **Success Metrics and Monitoring**

#### **Technical Performance Targets**
- **Response Time**: < 2 seconds for all page loads
- **Availability**: 99.5% uptime during build season
- **Offline Capability**: 7-day offline operation with full sync
- **Mobile Performance**: Lighthouse PWA score > 90

#### **User Experience Metrics**
- **Adoption Rate**: > 80% active team member usage within 2 weeks
- **Task Update Frequency**: Daily task updates during build season
- **Mobile Usage**: > 40% of interactions on mobile devices
- **User Satisfaction**: > 4.5/5 average user rating

#### **Security and Compliance Metrics**
- **COPPA Compliance**: 100% audit compliance for student data protection
- **Security Incidents**: Zero data breaches or unauthorized access
- **Authentication Success**: > 99% MFA authentication success rate
- **Audit Trail**: 100% coverage of student data access logging

#### **Business Impact Metrics**
- **Cost Savings**: $0 operational costs through Oracle Always Free tier
- **Team Efficiency**: 25% improvement in task completion velocity
- **Project Delivery**: On-time delivery of competition milestones
- **Collaboration**: 50% increase in cross-subteam task coordination

---

## Risk Mitigation and Contingency Planning

### **High-Priority Risks and Mitigation Strategies**

#### **1. Oracle Cloud Always Free Limitations**
**Risk**: Service limitations or policy changes affecting free tier
**Mitigation**:
- Implement resource monitoring and alerts for usage thresholds
- Prepare fallback deployment scripts for Fly.io ($5/month alternative)
- Design architecture to be cloud-agnostic with Docker containers
- Document migration procedures for emergency platform changes

#### **2. COPPA Compliance Violations**
**Risk**: Inadvertent collection or mishandling of student data
**Mitigation**:
- Implement automated compliance checks in all data collection points
- Regular compliance audits with documented procedures
- Legal review of all student-facing features
- Emergency data deletion procedures for compliance violations

#### **3. Performance Issues During Build Season**
**Risk**: High load during intensive 6-week build period
**Mitigation**:
- Implement comprehensive caching strategies (Redis, application-level)
- Load testing with simulated build season traffic patterns
- Auto-scaling configuration within Always Free limits
- Performance monitoring with automated alerting

#### **4. Offline Sync Conflicts**
**Risk**: Data conflicts from multiple offline users
**Mitigation**:
- Implement robust conflict resolution algorithms
- User training on offline best practices
- Real-time conflict detection and user notification
- Manual conflict resolution UI for complex cases

### **Contingency Plans**

#### **Infrastructure Failure Response**
1. **Immediate Response** (< 5 minutes):
   - Automated health checks trigger alerts
   - Backup instance activation on second Oracle region
   
2. **Short-term Recovery** (< 30 minutes):
   - Database backup restoration from automated daily backups
   - DNS failover to backup environment
   
3. **Long-term Recovery** (< 2 hours):
   - Full environment rebuild using Terraform scripts
   - Data synchronization and integrity verification

#### **Security Incident Response**
1. **Detection and Containment** (< 15 minutes):
   - Automated security monitoring triggers immediate alerts
   - Affected systems isolated from network access
   
2. **Assessment and Notification** (< 1 hour):
   - Impact assessment for student data exposure
   - COPPA-required notifications to parents and authorities
   
3. **Recovery and Prevention** (< 24 hours):
   - Security patches and system hardening
   - Comprehensive security audit and penetration testing

---

## Post-Deployment Support and Maintenance

### **Ongoing Maintenance Schedule**

#### **Daily Automated Tasks**
- **2:00 AM**: Automated database backups to Oracle Object Storage
- **2:30 AM**: Log rotation and cleanup of temporary files
- **3:00 AM**: System health checks and performance metrics collection
- **6:00 AM**: FRC Events API synchronization for competition updates

#### **Weekly Maintenance Tasks**
- **Sunday 2:00 AM**: Full system backup and disaster recovery testing
- **Monday 6:00 AM**: Security patch assessment and application
- **Wednesday**: Performance review and optimization analysis
- **Friday**: User feedback analysis and feature request prioritization

#### **Monthly Deep Maintenance**
- **First Saturday**: Comprehensive security audit and penetration testing
- **Second Saturday**: Capacity planning and resource optimization
- **Third Saturday**: COPPA compliance audit and documentation review
- **Fourth Saturday**: Disaster recovery drill and backup restoration testing

### **Support Structure**

#### **Tier 1: User Support**
- **Response Time**: < 4 hours during build season, < 24 hours off-season
- **Channels**: In-app help system, email support, knowledge base
- **Coverage**: Basic troubleshooting, account issues, feature guidance

#### **Tier 2: Technical Support**
- **Response Time**: < 2 hours for critical issues, < 8 hours for standard
- **Coverage**: Performance issues, integration problems, data synchronization
- **Escalation**: Security incidents, COPPA violations, system outages

#### **Tier 3: Development Support**
- **Response Time**: < 1 hour for critical security/compliance issues
- **Coverage**: Code-level debugging, infrastructure problems, emergency patches
- **On-call**: 24/7 during competition season (January-April)

### **Continuous Improvement Process**

#### **User Feedback Integration**
- **Weekly Feedback Reviews**: Analyze user feedback and feature requests
- **Monthly Feature Planning**: Prioritize enhancements based on user needs
- **Quarterly User Surveys**: Comprehensive satisfaction and needs assessment
- **Annual Feature Roadmap**: Long-term planning based on FRC season cycles

#### **Performance Optimization**
- **Real-time Monitoring**: Continuous performance metrics collection
- **Monthly Performance Reviews**: Identify bottlenecks and optimization opportunities
- **Quarterly Architecture Reviews**: Assess scalability and technology updates
- **Annual Technology Refresh**: Evaluate new technologies and frameworks

---

## Success Criteria and Project Completion

### **Phase 4 Completion Criteria**

#### **Technical Deliverables** ✅
- [ ] Oracle Cloud Always Free infrastructure fully operational
- [ ] Docker containerization with multi-stage builds implemented
- [ ] COPPA compliance service with full audit trail functionality
- [ ] TOTP multi-factor authentication for mentors operational
- [ ] Progressive Web App with 7-day offline capability
- [ ] FRC Events API integration with automated milestone creation
- [ ] GitHub integration for programming team workflows
- [ ] Comprehensive monitoring and alerting system
- [ ] Advanced analytics with predictive capabilities
- [ ] Load testing validation for 50+ concurrent users

#### **Security and Compliance** ✅
- [ ] COPPA compliance audit passed with 100% score
- [ ] Security penetration testing completed with no critical vulnerabilities
- [ ] Data encryption at rest and in transit verified
- [ ] Audit logging for all student data access functional
- [ ] Emergency data deletion procedures tested and documented

#### **User Experience** ✅
- [ ] Mobile-first responsive design implemented
- [ ] Offline functionality tested in workshop environment
- [ ] QR code attendance system operational
- [ ] Voice input capabilities for hands-free updates
- [ ] Real-time collaboration features functional

#### **Documentation and Training** ✅
- [ ] Comprehensive deployment documentation completed
- [ ] User training materials and videos created
- [ ] Administrative guides for mentors completed
- [ ] Emergency procedures and contact information documented
- [ ] COPPA compliance procedures documented and approved

### **Go-Live Readiness Checklist**

#### **Pre-Production Validation**
- [ ] All automated tests passing (unit, integration, end-to-end)
- [ ] Performance benchmarks met under simulated load
- [ ] Security scan results reviewed and approved
- [ ] COPPA compliance verification completed
- [ ] Backup and disaster recovery procedures tested

#### **Production Environment**
- [ ] Oracle Cloud infrastructure provisioned and secured
- [ ] SSL certificates installed and validated
- [ ] Domain name configured and DNS propagated
- [ ] Monitoring and alerting systems operational
- [ ] Support team trained and on-call schedule established

#### **User Preparation**
- [ ] Team training sessions completed
- [ ] User accounts created and MFA configured for mentors
- [ ] Data migration from existing systems completed
- [ ] User acceptance testing completed successfully
- [ ] Feedback incorporation and final adjustments made

### **Post-Launch Success Metrics (30-Day Review)**

#### **Adoption and Usage**
- **Target**: 80% of team members actively using the system within 14 days
- **Measurement**: Daily active users, feature usage analytics
- **Success Indicator**: Consistent daily usage during build season

#### **Performance and Reliability**
- **Target**: 99.5% uptime with < 2 second average response time
- **Measurement**: Automated monitoring dashboards
- **Success Indicator**: Zero critical outages, performance targets met

#### **Security and Compliance**
- **Target**: Zero security incidents or compliance violations
- **Measurement**: Security event logs, compliance audit reports
- **Success Indicator**: Clean security posture, COPPA compliance maintained

#### **User Satisfaction**
- **Target**: Average user satisfaction score > 4.5/5
- **Measurement**: In-app feedback, user surveys, support ticket analysis
- **Success Indicator**: Positive feedback trends, low support volume

---

## Conclusion

Phase 4 represents the culmination of the FRC Project Management System migration to a modern, scalable, and compliance-ready web application. This detailed implementation plan provides:

### **Key Achievements**
1. **Zero-Cost Production Deployment**: Oracle Cloud Always Free tier provides enterprise-grade hosting without ongoing operational costs
2. **COPPA Compliance**: Built-in student data protection meeting all regulatory requirements
3. **Mobile-First Experience**: Progressive Web App with offline capabilities optimized for workshop environments
4. **FRC-Specific Integration**: Seamless connection with FRC Events API and GitHub for comprehensive team workflow
5. **Advanced Analytics**: Predictive capabilities for project completion and team performance optimization

### **Long-Term Value**
- **Sustainable Operations**: Zero ongoing costs with enterprise-grade reliability
- **Regulatory Compliance**: Future-proof COPPA compliance with automated audit trails
- **Scalable Architecture**: Cloud-native design supporting multiple teams and seasons
- **Competitive Advantage**: Advanced analytics and predictive capabilities for strategic planning
- **Community Impact**: Open-source foundation enabling broader FRC community adoption

### **Migration Success Factors**
The success of this migration builds upon the strong foundation of the existing 95% complete service layer infrastructure. By preserving proven business logic and established patterns while modernizing the delivery platform, the project achieves maximum value with minimal risk.

**Total Estimated Effort**: 7 weeks
**Total Investment**: Time and expertise only (zero ongoing operational costs)
**Expected ROI**: Immediate productivity gains, long-term scalability, and regulatory compliance

This comprehensive Phase 4 implementation transforms the FRC Project Management System into a modern, collaborative platform that will serve FRC teams effectively for years to come while maintaining zero operational costs and full compliance with student data protection requirements.record FRCEvent(
    String eventKey,
    String name,
    String eventType,
    String venue,
    String city,
    String stateProv,
    String country,
    LocalDate dateStart,
    LocalDate dateEnd,
    int weekNumber
) {
    public FRCEventType eventType() {
        return FRCEventType.fromString(eventType);
    }
}

enum FRCEventType {
    REGIONAL, DISTRICT, DISTRICT_CHAMPIONSHIP, CHAMPIONSHIP, OFFSEASON;
    
    public static FRCEventType fromString(String type) {
        return switch (type.toLowerCase()) {
            case "regional" -> REGIONAL;
            case "district" -> DISTRICT;
            case "district_championship" -> DISTRICT_CHAMPIONSHIP;
            case "championship" -> CHAMPIONSHIP;
            default -> OFFSEASON;
        };
    }
}

record FRCMatch(
    String matchKey,
    String compLevel,
    int setNumber,
    int matchNumber,
    LocalDateTime scheduledTime,
    LocalDateTime actualTime,
    FRCAlliance redAlliance,
    FRCAlliance blueAlliance,
    FRCMatchResult result
) {}

record FRCAlliance(
    List<Integer> teamKeys,
    int score
) {}

record FRCMatchResult(
    String winningAlliance,
    int redScore,
    int blueScore
) {}

record FRCRankings(
    List<FRCTeamRanking> rankings
) {
    public FRCTeamRanking getTeamRanking(int teamNumber) {
        return rankings.stream()
            .filter(ranking -> ranking.teamNumber() == teamNumber)
            .findFirst()
            .orElse(null);
    }
}

record FRCTeamRanking(
    int teamNumber,
    int rank,
    double rankingScore,
    int wins,
    int losses,
    int ties
) {}

record FRCTeamPerformance(
    int teamNumber,
    int season,
    List<FRCTeamRanking> eventRankings
) {
    public double averageRank() {
        return eventRankings.stream()
            .mapToInt(FRCTeamRanking::rank)
            .average()
            .orElse(0.0);
    }
    
    public int totalWins() {
        return eventRankings.stream()
            .mapToInt(FRCTeamRanking::wins)
            .sum();
    }
    
    public int totalLosses() {
        return eventRankings.stream()
            .mapToInt(FRCTeamRanking::losses)
            .sum();
    }
}
```

### **GitHub Integration Service**

**File**: `src/main/java/org/frcpm/integration/GitHubIntegrationService.java`
```java
package org.frcpm.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class GitHubIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubIntegrationService.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    private final WebClient webClient;
    private final String githubToken;
    private final TaskService taskService;
    private final TeamMemberService teamMemberService;
    
    public GitHubIntegrationService(
            @Value("${frc-pm.integration.github.token:}") String githubToken,
            TaskService taskService,
            TeamMemberService teamMemberService) {
        
        this.githubToken = githubToken;
        this.taskService = taskService;
        this.teamMemberService = teamMemberService;
        
        this.webClient = WebClient.builder()
            .baseUrl(GITHUB_API_BASE)
            .defaultHeader("Authorization", "token " + githubToken)
            .defaultHeader("Accept", "application/vnd.github.v3+json")
            .build();
    }
    
    /**
     * Syncs programming tasks with GitHub issues
     */
    @Async
    public CompletableFuture<Void> syncProgrammingTasks(String repoOwner, String repoName, Long projectId) {
        if (githubToken == null || githubToken.isEmpty()) {
            logger.warn("GitHub integration disabled - no token configured");
            return CompletableFuture.completedFuture(null);
        }
        
        return getRepositoryIssues(repoOwner, repoName)
            .flatMapMany(Flux::fromIterable)
            .filter(issue -> !issue.isPullRequest()) // Exclude pull requests
            .flatMap(issue -> syncIssueWithTask(issue, projectId))
            .then()
            .toFuture();
    }
    
    /**
     * Links commits to tasks based on commit messages
     */
    @Async
    public CompletableFuture<Void> linkCommitsToTasks(String repoOwner, String repoName, Long projectId) {
        if (githubToken == null || githubToken.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return getRecentCommits(repoOwner, repoName, 50)
            .flatMapMany(Flux::fromIterable)
            .flatMap(commit -> linkCommitToTask(commit, projectId))
            .then()
            .toFuture();
    }
    
    /**
     * Creates GitHub issues for programming tasks
     */
    public Mono<GitHubIssue> createIssueForTask(String repoOwner, String repoName, Task task) {
        if (githubToken == null || githubToken.isEmpty()) {
            return Mono.empty();
        }
        
        GitHubIssueRequest issueRequest = new GitHubIssueRequest(
            task.getTitle(),
            generateIssueDescription(task),
            List.of("programming", "frc", "task-" + task.getId()),
            null // No assignee initially
        );
        
        return webClient.post()
            .uri("/repos/{owner}/{repo}/issues", repoOwner, repoName)
            .bodyValue(issueRequest)
            .retrieve()
            .bodyToMono(GitHubIssue.class)
            .doOnSuccess(issue -> {
                // Update task with GitHub issue number
                task.setGithubIssueNumber(issue.number());
                task.setGithubIssueUrl(issue.htmlUrl());
                taskService.save(task);
                logger.info("Created GitHub issue #{} for task {}", issue.number(), task.getId());
            })
            .doOnError(error -> logger.error("Failed to create GitHub issue for task {}: {}", 
                task.getId(), error.getMessage()));
    }
    
    /**
     * Updates GitHub issue when task status changes
     */
    public Mono<Void> updateIssueForTask(String repoOwner, String repoName, Task task) {
        if (task.getGithubIssueNumber() == null) {
            return Mono.empty();
        }
        
        String state = task.isCompleted() ? "closed" : "open";
        Map<String, Object> updateRequest = Map.of(
            "state", state,
            "body", generateIssueDescription(task)
        );
        
        return webClient.patch()
            .uri("/repos/{owner}/{repo}/issues/{issue_number}", 
                repoOwner, repoName, task.getGithubIssueNumber())
            .bodyValue(updateRequest)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> logger.info("Updated GitHub issue #{} for task {}", 
                task.getGithubIssueNumber(), task.getId()))
            .doOnError(error -> logger.error("Failed to update GitHub issue for task {}: {}", 
                task.getId(), error.getMessage()));
    }
    
    /**
     * Gets pull requests and links them to tasks
     */
    public Mono<List<GitHubPullRequest>> getPullRequestsForTask(String repoOwner, String repoName, Long taskId) {
        return getRepositoryPullRequests(repoOwner, repoName)
            .map(prs -> prs.stream()
                .filter(pr -> pr.title().contains("task-" + taskId) || 
                             pr.body().contains("task-" + taskId))
                .toList());
    }
    
    /**
     * Handles GitHub webhook events
     */
    public void handleWebhookEvent(String eventType, Map<String, Object> payload) {
        switch (eventType) {
            case "issues" -> handleIssueEvent(payload);
            case "pull_request" -> handlePullRequestEvent(payload);
            case "push" -> handlePushEvent(payload);
            default -> logger.debug("Unhandled GitHub webhook event: {}", eventType);
        }
    }
    
    private Mono<List<GitHubIssue>> getRepositoryIssues(String owner, String repo) {
        return webClient.get()
            .uri("/repos/{owner}/{repo}/issues?state=all&sort=updated&per_page=100", owner, repo)
            .retrieve()
            .bodyToFlux(GitHubIssue.class)
            .collectList()
            .doOnSuccess(issues -> logger.info("Retrieved {} issues from {}/{}", 
                issues.size(), owner, repo))
            .doOnError(error -> logger.error("Failed to retrieve issues from {}/{}: {}", 
                owner, repo, error.getMessage()));
    }
    
    private Mono<List<GitHubPullRequest>> getRepositoryPullRequests(String owner, String repo) {
        return webClient.get()
            .uri("/repos/{owner}/{repo}/pulls?state=all&sort=updated&per_page=50", owner, repo)
            .retrieve()
            .bodyToFlux(GitHubPullRequest.class)
            .collectList();
    }
    
    private Mono<List<GitHubCommit>> getRecentCommits(String owner, String repo, int count) {
        return webClient.get()
            .uri("/repos/{owner}/{repo}/commits?per_page={count}", owner, repo, count)
            .retrieve()
            .bodyToFlux(GitHubCommit.class)
            .collectList();
    }
    
    private Mono<Void> syncIssueWithTask(GitHubIssue issue, Long projectId) {
        return Mono.fromCallable(() -> {
            // Check if task already exists for this issue
            List<Task> existingTasks = taskService.findByGithubIssueNumber(issue.number());
            
            if (existingTasks.isEmpty()) {
                // Create new task from issue
                Task task = new Task();
                task.setTitle(issue.title());
                task.setDescription(issue.body());
                task.setGithubIssueNumber(issue.number());
                task.setGithubIssueUrl(issue.htmlUrl());
                task.setCompleted(issue.state().equals("closed"));
                task.setProgress(issue.state().equals("closed") ? 100 : 0);
                
                // Try to assign to team member based on GitHub username
                if (issue.assignee() != null) {
                    TeamMember assignee = teamMemberService.findByGithubUsername(issue.assignee().login());
                    if (assignee != null) {
                        task.getAssignedTo().add(assignee);
                    }
                }
                
                taskService.save(task);
                logger.info("Created task from GitHub issue #{}: {}", issue.number(), issue.title());
            } else {
                // Update existing task
                Task task = existingTasks.get(0);
                task.setTitle(issue.title());
                task.setDescription(issue.body());
                task.setCompleted(issue.state().equals("closed"));
                task.setProgress(issue.state().equals("closed") ? 100 : 
                    (task.getProgress() == 0 && !issue.state().equals("closed")) ? 10 : task.getProgress());
                
                taskService.save(task);
                logger.info("Updated task from GitHub issue #{}: {}", issue.number(), issue.title());
            }
            
            return null;
        }).then();
    }
    
    private Mono<Void> linkCommitToTask(GitHubCommit commit, Long projectId) {
        return Mono.fromCallable(() -> {
            // Parse commit message for task references (e.g., "fixes task-123" or "#123")
            String message = commit.message();
            List<Long> taskIds = extractTaskIdsFromCommitMessage(message);
            
            for (Long taskId : taskIds) {
                Task task = taskService.findById(taskId).orElse(null);
                if (task != null && task.getProject().getId().equals(projectId)) {
                    // Add commit reference to task
                    String commitRef = String.format("%s: %s (%s)", 
                        commit.sha().substring(0, 7), 
                        commit.message().split("\n")[0], 
                        commit.author().name());
                    
                    task.addCommitReference(commitRef);
                    taskService.save(task);
                    
                    logger.info("Linked commit {} to task {}", commit.sha().substring(0, 7), taskId);
                }
            }
            
            return null;
        }).then();
    }
    
    private List<Long> extractTaskIdsFromCommitMessage(String message) {
        // Extract task IDs from patterns like "task-123", "#123", "fixes #123", etc.
        return java.util.regex.Pattern.compile("(?:task-|#)(\\d+)")
            .matcher(message.toLowerCase())
            .results()
            .map(match -> Long.parseLong(match.group(1)))
            .toList();
    }
    
    private String generateIssueDescription(Task task) {
        StringBuilder description = new StringBuilder();
        description.append("**FRC Project Management Task**\n\n");
        
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            description.append(task.getDescription()).append("\n\n");
        }
        
        description.append("**Details:**\n");
        description.append("- Task ID: ").append(task.getId()).append("\n");
        description.append("- Priority: ").append(task.getPriority()).append("\n");
        description.append("- Estimated Hours: ").append(task.getEstimatedHours()).append("\n");
        
        if (task.getStartDate() != null) {
            description.append("- Start Date: ").append(task.getStartDate()).append("\n");
        }
        
        if (task.getEndDate() != null) {
            description.append("- Due Date: ").append(task.getEndDate()).append("\n");
        }
        
        if (!task.getAssignedTo().isEmpty()) {
            description.append("- Assigned To: ");
            description.append(task.getAssignedTo().stream()
                .map(TeamMember::getFullName)
                .collect(java.util.stream.Collectors.joining(", ")));
            description.append("\n");
        }
        
        description.append("\n---\n");
        description.append("*This issue was created automatically from FRC Project Management System*");
        
        return description.toString();
    }
    
    private void handleIssueEvent(Map<String, Object> payload) {
        // Handle GitHub issue events (opened, closed, assigned, etc.)
        String action = (String) payload.get("action");
        Map<String, Object> issue = (Map<String, Object>) payload.get("issue");
        
        if (issue != null) {
            Integer issueNumber = (Integer) issue.get("number");
            String state = (String) issue.get("state");
            
            // Find corresponding task and update it
            List<Task> tasks = taskService.findByGithubIssueNumber(issueNumber);
            for (Task task : tasks) {
                if ("closed".equals(state) && !task.isCompleted()) {
                    task.setCompleted(true);
                    task.setProgress(100);
                    taskService.save(task);
                    logger.info("Marked task {} as completed due to GitHub issue closure", task.getId());
                } else if ("reopened".equals(action) && task.isCompleted()) {
                    task.setCompleted(false);
                    task.setProgress(Math.max(10, task.getProgress()));
                    taskService.save(task);
                    logger.info("Reopened task {} due to GitHub issue reopening", task.getId());
                }
            }
        }
    }
    
    private void handlePullRequestEvent(Map<String, Object> payload) {
        // Handle pull request events
        String action = (String) payload.get("action");
        Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
        
        if (pullRequest != null && "closed".equals(action)) {
            Boolean merged = (Boolean) pullRequest.get("merged");
            String title = (String) pullRequest.get("title");
            String body = (String) pullRequest.get("body");
            
            if (Boolean.TRUE.equals(merged)) {
                // Extract task IDs and update progress
                List<Long> taskIds = extractTaskIdsFromCommitMessage(title + " " + body);
                for (Long taskId : taskIds) {
                    Task task = taskService.findById(taskId).orElse(null);
                    if (task != null && !task.isCompleted()) {
                        task.setProgress(Math.min(100, task.getProgress() + 25)); // Boost progress
                        taskService.save(task);
                        logger.info("Updated task {} progress due to merged PR", taskId);
                    }
                }
            }
        }
    }
    
    private void handlePushEvent(Map<String, Object> payload) {
        // Handle push events - could trigger task updates based on commits
        List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");
        
        if (commits != null) {
            for (Map<String, Object> commitData : commits) {
                String message = (String) commitData.get("message");
                String sha = (String) commitData.get("id");
                
                List<Long> taskIds = extractTaskIdsFromCommitMessage(message);
                for (Long taskId : taskIds) {
                    Task task = taskService.findById(taskId).orElse(null);
                    if (task != null) {
                        // Add commit reference
                        String commitRef = String.format("%s: %s", 
                            sha.substring(0, 7), 
                            message.split("\n")[0]);
                        task.addCommitReference(commitRef);
                        taskService.save(task);
                    }
                }
            }
        }
    }
}

// Supporting data classes
record GitHubIssue(
    int number,
    String title,
    String body,
    String state,
    GitHubUser assignee,
    String htmlUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<String> labels
) {
    public boolean isPullRequest() {
        return htmlUrl.contains("/pull/");
    }
}

record GitHubIssueRequest(
    String title,
    String body,
    List<String> labels,
    String assignee
) {}

record GitHubUser(
    String login,
    String name,
    String email
) {}

record GitHubPullRequest(
    int number,
    String title,
    String body,
    String state,
    boolean merged,
    GitHubUser user,
    String htmlUrl
) {}

record GitHubCommit(
    String sha,
    String message,
    GitHubCommitAuthor author,
    LocalDateTime date
) {}

record GitHubCommitAuthor(
    String name,
    String email
) {}
```

---

## Step 5: Monitoring and Observability Implementation

### **Application Health Monitoring**

**File**: `src/main/java/org/frcpm/monitoring/ApplicationHealthService.java`
```java
package org.frcpm.monitoring;

import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.boot.actuator.info.InfoContributor;
import org.springframework.boot.actuator.info.Info;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApplicationHealthService implements HealthIndicator, InfoContributor {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationHealthService.class);
    
    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, Object> healthMetrics = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    @Autowired
    public ApplicationHealthService(DataSource dataSource, 
                                  RedisTemplate<String, Object> redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
        initializeMetrics();
    }
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        try {
            // Check database connectivity
            checkDatabaseHealth(builder);
            
            // Check Redis connectivity
            checkRedisHealth(builder);
            
            // Check application metrics
            checkApplicationMetrics(builder);
            
            // Check disk space
            checkDiskSpace(builder);
            
            // Check memory usage
            checkMemoryUsage(builder);
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", LocalDateTime.now())
                .build();
        }
        
        return builder.build();
    }
    
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
            "name", "FRC Project Management",
            "version", "1.0.0",
            "buildTime", "2024-01-15T10:30:00Z",
            "environment", "production"
        ));
        
        builder.withDetail("metrics", Map.of(
            "requestCount", requestCount.get(),
            "errorCount", errorCount.get(),
            "errorRate", calculateErrorRate(),
            "uptime", getUptimeSeconds()
        ));
        
        builder.withDetail("features", Map.of(
            "coppaCompliance", true,
            "offlineSync", true,
            "mfaEnabled", true,
            "realTimeUpdates", true
        ));
    }
    
    private void checkDatabaseHealth(Health.Builder builder) {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout
            if (isValid) {
                builder.withDetail("database", "UP");
                builder.withDetail("databaseResponseTime", measureDatabaseResponseTime());
            } else {
                builder.down().withDetail("database", "Connection invalid");
            }
        } catch (SQLException e) {
            builder.down().withDetail("database", "Connection failed: " + e.getMessage());
        }
    }
    
    private void checkRedisHealth(Health.Builder builder) {
        try {
            String pong = redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
            
            if ("PONG".equals(pong)) {
                builder.withDetail("redis", "UP");
                builder.withDetail("redisResponseTime", measureRedisResponseTime());
            } else {
                builder.down().withDetail("redis", "Ping failed");
            }
        } catch (Exception e) {
            builder.down().withDetail("redis", "Connection failed: " + e.getMessage());
        }
    }
    
    private void checkApplicationMetrics(Health.Builder builder) {
        double errorRate = calculateErrorRate();
        long activeUsers = getActiveUserCount();
        
        builder.withDetail("errorRate", errorRate);
        builder.withDetail("activeUsers", activeUsers);
        
        if (errorRate > 0.05) { // 5% error rate threshold
            builder.down().withDetail("highErrorRate", "Error rate exceeds threshold: " + errorRate);
        }
        
        if (activeUsers > 100) { // High load threshold
            builder.withDetail("highLoad", "High user load detected: " + activeUsers);
        }
    }
    
    private void checkDiskSpace(Health.Builder builder) {
        java.io.File dataDir = new java.io.File("/opt/frc-pm/data");
        long freeSpace = dataDir.getFreeSpace();
        long totalSpace = dataDir.getTotalSpace();
        double freeSpacePercent = (double) freeSpace / totalSpace * 100;
        
        builder.withDetail("diskSpace", Map.of(
            "free", formatBytes(freeSpace),
            "total", formatBytes(totalSpace),
            "freePercent", String.format("%.1f%%", freeSpacePercent)
        ));
        
        if (freeSpacePercent < 10) {
            builder.down().withDetail("lowDiskSpace", 
                "Free disk space below 10%: " + freeSpacePercent + "%");
        }
    }
    
    private void checkMemoryUsage(Health.Builder builder) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        builder.withDetail("memory", Map.of(
            "used", formatBytes(usedMemory),
            "free", formatBytes(freeMemory),
            "total", formatBytes(totalMemory),
            "usagePercent", String.format("%.1f%%", memoryUsagePercent)
        ));
        
        if (memoryUsagePercent > 90) {
            builder.down().withDetail("highMemoryUsage", 
                "Memory usage above 90%: " + memoryUsagePercent + "%");
        }
    }
    
    private long measureDatabaseResponseTime() {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("SELECT 1");
        } catch (SQLException e) {
            logger.warn("Database response time measurement failed", e);
            return -1;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    private long measureRedisResponseTime() {
        long startTime = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue().get("health-check");
        } catch (Exception e) {
            logger.warn("Redis response time measurement failed", e);
            return -1;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    private double calculateErrorRate() {
        long requests = requestCount.get();
        long errors = errorCount.get();
        return requests > 0 ? (double) errors / requests : 0.0;
    }
    
    private long getActiveUserCount() {
        try {
            // Count active sessions in Redis
            return redisTemplate.keys("frc-pm:session:*").size();
        } catch (Exception e) {
            logger.warn("Failed to get active user count", e);
            return 0;
        }
    }
    
    private long getUptimeSeconds() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    private void initializeMetrics() {
        healthMetrics.put("startTime", LocalDateTime.now());
        healthMetrics.put("version", "1.0.0");
    }
    
    // Public methods for updating metrics
    public void incrementRequestCount() {
        requestCount.incrementAndGet();
    }
    
    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }
    
    // Scheduled health monitoring
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void performScheduledHealthCheck() {
        try {
            Health health = health();
            if (health.getStatus().getCode().equals("DOWN")) {
                logger.error("Application health check failed: {}", health.getDetails());
                
                // Send alert if configured
                sendHealthAlert(health);
            } else {
                logger.debug("Application health check passed");
            }
        } catch (Exception e) {
            logger.error("Scheduled health check failed", e);
        }
    }
    
    private void sendHealthAlert(Health health) {
        // In production, this would send alerts via email, Slack, etc.
        logger.error("HEALTH ALERT: Application health is DOWN - {}", health.getDetails());
    }
}
```

### **Performance Metrics Collector**

**File**: `src/main/java/org/frcpm/monitoring/PerformanceMetricsCollector.java`
```java
package org.frcpm.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PerformanceMetricscompose restart &>/dev/null || true
              EOF

runcmd:
  - /opt/frc-pm/deployment.sh

final_message: "FRC Project Management deployment completed successfully!"

---

## Step 2: Container and Application Configuration

### **Multi-Stage Docker Build Configuration**

**File**: `deployment/docker/Dockerfile`
```dockerfile
# Multi-stage build for optimal production image
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy POM and source code
COPY pom.xml .
COPY src ./src

# Build application with production profile
RUN mvn clean package -DskipTests -Pprod

# Production runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install required packages
RUN apk add --no-cache \
    curl \
    bash \
    tzdata \
    ca-certificates

# Create application user
RUN addgroup -g 1001 frcpm && \
    adduser -D -s /bin/bash -u 1001 -G frcpm frcpm

# Set timezone
ENV TZ=America/New_York
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create application directories
RUN mkdir -p /opt/frc-pm/{data,logs,config,backup} && \
    chown -R frcpm:frcpm /opt/frc-pm

# Copy application JAR from builder stage
COPY --from=builder /app/target/frc-project-management-*.jar /opt/frc-pm/app.jar
COPY --chown=frcpm:frcpm deployment/config/application-prod.yml /opt/frc-pm/config/

# Switch to application user
USER frcpm

# Set working directory
WORKDIR /opt/frc-pm

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx6g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application startup
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --spring.config.location=file:./config/application-prod.yml"]
```

### **Production Spring Boot Configuration**

**File**: `deployment/config/application-prod.yml`
```yaml
# Production Configuration for FRC Project Management
server:
  port: 8080
  servlet:
    context-path: /
  compression:
    enabled: true
    mime-types: text/html,text/css,application/javascript,application/json
  http2:
    enabled: true

spring:
  profiles:
    active: production
  
  # Database Configuration
  datasource:
    url: jdbc:sqlite:/opt/frc-pm/data/frc-project.db
    driver-class-name: org.sqlite.JDBC
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-test-query: SELECT 1
  
  # JPA Configuration
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  # Thymeleaf Configuration
  thymeleaf:
    cache: true
    encoding: UTF-8
    mode: HTML
    prefix: classpath:/templates/
    suffix: .html
  
  # Static Resources
  web:
    resources:
      static-locations: classpath:/static/
      cache:
        cachecontrol:
          max-age: 86400
          cache-public: true
  
  # Security Configuration
  security:
    require-ssl: true
    headers:
      frame-options: SAMEORIGIN
      content-type-options: nosniff
      xss-protection: 1; mode=block
  
  # Redis Configuration (for session management)
  data:
    redis:
      host: redis
      port: 6379
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      jedis:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  
  # Session Configuration
  session:
    store-type: redis
    redis:
      namespace: "frc-pm:session"
    timeout: 1800s # 30 minutes for mentors

# Management and Monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    redis:
      enabled: true

# Logging Configuration
logging:
  level:
    org.frcpm: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /opt/frc-pm/logs/frc-pm.log
    max-size: 10MB
    max-history: 30

# Application-specific Configuration
frc-pm:
  security:
    jwt:
      secret: ${JWT_SECRET:changeme-in-production}
      expiration: 1800000 # 30 minutes
    totp:
      issuer: "FRC Project Management"
      window: 3
    session:
      timeout:
        student: 900 # 15 minutes
        mentor: 1800 # 30 minutes
  
  coppa:
    enabled: true
    audit-logging: true
    data-retention-days: 365
    parental-consent-required: true
  
  features:
    offline-sync: true
    real-time-updates: true
    mobile-optimization: true
    qr-attendance: true
  
  backup:
    enabled: true
    interval: "0 2 * * *" # Daily at 2 AM
    retention-days: 30
    location: /opt/frc-pm/backup
  
  integration:
    github:
      enabled: false
      webhook-secret: ${GITHUB_WEBHOOK_SECRET:}
    frc-events:
      enabled: true
      api-key: ${FRC_EVENTS_API_KEY:}
    blue-alliance:
      enabled: true
      api-key: ${BLUE_ALLIANCE_API_KEY:}

# Async Configuration
async:
  core-pool-size: 4
  max-pool-size: 8
  queue-capacity: 100
  thread-name-prefix: "frc-pm-async-"
```

---

## Step 3: Advanced Application Features Implementation

### **COPPA Compliance Service Implementation**

**File**: `src/main/java/org/frcpm/security/COPPAComplianceService.java`
```java
package org.frcpm.security;

import org.frcpm.models.TeamMember;
import org.frcpm.models.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class COPPAComplianceService {
    
    private static final Logger logger = LoggerFactory.getLogger(COPPAComplianceService.class);
    private static final int COPPA_AGE_THRESHOLD = 13;
    
    private final AuditLogRepository auditLogRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final HttpServletRequest request;
    
    public COPPAComplianceService(AuditLogRepository auditLogRepository,
                                  TeamMemberRepository teamMemberRepository,
                                  HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.request = request;
    }
    
    /**
     * Determines if a team member requires parental consent under COPPA
     */
    public boolean requiresParentalConsent(TeamMember member) {
        if (member.getBirthDate() == null) {
            // If no birth date provided, assume consent required for safety
            return true;
        }
        
        LocalDate thirteenthBirthday = member.getBirthDate().plusYears(COPPA_AGE_THRESHOLD);
        return LocalDate.now().isBefore(thirteenthBirthday);
    }
    
    /**
     * Records consent status for a team member
     */
    public void recordConsentStatus(Long memberId, ConsentStatus status, String parentEmail) {
        TeamMember member = teamMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Team member not found"));
        
        member.setConsentStatus(status);
        member.setConsentDate(LocalDateTime.now());
        member.setParentEmail(parentEmail);
        
        teamMemberRepository.save(member);
        
        // Log consent action
        logDataAccess(getCurrentUser(), member, "CONSENT_RECORDED", 
            Map.of("status", status.toString(), "parentEmail", parentEmail));
        
        logger.info("COPPA consent recorded for member {} with status {}", 
            memberId, status);
    }
    
    /**
     * Enforces data minimization for users under 13
     */
    public void enforceDataMinimization(TeamMember member) {
        if (requiresParentalConsent(member)) {
            // Limit data collection for users under 13
            member.setPhone(null); // Remove phone number
            member.setEmergencyContact(null); // Remove emergency contact
            // Keep only essential data: name, username, subteam assignment
            
            logger.info("Data minimization enforced for member under 13: {}", 
                member.getId());
        }
    }
    
    /**
     * Logs all data access for audit compliance
     */
    public AuditLog logDataAccess(User user, TeamMember subject, String action) {
        return logDataAccess(user, subject, action, Map.of());
    }
    
    public AuditLog logDataAccess(User user, TeamMember subject, String action, 
                                  Map<String, String> additionalData) {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(user != null ? user.getId() : null);
        auditLog.setUsername(user != null ? user.getUsername() : "SYSTEM");
        auditLog.setSubjectMemberId(subject.getId());
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress(getClientIpAddress());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setRequiresParentalConsent(requiresParentalConsent(subject));
        
        // Add additional data as JSON
        if (!additionalData.isEmpty()) {
            auditLog.setAdditionalData(convertToJson(additionalData));
        }
        
        auditLog = auditLogRepository.save(auditLog);
        
        if (requiresParentalConsent(subject)) {
            logger.warn("COPPA-protected data access logged: user={}, subject={}, action={}", 
                user != null ? user.getUsername() : "SYSTEM", 
                subject.getId(), action);
        }
        
        return auditLog;
    }
    
    /**
     * Retrieves audit logs for a specific team member
     */
    public List<AuditLog> getAuditLogsForMember(Long memberId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findBySubjectMemberIdAndTimestampAfter(memberId, since);
    }
    
    /**
     * Generates COPPA compliance report
     */
    public COPPAComplianceReport generateComplianceReport() {
        List<TeamMember> minors = teamMemberRepository.findMinorsRequiringConsent();
        List<TeamMember> withoutConsent = minors.stream()
            .filter(member -> member.getConsentStatus() != ConsentStatus.GRANTED)
            .toList();
        
        long auditLogCount = auditLogRepository.countCOPPAProtectedAccess(
            LocalDateTime.now().minusDays(30));
        
        return new COPPAComplianceReport(
            minors.size(),
            withoutConsent.size(),
            auditLogCount,
            LocalDateTime.now()
        );
    }
    
    /**
     * Schedules automatic data deletion for aged-out members
     */
    @Scheduled(cron = "0 2 * * 0") // Weekly on Sunday at 2 AM
    public void scheduleDataRetentionCleanup() {
        List<TeamMember> membersForCleanup = teamMemberRepository
            .findMembersForDataRetentionCleanup(365); // 1 year retention
        
        for (TeamMember member : membersForCleanup) {
            if (!requiresParentalConsent(member)) {
                // Member is now over 13, can clean up excess audit logs
                auditLogRepository.deleteOldAuditLogs(member.getId(), 90); // Keep 90 days
                logger.info("Cleaned up old audit logs for member {}", member.getId());
            }
        }
    }
    
    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private User getCurrentUser() {
        // Get current authenticated user from Spring Security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
    
    private String convertToJson(Map<String, String> data) {
        // Simple JSON conversion - in production, use Jackson ObjectMapper
        return data.entrySet().stream()
            .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
            .collect(Collectors.joining(",", "{", "}"));
    }
}

// Supporting enums and records
enum ConsentStatus {
    PENDING, GRANTED, DENIED, EXPIRED
}

record COPPAComplianceReport(
    int totalMinors,
    int minorsWithoutConsent,
    long auditLogCount,
    LocalDateTime generatedAt
) {}
```

### **TOTP Multi-Factor Authentication Service**

**File**: `src/main/java/org/frcpm/security/TOTPService.java`
```java
package org.frcpm.security;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import org.frcpm.models.User;
import org.frcpm.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class TOTPService {
    
    private static final Logger logger = LoggerFactory.getLogger(TOTPService.class);
    private static final String ISSUER = "FRC Project Management";
    private static final int RECOVERY_CODE_COUNT = 10;
    private static final int RECOVERY_CODE_LENGTH = 8;
    
    private final SecretGenerator secretGenerator;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;
    private final TimeProvider timeProvider;
    private final UserRepository userRepository;
    private final Random random;
    
    public TOTPService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.secretGenerator = new DefaultSecretGenerator();
        this.codeGenerator = new DefaultCodeGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        this.qrGenerator = new ZxingPngQrGenerator();
        this.random = new Random();
    }
    
    /**
     * Generates a new TOTP secret for a user
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }
    
    /**
     * Generates a QR code for TOTP setup
     */
    public String generateQrCode(String username, String secret) throws QrGenerationException {
        QrData data = new QrData.Builder()
            .label(username)
            .secret(secret)
            .issuer(ISSUER)
            .algorithm(QrData.HashAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();
        
        byte[] qrBytes = qrGenerator.generate(data);
        return Base64.getEncoder().encodeToString(qrBytes);
    }
    
    /**
     * Verifies a TOTP code
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            logger.warn("TOTP verification failed", e);
            return false;
        }
    }
    
    /**
     * Enables TOTP for a user after successful verification
     */
    public List<String> enableTOTPForUser(Long userId, String secret, String verificationCode) {
        if (!verifyCode(secret, verificationCode)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate recovery codes
        List<String> recoveryCodes = generateRecoveryCodes();
        
        // Hash recovery codes before storing
        List<String> hashedRecoveryCodes = recoveryCodes.stream()
            .map(this::hashRecoveryCode)
            .collect(Collectors.toList());
        
        user.setTotpSecret(secret);
        user.setTotpEnabled(true);
        user.setRecoveryCodes(String.join(",", hashedRecoveryCodes));
        user.setTotpBackupCodesUsed(0);
        
        userRepository.save(user);
        
        logger.info("TOTP enabled for user: {}", user.getUsername());
        
        return recoveryCodes; // Return unhashed codes for user to save
    }
    
    /**
     * Disables TOTP for a user
     */
    public void disableTOTPForUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        user.setRecoveryCodes(null);
        user.setTotpBackupCodesUsed(0);
        
        userRepository.save(user);
        
        logger.info("TOTP disabled for user: {}", user.getUsername());
    }
    
    /**
     * Verifies TOTP code or recovery code
     */
    public boolean verifyUserCode(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
            return false;
        }
        
        // First try TOTP code
        if (verifyCode(user.getTotpSecret(), code)) {
            logger.info("TOTP code verified for user: {}", user.getUsername());
            return true;
        }
        
        // Then try recovery codes
        if (verifyRecoveryCode(user, code)) {
            logger.warn("Recovery code used for user: {}", user.getUsername());
            return true;
        }
        
        logger.warn("TOTP verification failed for user: {}", user.getUsername());
        return false;
    }
    
    /**
     * Verifies a recovery code and marks it as used
     */
    private boolean verifyRecoveryCode(User user, String code) {
        if (user.getRecoveryCodes() == null || code == null) {
            return false;
        }
        
        String hashedCode = hashRecoveryCode(code);
        List<String> recoveryCodes = List.of(user.getRecoveryCodes().split(","));
        
        if (!recoveryCodes.contains(hashedCode)) {
            return false;
        }
        
        // Mark recovery code as used by removing it
        List<String> remainingCodes = recoveryCodes.stream()
            .filter(rc -> !rc.equals(hashedCode))
            .collect(Collectors.toList());
        
        user.setRecoveryCodes(String.join(",", remainingCodes));
        user.setTotpBackupCodesUsed(user.getTotpBackupCodesUsed() + 1);
        
        userRepository.save(user);
        
        // Warn if running low on recovery codes
        if (remainingCodes.size() <= 2) {
            logger.warn("User {} has only {} recovery codes remaining", 
                user.getUsername(), remainingCodes.size());
        }
        
        return true;
    }
    
    /**
     * Generates new recovery codes for a user
     */
    public List<String> regenerateRecoveryCodes(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!user.isTotpEnabled()) {
            throw new IllegalStateException("TOTP is not enabled for this user");
        }
        
        List<String> newRecoveryCodes = generateRecoveryCodes();
        List<String> hashedCodes = newRecoveryCodes.stream()
            .map(this::hashRecoveryCode)
            .collect(Collectors.toList());
        
        user.setRecoveryCodes(String.join(",", hashedCodes));
        user.setTotpBackupCodesUsed(0);
        
        userRepository.save(user);
        
        logger.info("Recovery codes regenerated for user: {}", user.getUsername());
        
        return newRecoveryCodes;
    }
    
    /**
     * Checks if user has TOTP enabled
     */
    public boolean isTOTPEnabled(Long userId) {
        return userRepository.findById(userId)
            .map(User::isTotpEnabled)
            .orElse(false);
    }
    
    /**
     * Gets remaining recovery code count
     */
    public int getRemainingRecoveryCodeCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getRecoveryCodes() == null) {
            return 0;
        }
        
        return user.getRecoveryCodes().split(",").length;
    }
    
    private List<String> generateRecoveryCodes() {
        return IntStream.range(0, RECOVERY_CODE_COUNT)
            .mapToObj(i -> generateRecoveryCode())
            .collect(Collectors.toList());
    }
    
    private String generateRecoveryCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < RECOVERY_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    private String hashRecoveryCode(String code) {
        // In production, use BCrypt or similar
        // For simplicity, using SHA-256 here
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash recovery code", e);
        }
    }
}
```

### **Progressive Web App Service Worker**

**File**: `src/main/resources/static/js/sw.js`
```javascript
/**
 * Service Worker for FRC Project Management PWA
 * Provides offline functionality and background sync
 */

const CACHE_NAME = 'frc-pm-v1.0.0';
const STATIC_CACHE = 'frc-pm-static-v1.0.0';
const DYNAMIC_CACHE = 'frc-pm-dynamic-v1.0.0';
const OFFLINE_PAGE = '/offline.html';

// Resources to cache on install
const STATIC_ASSETS = [
    '/',
    '/offline.html',
    '/css/styles.css',
    '/css/mobile.css',
    '/js/app.js',
    '/js/offline-manager.js',
    '/icons/icon-192x192.png',
    '/icons/icon-512x512.png',
    '/manifest.json'
];

// API endpoints that support offline functionality
const CACHEABLE_APIS = [
    '/api/tasks',
    '/api/projects',
    '/api/team-members',
    '/api/attendance',
    '/api/meetings'
];

// Install event - cache static assets
self.addEventListener('install', event => {
    console.log('Service Worker: Installing...');
    
    event.waitUntil(
        Promise.all([
            caches.open(STATIC_CACHE).then(cache => {
                console.log('Service Worker: Caching static assets');
                return cache.addAll(STATIC_ASSETS);
            }),
            // Skip waiting to activate immediately
            self.skipWaiting()
        ])
    );
});

// Activate event - clean up old caches
self.addEventListener('activate', event => {
    console.log('Service Worker: Activating...');
    
    event.waitUntil(
        Promise.all([
            // Clean up old caches
            caches.keys().then(cacheNames => {
                return Promise.all(
                    cacheNames.map(cacheName => {
                        if (cacheName !== STATIC_CACHE && 
                            cacheName !== DYNAMIC_CACHE) {
                            console.log('Service Worker: Deleting old cache:', cacheName);
                            return caches.delete(cacheName);
                        }
                    })
                );
            }),
            // Take control of all clients
            self.clients.claim()
        ])
    );
});

// Fetch event - handle network requests
self.addEventListener('fetch', event => {
    const { request } = event;
    const url = new URL(request.url);
    
    // Skip non-GET requests and chrome-extension requests
    if (request.method !== 'GET' || url.protocol === 'chrome-extension:') {
        return;
    }
    
    // Handle different types of requests
    if (isStaticAsset(url.pathname)) {
        event.respondWith(handleStaticAsset(request));
    } else if (isAPIRequest(url.pathname)) {
        event.respondWith(handleAPIRequest(request));
    } else if (isPageRequest(request)) {
        event.respondWith(handlePageRequest(request));
    }
});

// Background sync for offline actions
self.addEventListener('sync', event => {
    console.log('Service Worker: Background sync triggered:', event.tag);
    
    if (event.tag === 'task-updates') {
        event.waitUntil(syncTaskUpdates());
    } else if (event.tag === 'attendance-updates') {
        event.waitUntil(syncAttendanceUpdates());
    }
});

// Push notifications
self.addEventListener('push', event => {
    console.log('Service Worker: Push notification received');
    
    const options = {
        body: event.data ? event.data.text() : 'New update available',
        icon: '/icons/icon-192x192.png',
        badge: '/icons/badge-72x72.png',
        vibrate: [200, 100, 200],
        tag: 'frc-pm-notification',
        actions: [
            {
                action: 'open',
                title: 'Open App',
                icon: '/icons/action-open.png'
            },
            {
                action: 'dismiss',
                title: 'Dismiss',
                icon: '/icons/action-dismiss.png'
            }
        ]
    };
    
    event.waitUntil(
        self.registration.showNotification('FRC Project Management', options)
    );
});

// Notification click handling
self.addEventListener('notificationclick', event => {
    console.log('Service Worker: Notification clicked');
    
    event.notification.close();
    
    if (event.action === 'open') {
        event.waitUntil(
            clients.openWindow('/')
        );
    }
    // 'dismiss' action or no action - just close notification
});

// Helper functions
function isStaticAsset(pathname) {
    return pathname.startsWith('/css/') || 
           pathname.startsWith('/js/') || 
           pathname.startsWith('/icons/') ||
           pathname.startsWith('/images/') ||
           pathname === '/manifest.json';
}

function isAPIRequest(pathname) {
    return pathname.startsWith('/api/');
}

function isPageRequest(request) {
    return request.headers.get('accept') && 
           request.headers.get('accept').includes('text/html');
}

function handleStaticAsset(request) {
    return caches.match(request).then(response => {
        return response || fetch(request).then(fetchResponse => {
            return caches.open(STATIC_CACHE).then(cache => {
                cache.put(request, fetchResponse.clone());
                return fetchResponse;
            });
        });
    });
}

function handleAPIRequest(request) {
    const url = new URL(request.url);
    
    // Check if this API supports offline caching
    if (!CACHEABLE_APIs.some(api => url.pathname.startsWith(api))) {
        return fetch(request);
    }
    
    return caches.open(DYNAMIC_CACHE).then(cache => {
        return fetch(request).then(response => {
            // Cache successful responses
            if (response.status === 200) {
                cache.put(request, response.clone());
            }
            return response;
        }).catch(() => {
            // Return cached version if network fails
            return cache.match(request).then(cachedResponse => {
                if (cachedResponse) {
                    // Add offline indicator to response
                    const headers = new Headers(cachedResponse.headers);
                    headers.set('X-Offline-Response', 'true');
                    
                    return new Response(cachedResponse.body, {
                        status: cachedResponse.status,
                        statusText: cachedResponse.statusText,
                        headers: headers
                    });
                }
                
                // Return offline indicator for uncached requests
                return new Response(JSON.stringify({
                    error: 'Offline',
                    message: 'This data is not available offline'
                }), {
                    status: 503,
                    headers: { 'Content-Type': 'application/json' }
                });
            });
        });
    });
}

function handlePageRequest(request) {
    return fetch(request).catch(() => {
        // Serve offline page for navigation requests
        return caches.match(OFFLINE_PAGE);
    });
}

async function syncTaskUpdates() {
    try {
        const offlineActions = await getOfflineActions('task-updates');
        
        for (const action of offlineActions) {
            try {
                await fetch(action.url, {
                    method: action.method,
                    headers: action.headers,
                    body: action.body
                });
                
                // Remove successful action from offline storage
                await removeOfflineAction(action.id);
                
                console.log('Service Worker: Synced task update:', action.id);
            } catch (error) {
                console.error('Service Worker: Failed to sync task update:', error);
            }
        }
    } catch (error) {
        console.error('Service Worker: Task sync failed:', error);
    }
}

async function syncAttendanceUpdates() {
    try {
        const offlineActions = await getOfflineActions('attendance-updates');
        
        for (const action of offlineActions) {
            try {
                await fetch(action.url, {
                    method: action.method,
                    headers: action.headers,
                    body: action.body
                });
                
                await removeOfflineAction(action.id);
                console.log('Service Worker: Synced attendance update:', action.id);
            } catch (error) {
                console.error('Service Worker: Failed to sync attendance update:', error);
            }
        }
    } catch (error) {
        console.error('Service Worker: Attendance sync failed:', error);
    }
}

async function getOfflineActions(tag) {
    // This would integrate with IndexedDB for offline action storage
    // For now, return empty array
    return [];
}

async function removeOfflineAction(actionId) {
    // Remove action from IndexedDB
    console.log('Service Worker: Removing offline action:', actionId);
}
```

### **Offline Data Management System**

**File**: `src/main/resources/static/js/offline-manager.js`
```javascript
/**
 * Offline Manager for FRC Project Management
 * Handles local data storage and synchronization
 */

class OfflineManager {
    constructor() {
        this.dbName = 'FRCProjectManagementDB';
        this.dbVersion = 1;
        this.db = null;
        this.syncQueue = [];
        this.isOnline = navigator.onLine;
        
        this.init();
        this.setupEventListeners();
    }
    
    async init() {
        try {
            this.db = await this.openDatabase();
            console.log('Offline Manager: Database initialized');
            
            // Process any pending sync operations
            if (this.isOnline) {
                await this.processSyncQueue();
            }
        } catch (error) {
            console.error('Offline Manager: Failed to initialize:', error);
        }
    }
    
    openDatabase() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(this.dbName, this.dbVersion);
            
            request.onerror = () => reject(request.error);
            request.onsuccess = () => resolve(request.result);
            
            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                
                // Tasks store
                if (!db.objectStoreNames.contains('tasks')) {
                    const taskStore = db.createObjectStore('tasks', { keyPath: 'id' });
                    taskStore.createIndex('projectId', 'projectId', { unique: false });
                    taskStore.createIndex('lastModified', 'lastModified', { unique: false });
                }
                
                // Attendance store
                if (!db.objectStoreNames.contains('attendance')) {
                    const attendanceStore = db.createObjectStore('attendance', { keyPath: 'id' });
                    attendanceStore.createIndex('meetingId', 'meetingId', { unique: false });
                    attendanceStore.createIndex('memberId', 'memberId', { unique: false });
                }
                
                // Sync queue store
                if (!db.objectStoreNames.contains('syncQueue')) {
                    const syncStore = db.createObjectStore('syncQueue', { 
                        keyPath: 'id', 
                        autoIncrement: true 
                    });
                    syncStore.createIndex('timestamp', 'timestamp', { unique: false });
                    syncStore.createIndex('type', 'type', { unique: false });
                }
                
                // Offline actions store
                if (!db.objectStoreNames.contains('offlineActions')) {
                    const actionStore = db.createObjectStore('offlineActions', { 
                        keyPath: 'id', 
                        autoIncrement: true 
                    });
                    actionStore.createIndex('timestamp', 'timestamp', { unique: false });
                    actionStore.createIndex('endpoint', 'endpoint', { unique: false });
                }
            };
        });
    }
    
    setupEventListeners() {
        // Online/offline status changes
        window.addEventListener('online', () => {
            console.log('Offline Manager: Back online');
            this.isOnline = true;
            this.processSyncQueue();
            this.notifyOnlineStatus(true);
        });
        
        window.addEventListener('offline', () => {
            console.log('Offline Manager: Gone offline');
            this.isOnline = false;
            this.notifyOnlineStatus(false);
        });
        
        // Page visibility changes (for background sync)
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden && this.isOnline) {
                this.processSyncQueue();
            }
        });
    }
    
    // Task Management Methods
    async cacheTaskData(projectId) {
        try {
            const response = await fetch(`/api/tasks?projectId=${projectId}`);
            if (!response.ok) throw new Error('Failed to fetch tasks');
            
            const tasks = await response.json();
            const transaction = this.db.transaction(['tasks'], 'readwrite');
            const store = transaction.objectStore('tasks');
            
            for (const task of tasks) {
                task.lastModified = new Date().toISOString();
                task.cached = true;
                await this.promisifyRequest(store.put(task));
            }
            
            console.log(`Offline Manager: Cached ${tasks.length} tasks for project ${projectId}`);
            return tasks;
        } catch (error) {
            console.error('Offline Manager: Failed to cache task data:', error);
            throw error;
        }
    }
    
    async getOfflineTasks(projectId) {
        try {
            const transaction = this.db.transaction(['tasks'], 'readonly');
            const store = transaction.objectStore('tasks');
            const index = store.index('projectId');
            
            const tasks = await this.promisifyRequest(index.getAll(projectId));
            return tasks.filter(task => task.cached);
        } catch (error) {
            console.error('Offline Manager: Failed to get offline tasks:', error);
            return [];
        }
    }
    
    async updateTaskOffline(taskId, updates) {
        try {
            const transaction = this.db.transaction(['tasks'], 'readwrite');
            const store = transaction.objectStore('tasks');
            
            const task = await this.promisifyRequest(store.get(taskId));
            if (!task) {
                throw new Error('Task not found in offline storage');
            }
            
            // Apply updates
            Object.assign(task, updates);
            task.lastModified = new Date().toISOString();
            task.pendingSync = true;
            
            await this.promisifyRequest(store.put(task));
            
            // Queue for sync when online
            await this.queueForSync({
                type: 'task-update',
                endpoint: `/api/tasks/${taskId}`,
                method: 'PUT',
                data: updates,
                timestamp: new Date().toISOString()
            });
            
            console.log('Offline Manager: Task updated offline:', taskId);
            return task;
        } catch (error) {
            console.error('Offline Manager: Failed to update task offline:', error);
            throw error;
        }
    }
    
    async createTaskOffline(taskData) {
        try {
            const tempId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
            const task = {
                ...taskData,
                id: tempId,
                tempId: true,
                lastModified: new Date().toISOString(),
                pendingSync: true,
                cached: true
            };
            
            const transaction = this.db.transaction(['tasks'], 'readwrite');
            const store = transaction.objectStore('tasks');
            await this.promisifyRequest(store.put(task));
            
            // Queue for sync when online
            await this.queueForSync({
                type: 'task-create',
                endpoint: '/api/tasks',
                method: 'POST',
                data: taskData,
                tempId: tempId,
                timestamp: new Date().toISOString()
            });
            
            console.log('Offline Manager: Task created offline:', tempId);
            return task;
        } catch (error) {
            console.error('Offline Manager: Failed to create task offline:', error);
            throw error;
        }
    }
    
    // Attendance Management Methods
    async recordAttendanceOffline(meetingId, attendanceData) {
        try {
            const attendance = {
                ...attendanceData,
                id: `temp_attendance_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                meetingId: meetingId,
                tempId: true,
                lastModified: new Date().toISOString(),
                pendingSync: true
            };
            
            const transaction = this.db.transaction(['attendance'], 'readwrite');
            const store = transaction.objectStore('attendance');
            await this.promisifyRequest(store.put(attendance));
            
            // Queue for sync
            await this.queueForSync({
                type: 'attendance-record',
                endpoint: `/api/attendance`,
                method: 'POST',
                data: { meetingId, ...attendanceData },
                tempId: attendance.id,
                timestamp: new Date().toISOString()
            });
            
            console.log('Offline Manager: Attendance recorded offline');
            return attendance;
        } catch (error) {
            console.error('Offline Manager: Failed to record attendance offline:', error);
            throw error;
        }
    }
    
    // Sync Queue Management
    async queueForSync(action) {
        try {
            const transaction = this.db.transaction(['syncQueue'], 'readwrite');
            const store = transaction.objectStore('syncQueue');
            
            await this.promisifyRequest(store.add(action));
            
            // Try to register background sync if available
            if ('serviceWorker' in navigator && 'sync' in window.ServiceWorkerRegistration.prototype) {
                const registration = await navigator.serviceWorker.ready;
                await registration.sync.register(action.type);
            }
            
            console.log('Offline Manager: Action queued for sync:', action.type);
        } catch (error) {
            console.error('Offline Manager: Failed to queue action for sync:', error);
        }
    }
    
    async processSyncQueue() {
        if (!this.isOnline || !this.db) return;
        
        try {
            const transaction = this.db.transaction(['syncQueue'], 'readwrite');
            const store = transaction.objectStore('syncQueue');
            const allActions = await this.promisifyRequest(store.getAll());
            
            console.log(`Offline Manager: Processing ${allActions.length} queued actions`);
            
            for (const action of allActions) {
                try {
                    await this.syncAction(action);
                    await this.promisifyRequest(store.delete(action.id));
                    console.log('Offline Manager: Synced action:', action.type);
                } catch (error) {
                    console.error('Offline Manager: Failed to sync action:', action.type, error);
                    // Leave failed actions in queue for retry
                }
            }
        } catch (error) {
            console.error('Offline Manager: Failed to process sync queue:', error);
        }
    }
    
    async syncAction(action) {
        const response = await fetch(action.endpoint, {
            method: action.method,
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(action.data)
        });
        
        if (!response.ok) {
            throw new Error(`Sync failed: ${response.status} ${response.statusText}`);
        }
        
        const result = await response.json();
        
        // Update local data with server response
        if (action.tempId && result.id) {
            await this.updateTempIdWithRealId(action.type, action.tempId, result.id, result);
        }
        
        return result;
    }
    
    async updateTempIdWithRealId(type, tempId, realId, serverData) {
        try {
            let storeName;
            if (type.includes('task')) {
                storeName = 'tasks';
            } else if (type.includes('attendance')) {
                storeName = 'attendance';
            } else {
                return; // Unknown type
            }
            
            const transaction = this.db.transaction([storeName], 'readwrite');
            const store = transaction.objectStore(storeName);
            
            // Get the temp record
            const tempRecord = await this.promisifyRequest(store.get(tempId));
            if (tempRecord) {
                // Delete temp record
                await this.promisifyRequest(store.delete(tempId));
                
                // Add real record with server data
                const realRecord = {
                    ...tempRecord,
                    ...serverData,
                    id: realId,
                    tempId: false,
                    pendingSync: false
                };
                
                await this.promisifyRequest(store.put(realRecord));
                console.log(`Offline Manager: Updated temp ID ${tempId} to real ID ${realId}`);
            }
        } catch (error) {
            console.error('Offline Manager: Failed to update temp ID:', error);
        }
    }
    
    // Conflict Resolution
    async resolveConflicts(localData, serverData) {
        // Simple last-write-wins strategy
        // In production, implement more sophisticated conflict resolution
        
        const localTimestamp = new Date(localData.lastModified);
        const serverTimestamp = new Date(serverData.lastModified);
        
        if (localTimestamp > serverTimestamp) {
            console.log('Offline Manager: Local data is newer, keeping local changes');
            return localData;
        } else {
            console.log('Offline Manager: Server data is newer, using server data');
            return serverData;
        }
    }
    
    // Data Cleanup
    async cleanupOldData(daysToKeep = 7) {
        try {
            const cutoffDate = new Date();
            cutoffDate.setDate(cutoffDate.getDate() - daysToKeep);
            const cutoffTimestamp = cutoffDate.toISOString();
            
            const stores = ['tasks', 'attendance'];
            
            for (const storeName of stores) {
                const transaction = this.db.transaction([storeName], 'readwrite');
                const store = transaction.objectStore('tasks');
                const index = store.index('lastModified');
                
                const oldRecords = await this.promisifyRequest(
                    index.getAll(IDBKeyRange.upperBound(cutoffTimestamp))
                );
                
                for (const record of oldRecords) {
                    if (!record.pendingSync) {
                        await this.promisifyRequest(store.delete(record.id));
                    }
                }
                
                console.log(`Offline Manager: Cleaned up ${oldRecords.length} old ${storeName} records`);
            }
        } catch (error) {
            console.error('Offline Manager: Failed to cleanup old data:', error);
        }
    }
    
    // Utility Methods
    promisifyRequest(request) {
        return new Promise((resolve, reject) => {
            request.onerror = () => reject(request.error);
            request.onsuccess = () => resolve(request.result);
        });
    }
    
    notifyOnlineStatus(isOnline) {
        // Dispatch custom event for UI updates
        const event = new CustomEvent('onlineStatusChanged', {
            detail: { isOnline }
        });
        window.dispatchEvent(event);
        
        // Update UI indicators
        const statusIndicators = document.querySelectorAll('.online-status');
        statusIndicators.forEach(indicator => {
            indicator.textContent = isOnline ? 'Online' : 'Offline';
            indicator.className = `online-status ${isOnline ? 'online' : 'offline'}`;
        });
    }
    
    // Public API
    isOffline() {
        return !this.isOnline;
    }
    
    async getQueuedActionCount() {
        if (!this.db) return 0;
        
        try {
            const transaction = this.db.transaction(['syncQueue'], 'readonly');
            const store = transaction.objectStore('syncQueue');
            const count = await this.promisifyRequest(store.count());
            return count;
        } catch (error) {
            console.error('Offline Manager: Failed to get queued action count:', error);
            return 0;
        }
    }
    
    async forceSyncNow() {
        if (this.isOnline) {
            await this.processSyncQueue();
        } else {
            console.warn('Offline Manager: Cannot sync while offline');
        }
    }
}

// Initialize offline manager when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.offlineManager = new OfflineManager();
});

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OfflineManager;
}
```

---

## Step 4: FRC-Specific Integration Services

### **FRC Events API Integration Service**

**File**: `src/main/java/org/frcpm/integration/FRCEventsService.java`
```java
package org.frcpm.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class FRCEventsService {
    
    private static final Logger logger = LoggerFactory.getLogger(FRCEventsService.class);
    private static final String FRC_EVENTS_BASE_URL = "https://frc-api.firstinspires.org/v3.0";
    
    private final WebClient webClient;
    private final String apiKey;
    private final TeamConfigurationService teamConfigService;
    private final ProjectService projectService;
    
    public FRCEventsService(@Value("${frc-pm.integration.frc-events.api-key:}") String apiKey,
                           TeamConfigurationService teamConfigService,
                           ProjectService projectService) {
        this.apiKey = apiKey;
        this.teamConfigService = teamConfigService;
        this.projectService = projectService;
        
        this.webClient = WebClient.builder()
            .baseUrl(FRC_EVENTS_BASE_URL)
            .defaultHeader("Authorization", "Basic " + apiKey)
            .defaultHeader("If-Modified-Since", "")
            .build();
    }
    
    /**
     * Gets the current FRC season year
     */
    public int getCurrentSeason() {
        LocalDate now = LocalDate.now();
        // FRC season typically runs January to April of the same year
        // But kickoff is in January, so season year = calendar year
        return now.getYear();
    }
    
    /**
     * Fetches team information from FRC Events API
     */
    public Mono<FRCTeamInfo> getTeamInfo(int teamNumber) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("FRC Events API key not configured");
            return Mono.empty();
        }
        
        return webClient.get()
            .uri("/teams/{teamNumber}", teamNumber)
            .retrieve()
            .bodyToMono(FRCTeamInfo.class)
            .doOnSuccess(team -> logger.info("Retrieved team info for team {}: {}", 
                teamNumber, team.nickname()))
            .doOnError(error -> logger.error("Failed to retrieve team info for team {}: {}", 
                teamNumber, error.getMessage()));
    }
    
    /**
     * Gets events for a specific team in current season
     */
    public Mono<List<FRCEvent>> getTeamEvents(int teamNumber) {
        return getTeamEvents(teamNumber, getCurrentSeason());
    }
    
    /**
     * Gets events for a specific team in a specific season
     */
    public Mono<List<FRCEvent>> getTeamEvents(int teamNumber, int season) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.just(List.of());
        }
        
        return webClient.get()
            .uri("/teams/{teamNumber}/events/{season}", teamNumber, season)
            .retrieve()
            .bodyToFlux(FRCEvent.class)
            .collectList()
            .doOnSuccess(events -> logger.info("Retrieved {} events for team {} in season {}", 
                events.size(), teamNumber, season))
            .doOnError(error -> logger.error("Failed to retrieve events for team {}: {}", 
                teamNumber, error.getMessage()));
    }
    
    /**
     * Gets competition schedule and results
     */
    public Mono<List<FRCMatch>> getEventMatches(String eventKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.just(List.of());
        }
        
        return webClient.get()
            .uri("/events/{eventKey}/matches", eventKey)
            .retrieve()
            .bodyToFlux(FRCMatch.class)
            .collectList()
            .doOnSuccess(matches -> logger.info("Retrieved {} matches for event {}", 
                matches.size(), eventKey))
            .doOnError(error -> logger.error("Failed to retrieve matches for event {}: {}", 
                eventKey, error.getMessage()));
    }
    
    /**
     * Gets team rankings at an event
     */
    public Mono<FRCRankings> getTeamRankings(String eventKey, int teamNumber) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.empty();
        }
        
        return webClient.get()
            .uri("/events/{eventKey}/rankings", eventKey)
            .retrieve()
            .bodyToMono(FRCRankings.class)
            .map(rankings -> rankings.getTeamRanking(teamNumber))
            .doOnSuccess(ranking -> {
                if (ranking != null) {
                    logger.info("Team {} ranked {} at event {}", 
                        teamNumber, ranking.rank(), eventKey);
                }
            });
    }
    
    /**
     * Automatically updates project milestones based on competition schedule
     */
    @Scheduled(cron = "0 6 * * MON") // Every Monday at 6 AM
    public void updateCompetitionMilestones() {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.info("FRC Events integration disabled - no API key configured");
            return;
        }
        
        try {
            Integer teamNumber = teamConfigService.getTeamNumber();
            if (teamNumber == null) {
                logger.warn("Team number not configured - cannot update competition milestones");
                return;
            }
            
            // Get current active project
            Project currentProject = projectService.getCurrentProject();
            if (currentProject == null) {
                logger.info("No active project found - skipping milestone updates");
                return;
            }
            
            // Get team events for current season
            List<FRCEvent> events = getTeamEvents(teamNumber).block();
            if (events == null || events.isEmpty()) {
                logger.info("No events found for team {} - skipping milestone updates", teamNumber);
                return;
            }
            
            // Create or update milestones for each competition
            for (FRCEvent event : events) {
                createCompetitionMilestones(currentProject, event);
            }
            
            logger.info("Updated competition milestones for {} events", events.size());
            
        } catch (Exception e) {
            logger.error("Failed to update competition milestones", e);
        }
    }
    
    /**
     * Creates milestones for a competition event
     */
    private void createCompetitionMilestones(Project project, FRCEvent event) {
        try {
            // Robot ship deadline (usually 6 weeks after kickoff)
            LocalDate shipDeadline = event.dateStart().minusDays(21); // 3 weeks before competition
            createMilestoneIfNotExists(project, 
                "Robot Ship Deadline - " + event.name(),
                shipDeadline,
                "Final robot must be shipped to competition");
            
            // Load-in deadline
            LocalDate loadInDate = event.dateStart().minusDays(1);
            createMilestoneIfNotExists(project,
                "Load-In - " + event.name(),
                loadInDate,
                "Load robot and equipment into competition venue");
            
            // Competition start
            createMilestoneIfNotExists(project,
                "Competition Start - " + event.name(),
                event.dateStart(),
                "Competition begins at " + event.venue());
            
            // If this is a district championship or world championship
            if (event.eventType() == FRCEventType.DISTRICT_CHAMPIONSHIP ||
                event.eventType() == FRCEventType.CHAMPIONSHIP) {
                
                // Add extra preparation milestone
                LocalDate prepDeadline = event.dateStart().minusDays(7);
                createMilestoneIfNotExists(project,
                    "Championship Prep - " + event.name(),
                    prepDeadline,
                    "Final preparations and practice for championship event");
            }
            
        } catch (Exception e) {
            logger.error("Failed to create milestones for event {}", event.name(), e);
        }
    }
    
    private void createMilestoneIfNotExists(Project project, String name, 
                                          LocalDate date, String description) {
        // Check if milestone already exists
        List<Milestone> existing = milestoneService.findByProjectAndName(project, name);
        if (existing.isEmpty()) {
            Milestone milestone = new Milestone();
            milestone.setName(name);
            milestone.setDate(date);
            milestone.setDescription(description);
            milestone.setProject(project);
            milestone.setCategory("Competition");
            milestone.setAutoGenerated(true);
            
            milestoneService.save(milestone);
            logger.info("Created competition milestone: {}", name);
        }
    }
    
    /**
     * Gets team performance data from competitions
     */
    public Mono<FRCTeamPerformance> getTeamPerformance(int teamNumber, int season) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.empty();
        }
        
        return getTeamEvents(teamNumber, season)
            .flatMapMany(events -> Flux.fromIterable(events))
            .flatMap(event -> getTeamRankings(event.eventKey(), teamNumber))
            .collectList()
            .map(rankings -> new FRCTeamPerformance(teamNumber, season, rankings))
            .doOnSuccess(performance -> logger.info("Calculated performance for team {} in {}: avg rank {}", 
                teamNumber, season, performance.averageRank()));
    }
}

// Supporting data classes
record FRCTeamInfo(
    int teamNumber,
    String nickname,
    String name,
    String city,
    String stateProv,
    String country,
    String website,
    int rookieYear
) {}

record FRCEvent(
    String eventKey,
    String name,
    String eventType,
    String venue,
    String city,
    String stateProv,
    String country,
    LocalDate dateStart,
    LocalDate dateEnd,
    int weekNumber
) {
    public FRCEventType eventType() {
        return FRCEventType.from

@Component
public class PerformanceMetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsCollector.class);
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gaugeValues = new ConcurrentHashMap<>();
    
    // FRC-specific metrics
    private final AtomicLong tasksCreatedToday = new AtomicLong(0);
    private final AtomicLong tasksCompletedToday = new AtomicLong(0);
    private final AtomicLong attendanceRecordsToday = new AtomicLong(0);
    private final AtomicLong activeProjects = new AtomicLong(0);
    private final AtomicLong activeTeamMembers = new AtomicLong(0);
    
    public PerformanceMetricsCollector(MeterRegistry meterRegistry,
                                     TaskService taskService,
                                     ProjectService projectService,
                                     TeamMemberService teamMemberService,
                                     AttendanceService attendanceService) {
        this.meterRegistry = meterRegistry;
        
        // Register FRC-specific gauges
        Gauge.builder("frc.tasks.created.today")
            .description("Number of tasks created today")
            .register(meterRegistry, tasksCreatedToday, AtomicLong::get);
            
        Gauge.builder("frc.tasks.completed.today")
            .description("Number of tasks completed today")
            .register(meterRegistry, tasksCompletedToday, AtomicLong::get);
            
        Gauge.builder("frc.attendance.records.today")
            .description("Number of attendance records created today")
            .register(meterRegistry, attendanceRecordsToday, AtomicLong::get);
            
        Gauge.builder("frc.projects.active")
            .description("Number of active projects")
            .register(meterRegistry, activeProjects, AtomicLong::get);
            
        Gauge.builder("frc.team.members.active")
            .description("Number of active team members")
            .register(meterRegistry, activeTeamMembers, AtomicLong::get);
    }
    
    /**
     * Records method execution time
     */
    public Timer.Sample startTimer(String metricName) {
        Timer timer = timers.computeIfAbsent(metricName, 
            name -> Timer.builder(name)
                .description("Execution time for " + name)
                .register(meterRegistry));
        return Timer.start(meterRegistry);
    }
    
    /**
     * Records method execution completion
     */
    public void recordTimer(String metricName, Timer.Sample sample) {
        Timer timer = timers.get(metricName);
        if (timer != null && sample != null) {
            sample.stop(timer);
        }
    }
    
    /**
     * Increments a counter metric
     */
    public void incrementCounter(String metricName) {
        incrementCounter(metricName, 1);
    }
    
    public void incrementCounter(String metricName, long amount) {
        Counter counter = counters.computeIfAbsent(metricName,
            name -> Counter.builder(name)
                .description("Counter for " + name)
                .register(meterRegistry));
        counter.increment(amount);
    }
    
    /**
     * Records a gauge value
     */
    public void recordGauge(String metricName, long value) {
        gaugeValues.computeIfAbsent(metricName, name -> {
            AtomicLong atomicValue = new AtomicLong(value);
            Gauge.builder(name)
                .description("Gauge for " + name)
                .register(meterRegistry, atomicValue, AtomicLong::get);
            return atomicValue;
        }).set(value);
    }
    
    // FRC-specific metric recording methods
    public void recordTaskCreated() {
        tasksCreatedToday.incrementAndGet();
        incrementCounter("frc.tasks.created.total");
    }
    
    public void recordTaskCompleted() {
        tasksCompletedToday.incrementAndGet();
        incrementCounter("frc.tasks.completed.total");
    }
    
    public void recordAttendanceRecorded() {
        attendanceRecordsToday.incrementAndGet();
        incrementCounter("frc.attendance.records.total");
    }
    
    public void recordUserLogin(String userType) {
        incrementCounter("frc.user.logins.total", "type", userType);
    }
    
    public void recordMeetingCreated() {
        incrementCounter("frc.meetings.created.total");
    }
    
    public void recordComponentDelivered() {
        incrementCounter("frc.components.delivered.total");
    }
    
    public void recordMilestoneReached() {
        incrementCounter("frc.milestones.reached.total");
    }
    
    public void recordOfflineSync() {
        incrementCounter("frc.offline.sync.total");
    }
    
    public void recordCOPPAAuditLog() {
        incrementCounter("frc.coppa.audit.logs.total");
    }
    
    // Scheduled metrics collection
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectActiveMetrics() {
        try {
            // These would be injected or accessed via service layer
            // For now, using placeholder logic
            recordGauge("frc.active.sessions", getActiveSessionCount());
            recordGauge("frc.pending.sync.actions", getPendingSyncActions());
            recordGauge("frc.database.connections", getDatabaseConnectionCount());
            
        } catch (Exception e) {
            logger.error("Failed to collect active metrics", e);
        }
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void resetDailyMetrics() {
        tasksCreatedToday.set(0);
        tasksCompletedToday.set(0);
        attendanceRecordsToday.set(0);
        logger.info("Reset daily metrics");
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void collectProjectMetrics() {
        try {
            // Update project and team member counts
            // These would be calculated from service layer
            activeProjects.set(getActiveProjectCount());
            activeTeamMembers.set(getActiveTeamMemberCount());
            
            // Record build season specific metrics
            recordBuildSeasonMetrics();
            
        } catch (Exception e) {
            logger.error("Failed to collect project metrics", e);
        }
    }
    
    private void recordBuildSeasonMetrics() {
        // Calculate build season progress if in season
        if (isInBuildSeason()) {
            recordGauge("frc.build.season.days.remaining", getDaysUntilCompetition());
            recordGauge("frc.build.season.progress.percent", getBuildSeasonProgressPercent());
            recordGauge("frc.tasks.overdue.count", getOverdueTaskCount());
            recordGauge("frc.milestones.at.risk.count", getAtRiskMilestoneCount());
        }
    }
    
    // Helper methods (these would integrate with actual services)
    private long getActiveSessionCount() {
        // Count active Redis sessions
        return 0; // Placeholder
    }
    
    private long getPendingSyncActions() {
        // Count pending offline sync actions
        return 0; // Placeholder
    }
    
    private long getDatabaseConnectionCount() {
        // Get active database connections from HikariCP
        return 0; // Placeholder
    }
    
    private long getActiveProjectCount() {
        // Count active projects
        return 1; // Placeholder
    }
    
    private long getActiveTeamMemberCount() {
        // Count active team members
        return 15; // Placeholder
    }
    
    private boolean isInBuildSeason() {
        // Check if current date is within build season
        LocalDateTime now = LocalDateTime.now();
        return now.getMonthValue() >= 1 && now.getMonthValue() <= 4;
    }
    
    private long getDaysUntilCompetition() {
        // Calculate days until first competition
        return 30; // Placeholder
    }
    
    private long getBuildSeasonProgressPercent() {
        // Calculate overall build season progress
        return 65; // Placeholder
    }
    
    private long getOverdueTaskCount() {
        // Count overdue tasks
        return 3; // Placeholder
    }
    
    private long getAtRiskMilestoneCount() {
        // Count at-risk milestones
        return 1; // Placeholder
    }
}
```

---

## Step 6: Advanced Analytics and Reporting

### **Advanced Analytics Service**

**File**: `src/main/java/org/frcpm/analytics/AdvancedAnalyticsService.java`
```java
package org.frcpm.analytics;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdvancedAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsService.class);
    
    private final TaskService taskService;
    private final TeamMemberService teamMemberService;
    private final ProjectService projectService;
    private final AttendanceService attendanceService;
    private final MilestoneService milestoneService;
    
    public AdvancedAnalyticsService(TaskService taskService,
                                  TeamMemberService teamMemberService,
                                  ProjectService projectService,
                                  AttendanceService attendanceService,
                                  MilestoneService milestoneService) {
        this.taskService = taskService;
        this.teamMemberService = teamMemberService;
        this.projectService = projectService;
        this.attendanceService = attendanceService;
        this.milestoneService = milestoneService;
    }
    
    /**
     * Generates comprehensive team velocity analysis
     */
    @Async
    public CompletableFuture<TeamVelocityAnalysis> analyzeTeamVelocity(Long projectId, int weeks) {
        Project project = projectService.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(weeks);
        
        List<Task> completedTasks = taskService.findCompletedTasksInPeriod(projectId, startDate, endDate);
        List<TeamMember> teamMembers = teamMemberService.findByProject(projectId);
        
        // Calculate weekly velocity
        Map<Integer, VelocityWeek> weeklyVelocity = new LinkedHashMap<>();
        for (int week = 0; week < weeks; week++) {
            LocalDate weekStart = startDate.plusWeeks(week);
            LocalDate weekEnd = weekStart.plusWeeks(1);
            
            List<Task> weekTasks = completedTasks.stream()
                .filter(task -> isTaskCompletedInWeek(task, weekStart, weekEnd))
                .toList();
            
            VelocityWeek velocityWeek = new VelocityWeek(
                week + 1,
                weekStart,
                weekEnd,
                weekTasks.size(),
                weekTasks.stream().mapToDouble(Task::getEstimatedHours).sum(),
                weekTasks.stream().mapToDouble(Task::getActualHours).sum(),
                calculateStoryPoints(weekTasks)
            );
            
            weeklyVelocity.put(week + 1, velocityWeek);
        }
        
        // Calculate individual member velocity
        Map<TeamMember, MemberVelocity> memberVelocity = teamMembers.stream()
            .collect(Collectors.toMap(
                member -> member,
                member -> calculateMemberVelocity(member, completedTasks, startDate, endDate)
            ));
        
        // Calculate trends and predictions
        VelocityTrend trend = calculateVelocityTrend(weeklyVelocity.values());
        VelocityPrediction prediction = predictFutureVelocity(weeklyVelocity.values(), 4);
        
        TeamVelocityAnalysis analysis = new TeamVelocityAnalysis(
            project,
            startDate,
            endDate,
            weeklyVelocity,
            memberVelocity,
            trend,
            prediction,
            generateVelocityRecommendations(weeklyVelocity, memberVelocity)
        );
        
        logger.info("Generated team velocity analysis for project {}: avg velocity {} tasks/week", 
            projectId, analysis.getAverageVelocity());
        
        return CompletableFuture.completedFuture(analysis);
    }
    
    /**
     * Performs predictive analysis for project completion
     */
    @Async
    public CompletableFuture<ProjectCompletionPrediction> predictProjectCompletion(Long projectId) {
        Project project = projectService.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        List<Task> allTasks = taskService.findByProject(project);
        List<Task> completedTasks = allTasks.stream().filter(Task::isCompleted).toList();
        List<Task> remainingTasks = allTasks.stream().filter(task -> !task.isCompleted()).toList();
        
        // Calculate current progress
        double progressPercent = (double) completedTasks.size() / allTasks.size() * 100;
        
        // Calculate historical velocity (tasks completed per day)
        long daysSinceStart = ChronoUnit.DAYS.between(project.getStartDate(), LocalDate.now());
        double currentVelocity = daysSinceStart > 0 ? (double) completedTasks.size() / daysSinceStart : 0;
        
        // Predict completion based on current velocity
        double daysToComplete = remainingTasks.size() / Math.max(currentVelocity, 0.1);
        LocalDate predictedCompletionDate = LocalDate.now().plusDays((long) daysToComplete);
        
        // Calculate confidence based on velocity consistency
        double velocityConsistency = calculateVelocityConsistency(projectId);
        PredictionConfidence confidence = determineConfidence(velocityConsistency, progressPercent);
        
        // Identify risk factors
        List<RiskFactor> riskFactors = identifyRiskFactors(project, remainingTasks);
        
        // Calculate scenarios
        ProjectScenario optimisticScenario = calculateOptimisticScenario(remainingTasks, currentVelocity);
        ProjectScenario realisticScenario = calculateRealisticScenario(remainingTasks, currentVelocity, riskFactors);
        ProjectScenario pessimisticScenario = calculatePessimisticScenario(remainingTasks, currentVelocity, riskFactors);
        
        ProjectCompletionPrediction prediction = new ProjectCompletionPrediction(
            project,
            progressPercent,
            currentVelocity,
            predictedCompletionDate,
            confidence,
            riskFactors,
            optimisticScenario,
            realisticScenario,
            pessimisticScenario,
            generateCompletionRecommendations(project, remainingTasks, riskFactors)
        );
        
        logger.info("Generated completion prediction for project {}: {}% complete, predicted completion {}", 
            projectId, Math.round(progressPercent), predictedCompletionDate);
        
        return CompletableFuture.completedFuture(prediction);
    }
    
    /**
     * Analyzes team member performance and engagement
     */
    @Async
    public CompletableFuture<TeamPerformanceAnalysis> analyzeTeamPerformance(Long projectId, 
                                                                            LocalDate startDate, 
                                                                            LocalDate endDate) {
        Project project = projectService.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        List<TeamMember> teamMembers = teamMemberService.findByProject(projectId);
        Map<TeamMember, MemberPerformance> performanceMap = new HashMap<>();
        
        for (TeamMember member : teamMembers) {
            MemberPerformance performance = analyzeMemberPerformance(member, startDate, endDate);
            performanceMap.put(member, performance);
        }
        
        // Calculate team-wide metrics
        double averageAttendance = performanceMap.values().stream()
            .mapToDouble(MemberPerformance::attendanceRate)
            .average()
            .orElse(0.0);
        
        double averageTaskCompletion = performanceMap.values().stream()
            .mapToDouble(MemberPerformance::taskCompletionRate)
            .average()
            .orElse(0.0);
        
        // Identify high and low performers
        List<TeamMember> highPerformers = performanceMap.entrySet().stream()
            .filter(entry -> entry.getValue().overallScore() > 0.8)
            .map(Map.Entry::getKey)
            .toList();
        
        List<TeamMember> lowPerformers = performanceMap.entrySet().stream()
            .filter(entry -> entry.getValue().overallScore() < 0.5)
            .map(Map.Entry::getKey)
            .toList();
        
        // Identify skill gaps
        Map<String, Integer> skillDemand = calculateSkillDemand(projectId);
        Map<String, Integer> skillSupply = calculateSkillSupply(teamMembers);
        List<SkillGap> skillGaps = identifySkillGaps(skillDemand, skillSupply);
        
        TeamPerformanceAnalysis analysis = new TeamPerformanceAnalysis(
            project,
            startDate,
            endDate,
            performanceMap,
            averageAttendance,
            averageTaskCompletion,
            highPerformers,
            lowPerformers,
            skillGaps,
            generateTeamRecommendations(performanceMap, skillGaps)
        );
        
        logger.info("Generated team performance analysis for project {}: {} members analyzed", 
            projectId, teamMembers.size());
        
        return CompletableFuture.completedFuture(analysis);
    }
    
    /**
     * Generates build season health report
     */
    @Async
    public CompletableFuture<BuildSeasonHealthReport> generateBuildSeasonHealth(Long projectId) {
        Project project = projectService.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        // Calculate key health indicators
        ScheduleHealth scheduleHealth = calculateScheduleHealth(project);
        TeamHealth teamHealth = calculateTeamHealth(projectId);
        ResourceHealth resourceHealth = calculateResourceHealth(projectId);
        QualityHealth qualityHealth = calculateQualityHealth(projectId);
        
        // Overall health score (0-100)
        double overallHealth = (scheduleHealth.score() + teamHealth.score() + 
                               resourceHealth.score() + qualityHealth.score()) / 4.0;
        
        HealthStatus status = determineHealthStatus(overallHealth);
        
        // Generate action items
        List<ActionItem> actionItems = generateHealthActionItems(
            scheduleHealth, teamHealth, resourceHealth, qualityHealth);
        
        // Calculate risk assessment
        RiskAssessment riskAssessment = calculateProjectRiskAssessment(project, 
            scheduleHealth, teamHealth, resourceHealth);
        
        BuildSeasonHealthReport report = new BuildSeasonHealthReport(
            project,
            LocalDateTime.now(),
            overallHealth,
            status,
            scheduleHealth,
            teamHealth,
            resourceHealth,
            qualityHealth,
            riskAssessment,
            actionItems
        );
        
        logger.info("Generated build season health report for project {}: {} health ({})", 
            projectId, Math.round(overallHealth), status);
        
        return CompletableFuture.completedFuture(report);
    }
    
    // Helper methods for velocity analysis
    private boolean isTaskCompletedInWeek(Task task, LocalDate weekStart, LocalDate weekEnd) {
        LocalDate completionDate = task.getActualEndDate() != null ? 
            task.getActualEndDate() : task.getEndDate();
        return completionDate != null && 
               !completionDate.isBefore(weekStart) && 
               completionDate.isBefore(weekEnd);
    }
    
    private int calculateStoryPoints(List<Task> tasks) {
        // Simple story point calculation based on task complexity
        return tasks.stream()
            .mapToInt(task -> switch (task.getPriority()) {
                case HIGH -> 8;
                case MEDIUM -> 5;
                case LOW -> 3;
            })
            .sum();
    }
    
    private MemberVelocity calculateMemberVelocity(TeamMember member, List<Task> tasks, 
                                                  LocalDate startDate, LocalDate endDate) {
        List<Task> memberTasks = tasks.stream()
            .filter(task -> task.getAssignedTo().contains(member))
            .toList();
        
        double estimatedHours = memberTasks.stream().mapToDouble(Task::getEstimatedHours).sum();
        double actualHours = memberTasks.stream().mapToDouble(Task::getActualHours).sum();
        
        return new MemberVelocity(
            member,
            memberTasks.size(),
            estimatedHours,
            actualHours,
            actualHours > 0 ? estimatedHours / actualHours : 1.0
        );
    }
    
    private VelocityTrend calculateVelocityTrend(Collection<VelocityWeek> weeks) {
        List<VelocityWeek> weekList = new ArrayList<>(weeks);
        if (weekList.size() < 2) {
            return new VelocityTrend(TrendDirection.STABLE, 0.0, 0.0);
        }
        
        // Simple linear regression for trend
        double[] weekNumbers = weekList.stream().mapToDouble(VelocityWeek::weekNumber).toArray();
        double[] taskCounts = weekList.stream().mapToDouble(VelocityWeek::tasksCompleted).toArray();
        
        double slope = calculateSlope(weekNumbers, taskCounts);
        double correlation = calculateCorrelation(weekNumbers, taskCounts);
        
        TrendDirection direction = slope > 0.1 ? TrendDirection.IMPROVING :
                                 slope < -0.1 ? TrendDirection.DECLINING : 
                                 TrendDirection.STABLE;
        
        return new VelocityTrend(direction, slope, correlation);
    }
    
    private VelocityPrediction predictFutureVelocity(Collection<VelocityWeek> weeks, int futureWeeks) {
        List<VelocityWeek> weekList = new ArrayList<>(weeks);
        double averageVelocity = weekList.stream()
            .mapToDouble(VelocityWeek::tasksCompleted)
            .average()
            .orElse(0.0);
        
        VelocityTrend trend = calculateVelocityTrend(weeks);
        
        List<Double> predictions = new ArrayList<>();
        for (int i = 1; i <= futureWeeks; i++) {
            double prediction = averageVelocity + (trend.slope() * (weekList.size() + i));
            predictions.add(Math.max(0, prediction)); // Ensure non-negative
        }
        
        return new VelocityPrediction(futureWeeks, predictions, trend.correlation());
    }
    
    // Mathematical helper methods
    private double calculateSlope(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) return 0.0;
        
        double sumX = Arrays.stream(x).sum();
        double sumY = Arrays.stream(y).sum();
        double sumXY = 0.0;
        double sumXX = 0.0;
        
        for (int i = 0; i < x.length; i++) {
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        
        double denominator = (x.length * sumXX - sumX * sumX);
        return denominator != 0 ? (x.length * sumXY - sumX * sumY) / denominator : 0.0;
    }
    
    private double calculateCorrelation(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) return 0.0;
        
        double meanX = Arrays.stream(x).average().orElse(0.0);
        double meanY = Arrays.stream(y).average().orElse(0.0);
        
        double numerator = 0.0;
        double sumXDiff = 0.0;
        double sumYDiff = 0.0;
        
        for (int i = 0; i < x.length; i++) {
            double xDiff = x[i] - meanX;
            double yDiff = y[i] - meanY;
            numerator += xDiff * yDiff;
            sumXDiff += xDiff * xDiff;
            sumYDiff += yDiff * yDiff;
        }
        
        double denominator = Math.sqrt(sumXDiff * sumYDiff);
        return denominator != 0 ? numerator / denominator : 0.0;
    }
    
    // Additional analysis methods would be implemented here...
    // (Performance analysis, risk assessment, health calculations, etc.)
    
    private List<String> generateVelocityRecommendations(Map<Integer, VelocityWeek> weeklyVelocity,
                                                        Map<TeamMember, MemberVelocity> memberVelocity) {
        List<String> recommendations = new ArrayList<>();
        
        // Analyze velocity trend
        VelocityTrend trend = calculateVelocityTrend(weeklyVelocity.values());
        if (trend.direction() == TrendDirection.DECLINING) {
            recommendations.add("Team velocity is declining. Consider reviewing task complexity and team capacity.");
        }
        
        // Analyze member performance
        long lowPerformers = memberVelocity.values().stream()
            .filter(mv -> mv.efficiency() < 0.8)
            .count();
        
        if (lowPerformers > 0) {
            recommendations.add(String.format("%d team members showing low efficiency. Consider additional training or task reassignment.", lowPerformers));
        }
        
        return recommendations;
    }
}

// Supporting data classes and enums
enum TrendDirection { IMPROVING, STABLE, DECLINING }
enum PredictionConfidence { HIGH, MEDIUM, LOW }
enum HealthStatus { EXCELLENT, GOOD, FAIR, POOR, CRITICAL }

record VelocityWeek(
    int weekNumber,
    LocalDate startDate,
    LocalDate endDate,
    int tasksCompleted,
    double estimatedHours,
    double actualHours,
    int storyPoints
) {}

record MemberVelocity(
    TeamMember member,
    int tasksCompleted,
    double estimatedHours,
    double actualHours,
    double efficiency
) {}

record VelocityTrend(
    TrendDirection direction,
    double slope,
    double correlation
) {}

record VelocityPrediction(
    int weeks,
    List<Double> predictions,
    double confidence
) {}

record TeamVelocityAnalysis(
    Project project,
    LocalDate startDate,
    LocalDate endDate,
    Map<Integer, VelocityWeek> weeklyVelocity,
    Map<TeamMember, MemberVelocity> memberVelocity,
    VelocityTrend trend,
    VelocityPrediction prediction,
    List<String> recommendations
) {
    public double getAverageVelocity() {
        return weeklyVelocity.values().stream()
            .mapToDouble(VelocityWeek::tasksCompleted)
            .average()
            .orElse(0.0);
    }
}
```