// src/main/java/org/frcpm/services/impl/BuildLogEntryServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.BuildLogEntry;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.BuildLogEntryRepository;
import org.frcpm.services.BuildLogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of BuildLogEntryService.
 * 
 * Provides comprehensive visual build log management services including
 * timeline generation, photo management, progress tracking, milestone
 * documentation, and collaborative build process documentation with
 * sophisticated analytics and reporting capabilities.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.4 Visual Build Log with Photo Timeline
 */
@Service
@Transactional
public class BuildLogEntryServiceImpl implements BuildLogEntryService {

    @Autowired
    private BuildLogEntryRepository entryRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public BuildLogEntry create(BuildLogEntry entry) {
        return createEntry(entry);
    }

    @Override
    public BuildLogEntry update(Long id, BuildLogEntry entry) {
        return updateEntry(id, entry);
    }

    @Override
    public void delete(Long id) {
        archiveEntry(id, "Deleted by user");
    }

    @Override
    public Optional<BuildLogEntry> findById(Long id) {
        return entryRepository.findById(id);
    }

    @Override
    public List<BuildLogEntry> findAll() {
        return entryRepository.findAll().stream()
                .filter(BuildLogEntry::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return entryRepository.existsById(id);
    }

    @Override
    public long count() {
        return entryRepository.count();
    }

    // =========================================================================
    // ENTRY MANAGEMENT
    // =========================================================================

    @Override
    public BuildLogEntry createEntry(BuildLogEntry entry) {
        validateEntry(entry);
        
        // Set defaults
        if (entry.getEntryDateTime() == null) {
            entry.setEntryDateTime(LocalDateTime.now());
        }
        if (entry.getVisibility() == null) {
            entry.setVisibility(BuildLogEntry.VisibilityLevel.TEAM_ONLY);
        }
        if (entry.getCompletionStatus() == null) {
            entry.setCompletionStatus(BuildLogEntry.CompletionStatus.IN_PROGRESS);
        }
        if (entry.getProgressPercentage() == null) {
            entry.setProgressPercentage(0.0);
        }
        
        // Initialize collections
        if (entry.getPhotoUrls() == null) {
            entry.setPhotoUrls(new ArrayList<>());
        }
        if (entry.getPhotoCaptions() == null) {
            entry.setPhotoCaptions(new ArrayList<>());
        }
        if (entry.getParticipantIds() == null) {
            entry.setParticipantIds(new ArrayList<>());
        }
        if (entry.getParticipantNames() == null) {
            entry.setParticipantNames(new ArrayList<>());
        }
        if (entry.getTags() == null) {
            entry.setTags(new ArrayList<>());
        }
        
        // Set audit fields
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setIsActive(true);
        
        // Calculate work duration if times are set
        if (entry.getWorkStartTime() != null && entry.getWorkEndTime() != null) {
            entry.calculateWorkDuration();
        }
        
        // Update content quality score
        entry.updateContentQuality();
        
        BuildLogEntry saved = entryRepository.save(entry);
        
        // Auto-generate tags if none provided
        if (saved.getTags().isEmpty()) {
            autoGenerateTags(saved.getId());
        }
        
        return saved;
    }

    @Override
    public BuildLogEntry createEntry(Integer teamNumber, Integer season, String title, 
                                   BuildLogEntry.EntryType entryType, BuildLogEntry.BuildPhase buildPhase,
                                   TeamMember createdBy) {
        BuildLogEntry entry = new BuildLogEntry(teamNumber, season, title, entryType, buildPhase, createdBy);
        return createEntry(entry);
    }

    @Override
    public BuildLogEntry createWorkSession(Integer teamNumber, Integer season, String title, String workAccomplished,
                                         LocalDateTime startTime, LocalDateTime endTime, TeamMember createdBy) {
        BuildLogEntry entry = new BuildLogEntry(teamNumber, season, title, 
                BuildLogEntry.EntryType.WORK_SESSION, BuildLogEntry.BuildPhase.FABRICATION, createdBy);
        entry.setWorkAccomplished(workAccomplished);
        entry.setWorkStartTime(startTime);
        entry.setWorkEndTime(endTime);
        entry.calculateWorkDuration();
        
        return createEntry(entry);
    }

    @Override
    public BuildLogEntry createMilestone(Integer teamNumber, Integer season, String title, String milestoneDescription,
                                       Double progressPercentage, TeamMember createdBy) {
        BuildLogEntry entry = new BuildLogEntry(teamNumber, season, title, 
                BuildLogEntry.EntryType.MILESTONE, BuildLogEntry.BuildPhase.ASSEMBLY, createdBy);
        entry.setMilestoneDescription(milestoneDescription);
        entry.setProgressPercentage(progressPercentage);
        entry.setIsMilestone(true);
        entry.setIsHighlight(true);
        entry.setCompletionStatus(BuildLogEntry.CompletionStatus.COMPLETED);
        
        return createEntry(entry);
    }

    @Override
    public BuildLogEntry updateEntry(Long entryId, BuildLogEntry entry) {
        BuildLogEntry existing = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        // Update fields
        if (entry.getTitle() != null) {
            existing.setTitle(entry.getTitle());
        }
        if (entry.getDescription() != null) {
            existing.setDescription(entry.getDescription());
        }
        if (entry.getWorkAccomplished() != null) {
            existing.setWorkAccomplished(entry.getWorkAccomplished());
        }
        if (entry.getLessonsLearned() != null) {
            existing.setLessonsLearned(entry.getLessonsLearned());
        }
        if (entry.getNextSteps() != null) {
            existing.setNextSteps(entry.getNextSteps());
        }
        if (entry.getBuildPhase() != null) {
            existing.setBuildPhase(entry.getBuildPhase());
        }
        if (entry.getPriority() != null) {
            existing.setPriority(entry.getPriority());
        }
        if (entry.getCompletionStatus() != null) {
            existing.setCompletionStatus(entry.getCompletionStatus());
        }
        if (entry.getProgressPercentage() != null) {
            existing.setProgressPercentage(entry.getProgressPercentage());
        }
        if (entry.getSubsystem() != null) {
            existing.setSubsystem(entry.getSubsystem());
        }
        if (entry.getComponent() != null) {
            existing.setComponent(entry.getComponent());
        }
        
        // Update technical fields
        if (entry.getTechnicalNotes() != null) {
            existing.setTechnicalNotes(entry.getTechnicalNotes());
        }
        if (entry.getSafetyNotes() != null) {
            existing.setSafetyNotes(entry.getSafetyNotes());
        }
        if (entry.getProblemsEncountered() != null) {
            existing.setProblemsEncountered(entry.getProblemsEncountered());
        }
        if (entry.getSolutionsImplemented() != null) {
            existing.setSolutionsImplemented(entry.getSolutionsImplemented());
        }
        
        // Update collections
        if (entry.getMaterialsUsed() != null) {
            existing.setMaterialsUsed(new ArrayList<>(entry.getMaterialsUsed()));
        }
        if (entry.getToolsUsed() != null) {
            existing.setToolsUsed(new ArrayList<>(entry.getToolsUsed()));
        }
        if (entry.getTechniquesUsed() != null) {
            existing.setTechniquesUsed(new ArrayList<>(entry.getTechniquesUsed()));
        }
        if (entry.getTags() != null) {
            existing.setTags(new ArrayList<>(entry.getTags()));
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(entry.getUpdatedBy());
        
        // Recalculate quality and engagement
        existing.updateContentQuality();
        existing.updateEngagementScore();
        
        return entryRepository.save(existing);
    }

    @Override
    public void archiveEntry(Long entryId, String reason) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsArchived(true);
        entry.setArchiveReason(reason);
        entry.setArchivedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        
        entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findActiveEntries(Integer teamNumber, Integer season) {
        return entryRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findByEntryType(Integer teamNumber, Integer season, BuildLogEntry.EntryType entryType) {
        return entryRepository.findByTeamNumberAndSeasonAndEntryTypeAndIsActiveTrue(teamNumber, season, entryType);
    }

    @Override
    public List<BuildLogEntry> findByBuildPhase(Integer teamNumber, Integer season, BuildLogEntry.BuildPhase buildPhase) {
        return entryRepository.findByTeamNumberAndSeasonAndBuildPhaseAndIsActiveTrue(teamNumber, season, buildPhase);
    }

    // =========================================================================
    // TIMELINE OPERATIONS
    // =========================================================================

    @Override
    public List<BuildLogEntry> generateTimeline(Integer teamNumber, Integer season) {
        return entryRepository.findTimelineEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findTimelineEntriesInDateRange(Integer teamNumber, Integer season, 
                                                            LocalDateTime startDate, LocalDateTime endDate) {
        return entryRepository.findTimelineEntriesInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<BuildLogEntry> findTimelineHighlights(Integer teamNumber, Integer season) {
        return entryRepository.findTimelineHighlights(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findByBuildSeasonDay(Integer teamNumber, Integer season, Integer day) {
        return entryRepository.findByBuildSeasonDay(teamNumber, season, day);
    }

    @Override
    public List<BuildLogEntry> findByBuildWeek(Integer teamNumber, Integer season, Integer week) {
        return entryRepository.findByBuildWeek(teamNumber, season, week);
    }

    @Override
    public List<BuildLogEntry> findRecentEntries(Integer teamNumber, Integer season, Integer days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return entryRepository.findRecentEntries(teamNumber, season, since);
    }

    @Override
    public Map<String, Object> generateTimelineData(Integer teamNumber, Integer season) {
        Map<String, Object> data = new HashMap<>();
        
        List<BuildLogEntry> entries = generateTimeline(teamNumber, season);
        List<BuildLogEntry> highlights = findTimelineHighlights(teamNumber, season);
        
        data.put("entries", entries);
        data.put("highlights", highlights);
        data.put("totalEntries", entries.size());
        
        // Group by week for visualization
        Map<Integer, List<BuildLogEntry>> byWeek = entries.stream()
                .filter(e -> e.getBuildWeek() != null)
                .collect(Collectors.groupingBy(BuildLogEntry::getBuildWeek));
        data.put("weeklyEntries", byWeek);
        
        // Group by phase
        Map<BuildLogEntry.BuildPhase, List<BuildLogEntry>> byPhase = entries.stream()
                .collect(Collectors.groupingBy(BuildLogEntry::getBuildPhase));
        data.put("phaseEntries", byPhase);
        
        // Calculate progress trend
        List<Map<String, Object>> progressTrend = new ArrayList<>();
        for (BuildLogEntry entry : entries) {
            if (entry.getProgressPercentage() != null) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", entry.getEntryDateTime());
                point.put("progress", entry.getProgressPercentage());
                point.put("title", entry.getTitle());
                progressTrend.add(point);
            }
        }
        data.put("progressTrend", progressTrend);
        
        return data;
    }

    @Override
    public void calculateBuildSeasonDay(Long entryId, LocalDateTime kickoffDate) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.calculateBuildSeasonDay(kickoffDate);
        entryRepository.save(entry);
    }

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    @Override
    public List<BuildLogEntry> searchByTitle(Integer teamNumber, Integer season, String searchTerm) {
        return entryRepository.searchByTitle(teamNumber, season, searchTerm);
    }

    @Override
    public List<BuildLogEntry> searchByContent(Integer teamNumber, Integer season, String searchTerm) {
        return entryRepository.searchByContent(teamNumber, season, searchTerm);
    }

    @Override
    public List<BuildLogEntry> fullTextSearch(Integer teamNumber, Integer season, String searchTerm) {
        return entryRepository.fullTextSearch(teamNumber, season, searchTerm);
    }

    @Override
    public List<BuildLogEntry> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria) {
        List<BuildLogEntry> results = findActiveEntries(teamNumber, season);
        
        // Filter by entry type
        if (criteria.containsKey("entryType")) {
            BuildLogEntry.EntryType entryType = (BuildLogEntry.EntryType) criteria.get("entryType");
            results = results.stream()
                    .filter(e -> e.getEntryType() == entryType)
                    .collect(Collectors.toList());
        }
        
        // Filter by build phase
        if (criteria.containsKey("buildPhase")) {
            BuildLogEntry.BuildPhase buildPhase = (BuildLogEntry.BuildPhase) criteria.get("buildPhase");
            results = results.stream()
                    .filter(e -> e.getBuildPhase() == buildPhase)
                    .collect(Collectors.toList());
        }
        
        // Filter by subsystem
        if (criteria.containsKey("subsystem")) {
            String subsystem = (String) criteria.get("subsystem");
            results = results.stream()
                    .filter(e -> subsystem.equalsIgnoreCase(e.getSubsystem()))
                    .collect(Collectors.toList());
        }
        
        // Filter by date range
        if (criteria.containsKey("startDate") && criteria.containsKey("endDate")) {
            LocalDateTime startDate = (LocalDateTime) criteria.get("startDate");
            LocalDateTime endDate = (LocalDateTime) criteria.get("endDate");
            results = results.stream()
                    .filter(e -> e.getEntryDateTime().isAfter(startDate) && e.getEntryDateTime().isBefore(endDate))
                    .collect(Collectors.toList());
        }
        
        // Filter by quality score
        if (criteria.containsKey("minQuality")) {
            Integer minQuality = (Integer) criteria.get("minQuality");
            results = results.stream()
                    .filter(e -> e.getContentQualityScore() >= minQuality)
                    .collect(Collectors.toList());
        }
        
        // Filter by photo count
        if (criteria.containsKey("hasPhotos")) {
            Boolean hasPhotos = (Boolean) criteria.get("hasPhotos");
            if (hasPhotos) {
                results = results.stream()
                        .filter(e -> e.getPhotoCount() > 0)
                        .collect(Collectors.toList());
            }
        }
        
        return results;
    }

    @Override
    public List<BuildLogEntry> findByTag(Integer teamNumber, Integer season, String tag) {
        return entryRepository.findByTag(teamNumber, season, tag);
    }

    @Override
    public List<BuildLogEntry> findBySubsystem(Integer teamNumber, Integer season, String subsystem) {
        return entryRepository.findBySubsystem(teamNumber, season, subsystem);
    }

    @Override
    public List<BuildLogEntry> findByComponent(Integer teamNumber, Integer season, String component) {
        return entryRepository.findByComponent(teamNumber, season, component);
    }

    @Override
    public List<BuildLogEntry> findByCompletionStatus(Integer teamNumber, Integer season, 
                                                    BuildLogEntry.CompletionStatus status) {
        return entryRepository.findByTeamNumberAndSeasonAndCompletionStatusAndIsActiveTrue(teamNumber, season, status);
    }

    // =========================================================================
    // PHOTO AND MEDIA MANAGEMENT
    // =========================================================================

    @Override
    public BuildLogEntry addPhoto(Long entryId, String photoUrl, String caption) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.addPhoto(photoUrl, caption);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry removePhoto(Long entryId, String photoUrl) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.removePhoto(photoUrl);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry setPrimaryPhoto(Long entryId, String photoUrl, String caption) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setPrimaryPhotoUrl(photoUrl);
        entry.setPrimaryPhotoCaption(caption);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry addVideo(Long entryId, String videoUrl) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getVideoUrls() == null) {
            entry.setVideoUrls(new ArrayList<>());
        }
        
        entry.getVideoUrls().add(videoUrl);
        entry.setVideoCount(entry.getVideoUrls().size());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.updateContentQuality();
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry removeVideo(Long entryId, String videoUrl) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getVideoUrls() != null && entry.getVideoUrls().remove(videoUrl)) {
            entry.setVideoCount(entry.getVideoUrls().size());
            entry.setUpdatedAt(LocalDateTime.now());
            entry.updateContentQuality();
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithPhotos(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithPhotos(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithVideos(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithVideos(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findPhotoRichEntries(Integer teamNumber, Integer season, Integer minPhotos) {
        return entryRepository.findPhotoRichEntries(teamNumber, season, minPhotos);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithoutMedia(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithoutMedia(teamNumber, season);
    }

    @Override
    public List<String> getAllPhotoUrls(Integer teamNumber, Integer season) {
        return entryRepository.findAllPhotoUrls(teamNumber, season);
    }

    @Override
    public Map<String, Object> generatePhotoGalleryData(Integer teamNumber, Integer season) {
        Map<String, Object> data = new HashMap<>();
        
        List<BuildLogEntry> photoEntries = findEntriesWithPhotos(teamNumber, season);
        List<String> allPhotos = getAllPhotoUrls(teamNumber, season);
        
        data.put("photoEntries", photoEntries);
        data.put("allPhotoUrls", allPhotos);
        data.put("totalPhotos", allPhotos.size());
        
        // Group photos by month for timeline view
        Map<String, List<BuildLogEntry>> photosByMonth = photoEntries.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getEntryDateTime().getYear() + "-" + 
                         String.format("%02d", e.getEntryDateTime().getMonthValue())
                ));
        data.put("photosByMonth", photosByMonth);
        
        // Find featured photos
        List<BuildLogEntry> featuredPhotos = photoEntries.stream()
                .filter(BuildLogEntry::getIsFeatured)
                .limit(20)
                .collect(Collectors.toList());
        data.put("featuredPhotos", featuredPhotos);
        
        return data;
    }

    // =========================================================================
    // PROJECT AND TASK ASSOCIATIONS
    // =========================================================================

    @Override
    public BuildLogEntry associateWithProject(Long entryId, Project project) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setRelatedProject(project);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry associateWithTask(Long entryId, Task task) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setRelatedTask(task);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry removeProjectAssociation(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setRelatedProject(null);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry removeTaskAssociation(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setRelatedTask(null);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findByProject(Project project, Integer teamNumber, Integer season) {
        return entryRepository.findByRelatedProjectAndTeamNumberAndSeasonAndIsActiveTrue(project, teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findByTask(Task task, Integer teamNumber, Integer season) {
        return entryRepository.findByRelatedTaskAndTeamNumberAndSeasonAndIsActiveTrue(task, teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findUnassociatedEntries(Integer teamNumber, Integer season) {
        return entryRepository.findUnassociatedEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithoutTasks(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithoutTasks(teamNumber, season);
    }

    // =========================================================================
    // PARTICIPANT MANAGEMENT
    // =========================================================================

    @Override
    public BuildLogEntry addParticipant(Long entryId, Long participantId, String participantName) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.addParticipant(participantId, participantName);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry removeParticipant(Long entryId, Long participantId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.removeParticipant(participantId);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findByCreator(TeamMember creator, Integer teamNumber, Integer season) {
        return entryRepository.findByCreatedByAndTeamNumberAndSeasonAndIsActiveTrue(creator, teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findByParticipant(Integer teamNumber, Integer season, Long participantId) {
        return entryRepository.findByParticipant(teamNumber, season, participantId);
    }

    @Override
    public Map<String, Object> getParticipantStatistics(Integer teamNumber, Integer season, Long participantId) {
        List<BuildLogEntry> participantEntries = findByParticipant(teamNumber, season, participantId);
        List<BuildLogEntry> createdEntries = participantEntries.stream()
                .filter(e -> e.getCreatedBy().getId().equals(participantId))
                .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalParticipations", participantEntries.size());
        stats.put("entriesCreated", createdEntries.size());
        
        // Calculate work hours
        double totalWorkHours = participantEntries.stream()
                .filter(e -> e.getWorkDurationMinutes() != null)
                .mapToDouble(e -> e.getWorkDurationMinutes() / 60.0)
                .sum();
        stats.put("totalWorkHours", totalWorkHours);
        
        // Most active subsystems
        Map<String, Long> subsystemCounts = participantEntries.stream()
                .filter(e -> e.getSubsystem() != null)
                .collect(Collectors.groupingBy(BuildLogEntry::getSubsystem, Collectors.counting()));
        stats.put("topSubsystems", subsystemCounts);
        
        // Average quality score
        double avgQuality = participantEntries.stream()
                .mapToDouble(BuildLogEntry::getContentQualityScore)
                .average().orElse(0.0);
        stats.put("averageQualityScore", avgQuality);
        
        return stats;
    }

    @Override
    public List<Object[]> findMostActiveContributors(Integer teamNumber, Integer season) {
        // This would need a specialized query to count participations by user
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    // =========================================================================
    // PROGRESS AND MILESTONE TRACKING
    // =========================================================================

    @Override
    public BuildLogEntry updateProgress(Long entryId, Double progressPercentage) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setProgressPercentage(progressPercentage);
        
        // Auto-update completion status based on progress
        if (progressPercentage >= 100.0) {
            entry.setCompletionStatus(BuildLogEntry.CompletionStatus.COMPLETED);
        } else if (progressPercentage > 0.0) {
            entry.setCompletionStatus(BuildLogEntry.CompletionStatus.IN_PROGRESS);
        }
        
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry markAsMilestone(Long entryId, String milestoneDescription) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsMilestone(true);
        entry.setMilestoneDescription(milestoneDescription);
        entry.setIsHighlight(true);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry unmarkAsMilestone(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsMilestone(false);
        entry.setMilestoneDescription(null);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findMilestoneEntries(Integer teamNumber, Integer season) {
        return entryRepository.findMilestoneEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findByProgressRange(Integer teamNumber, Integer season, 
                                                 Double minProgress, Double maxProgress) {
        return entryRepository.findByProgressRange(teamNumber, season, minProgress, maxProgress);
    }

    @Override
    public List<BuildLogEntry> findCompletedEntries(Integer teamNumber, Integer season) {
        return entryRepository.findCompletedEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findProblematicEntries(Integer teamNumber, Integer season) {
        return entryRepository.findProblematicEntries(teamNumber, season);
    }

    @Override
    public Double calculateOverallProgress(Integer teamNumber, Integer season) {
        List<BuildLogEntry> entries = findActiveEntries(teamNumber, season);
        
        if (entries.isEmpty()) {
            return 0.0;
        }
        
        double totalProgress = entries.stream()
                .filter(e -> e.getProgressPercentage() != null)
                .mapToDouble(BuildLogEntry::getProgressPercentage)
                .average().orElse(0.0);
        
        return totalProgress;
    }

    @Override
    public Map<String, Object> generateProgressReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<BuildLogEntry> entries = findActiveEntries(teamNumber, season);
        List<BuildLogEntry> milestones = findMilestoneEntries(teamNumber, season);
        List<BuildLogEntry> completed = findCompletedEntries(teamNumber, season);
        List<BuildLogEntry> problematic = findProblematicEntries(teamNumber, season);
        
        report.put("totalEntries", entries.size());
        report.put("milestoneCount", milestones.size());
        report.put("completedCount", completed.size());
        report.put("problematicCount", problematic.size());
        report.put("overallProgress", calculateOverallProgress(teamNumber, season));
        
        // Progress by subsystem
        Map<String, Double> subsystemProgress = entries.stream()
                .filter(e -> e.getSubsystem() != null && e.getProgressPercentage() != null)
                .collect(Collectors.groupingBy(
                    BuildLogEntry::getSubsystem,
                    Collectors.averagingDouble(BuildLogEntry::getProgressPercentage)
                ));
        report.put("subsystemProgress", subsystemProgress);
        
        // Progress by build phase
        Map<BuildLogEntry.BuildPhase, Double> phaseProgress = entries.stream()
                .filter(e -> e.getProgressPercentage() != null)
                .collect(Collectors.groupingBy(
                    BuildLogEntry::getBuildPhase,
                    Collectors.averagingDouble(BuildLogEntry::getProgressPercentage)
                ));
        report.put("phaseProgress", phaseProgress);
        
        return report;
    }

    // =========================================================================
    // TECHNICAL DOCUMENTATION
    // =========================================================================

    @Override
    public BuildLogEntry addMaterial(Long entryId, String material) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getMaterialsUsed() == null) {
            entry.setMaterialsUsed(new ArrayList<>());
        }
        
        if (!entry.getMaterialsUsed().contains(material)) {
            entry.getMaterialsUsed().add(material);
            entry.setUpdatedAt(LocalDateTime.now());
            entry.updateContentQuality();
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry addTool(Long entryId, String tool) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getToolsUsed() == null) {
            entry.setToolsUsed(new ArrayList<>());
        }
        
        if (!entry.getToolsUsed().contains(tool)) {
            entry.getToolsUsed().add(tool);
            entry.setUpdatedAt(LocalDateTime.now());
            entry.updateContentQuality();
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry addTechnique(Long entryId, String technique) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getTechniquesUsed() == null) {
            entry.setTechniquesUsed(new ArrayList<>());
        }
        
        if (!entry.getTechniquesUsed().contains(technique)) {
            entry.getTechniquesUsed().add(technique);
            entry.setUpdatedAt(LocalDateTime.now());
            entry.updateContentQuality();
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry updateTechnicalNotes(Long entryId, String technicalNotes) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setTechnicalNotes(technicalNotes);
        entry.setUpdatedAt(LocalDateTime.now());
        entry.updateContentQuality();
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry updateSafetyNotes(Long entryId, String safetyNotes) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setSafetyNotes(safetyNotes);
        entry.setUpdatedAt(LocalDateTime.now());
        entry.updateContentQuality();
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry addMeasurement(Long entryId, Double value, String unit, String description) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setMeasurementValue(value);
        entry.setMeasurementUnit(unit);
        entry.setMeasurementDescription(description);
        entry.setUpdatedAt(LocalDateTime.now());
        entry.updateContentQuality();
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findByMaterial(Integer teamNumber, Integer season, String material) {
        return entryRepository.findByMaterial(teamNumber, season, material);
    }

    @Override
    public List<BuildLogEntry> findByTool(Integer teamNumber, Integer season, String tool) {
        return entryRepository.findByTool(teamNumber, season, tool);
    }

    @Override
    public List<BuildLogEntry> findByTechnique(Integer teamNumber, Integer season, String technique) {
        return entryRepository.findByTechnique(teamNumber, season, technique);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithSafetyNotes(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithSafetyNotes(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithMeasurements(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithMeasurements(teamNumber, season);
    }

    // =========================================================================
    // WORK SESSION TRACKING
    // =========================================================================

    @Override
    public BuildLogEntry setWorkSession(Long entryId, LocalDateTime startTime, LocalDateTime endTime) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setWorkSession(startTime, endTime);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry recordWorkDuration(Long entryId, Integer durationMinutes) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setWorkDurationMinutes(durationMinutes);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findEntriesWithWorkDuration(Integer teamNumber, Integer season) {
        return entryRepository.findEntriesWithWorkDuration(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findLongWorkSessions(Integer teamNumber, Integer season, Integer minMinutes) {
        return entryRepository.findLongWorkSessions(teamNumber, season, minMinutes);
    }

    @Override
    public Double calculateTotalWorkHours(Integer teamNumber, Integer season) {
        Long totalMinutes = entryRepository.calculateTotalWorkMinutes(teamNumber, season).orElse(0L);
        return totalMinutes / 60.0;
    }

    @Override
    public List<BuildLogEntry> findByWorkLocation(Integer teamNumber, Integer season, String location) {
        return entryRepository.findByWorkLocation(teamNumber, season, location);
    }

    @Override
    public Map<String, Object> generateWorkSessionReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<BuildLogEntry> workSessions = findEntriesWithWorkDuration(teamNumber, season);
        Double totalHours = calculateTotalWorkHours(teamNumber, season);
        
        report.put("totalWorkSessions", workSessions.size());
        report.put("totalWorkHours", totalHours);
        
        if (!workSessions.isEmpty()) {
            double avgDuration = workSessions.stream()
                    .mapToDouble(e -> e.getWorkDurationMinutes() / 60.0)
                    .average().orElse(0.0);
            report.put("averageSessionHours", avgDuration);
            
            // Longest session
            Optional<BuildLogEntry> longestSession = workSessions.stream()
                    .max(Comparator.comparing(BuildLogEntry::getWorkDurationMinutes));
            if (longestSession.isPresent()) {
                report.put("longestSessionHours", longestSession.get().getWorkDurationMinutes() / 60.0);
            }
        }
        
        // Work by location
        Map<String, Long> locationCounts = workSessions.stream()
                .filter(e -> e.getWorkLocation() != null)
                .collect(Collectors.groupingBy(BuildLogEntry::getWorkLocation, Collectors.counting()));
        report.put("workByLocation", locationCounts);
        
        return report;
    }

    // =========================================================================
    // QUALITY AND ENGAGEMENT
    // =========================================================================

    @Override
    public BuildLogEntry updateContentQuality(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.updateContentQuality();
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry recordView(Long entryId, Long userId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setViewCount(entry.getViewCount() + 1);
        entry.setLastViewed(LocalDateTime.now());
        entry.updateEngagementScore();
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry recordHelpfulVote(Long entryId, Long userId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setHelpfulVotes(entry.getHelpfulVotes() + 1);
        entry.updateEngagementScore();
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry updateEngagementMetrics(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.updateEngagementScore();
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findMostViewed(Integer teamNumber, Integer season, Integer limit) {
        return entryRepository.findMostViewed(teamNumber, season).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildLogEntry> findHighEngagement(Integer teamNumber, Integer season, Double minScore, Integer limit) {
        return entryRepository.findHighEngagement(teamNumber, season, minScore).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildLogEntry> findHighQuality(Integer teamNumber, Integer season, Integer minScore, Integer limit) {
        return entryRepository.findHighQuality(teamNumber, season, minScore).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildLogEntry> findMostHelpful(Integer teamNumber, Integer season, Integer minVotes, Integer limit) {
        return entryRepository.findMostHelpful(teamNumber, season, minVotes).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public BuildLogEntry setFeatured(Long entryId, boolean featured) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsFeatured(featured);
        if (featured) {
            entry.setIsHighlight(true);
        }
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findFeaturedEntries(Integer teamNumber, Integer season) {
        return entryRepository.findFeaturedEntries(teamNumber, season);
    }

    // =========================================================================
    // COMPETITION AND EVENT TRACKING
    // =========================================================================

    @Override
    public BuildLogEntry setTargetEvent(Long entryId, String event, LocalDateTime targetDate) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setTargetEvent(event);
        entry.setTargetDate(targetDate);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry markCompetitionReady(Long entryId, boolean ready) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsCompetitionReady(ready);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findByTargetEvent(Integer teamNumber, Integer season, String event) {
        return entryRepository.findByTargetEvent(teamNumber, season, event);
    }

    @Override
    public List<BuildLogEntry> findCompetitionReadyEntries(Integer teamNumber, Integer season) {
        return entryRepository.findCompetitionReadyEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findWithApproachingDeadlines(Integer teamNumber, Integer season, Integer days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);
        return entryRepository.findWithApproachingDeadlines(teamNumber, season, now, deadline);
    }

    @Override
    public Map<String, Object> generateCompetitionReadinessReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        List<BuildLogEntry> readyEntries = findCompetitionReadyEntries(teamNumber, season);
        List<BuildLogEntry> approachingDeadlines = findWithApproachingDeadlines(teamNumber, season, 7);
        
        report.put("competitionReadyCount", readyEntries.size());
        report.put("approachingDeadlines", approachingDeadlines.size());
        
        // Readiness by subsystem
        Map<String, Long> readinessBySubsystem = readyEntries.stream()
                .filter(e -> e.getSubsystem() != null)
                .collect(Collectors.groupingBy(BuildLogEntry::getSubsystem, Collectors.counting()));
        report.put("readinessBySubsystem", readinessBySubsystem);
        
        // Target events
        Map<String, Long> targetEvents = readyEntries.stream()
                .filter(e -> e.getTargetEvent() != null)
                .collect(Collectors.groupingBy(BuildLogEntry::getTargetEvent, Collectors.counting()));
        report.put("targetEvents", targetEvents);
        
        return report;
    }

    // =========================================================================
    // TAGGING AND CATEGORIZATION
    // =========================================================================

    @Override
    public BuildLogEntry addTag(Long entryId, String tag) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getTags() == null) {
            entry.setTags(new ArrayList<>());
        }
        
        if (!entry.getTags().contains(tag.toLowerCase())) {
            entry.getTags().add(tag.toLowerCase());
            entry.setUpdatedAt(LocalDateTime.now());
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry removeTag(Long entryId, String tag) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getTags() != null && entry.getTags().remove(tag.toLowerCase())) {
            entry.setUpdatedAt(LocalDateTime.now());
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry setTags(Long entryId, List<String> tags) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        List<String> lowerCaseTags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        
        entry.setTags(lowerCaseTags);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public Map<String, Long> findMostCommonTags(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.findMostCommonTags(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1],
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Object> generateTagCloudData(Integer teamNumber, Integer season) {
        Map<String, Long> tagCounts = findMostCommonTags(teamNumber, season);
        
        Map<String, Object> cloudData = new HashMap<>();
        cloudData.put("tags", tagCounts);
        cloudData.put("totalTags", tagCounts.size());
        
        if (!tagCounts.isEmpty()) {
            Long maxCount = tagCounts.values().stream().max(Long::compareTo).orElse(1L);
            Long minCount = tagCounts.values().stream().min(Long::compareTo).orElse(1L);
            
            cloudData.put("maxCount", maxCount);
            cloudData.put("minCount", minCount);
        }
        
        return cloudData;
    }

    @Override
    public BuildLogEntry autoGenerateTags(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        List<String> autoTags = new ArrayList<>();
        
        // Add subsystem as tag
        if (entry.getSubsystem() != null) {
            autoTags.add(entry.getSubsystem().toLowerCase());
        }
        
        // Add component as tag
        if (entry.getComponent() != null) {
            autoTags.add(entry.getComponent().toLowerCase());
        }
        
        // Add build phase as tag
        if (entry.getBuildPhase() != null) {
            autoTags.add(entry.getBuildPhase().name().toLowerCase().replace("_", "-"));
        }
        
        // Add entry type as tag
        if (entry.getEntryType() != null) {
            autoTags.add(entry.getEntryType().name().toLowerCase().replace("_", "-"));
        }
        
        // Add materials as tags
        if (entry.getMaterialsUsed() != null) {
            autoTags.addAll(entry.getMaterialsUsed().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
        }
        
        // Add milestone tag if applicable
        if (entry.getIsMilestone()) {
            autoTags.add("milestone");
        }
        
        // Add competition-ready tag if applicable
        if (entry.getIsCompetitionReady()) {
            autoTags.add("competition-ready");
        }
        
        // Merge with existing tags
        if (entry.getTags() == null) {
            entry.setTags(new ArrayList<>());
        }
        
        for (String tag : autoTags) {
            if (!entry.getTags().contains(tag)) {
                entry.getTags().add(tag);
            }
        }
        
        entry.setUpdatedAt(LocalDateTime.now());
        return entryRepository.save(entry);
    }

    // =========================================================================
    // MODERATION AND REVIEW
    // =========================================================================

    @Override
    public BuildLogEntry submitForReview(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsReviewed(false);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry reviewEntry(Long entryId, TeamMember reviewer, String comments, boolean approved) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsReviewed(true);
        entry.setReviewedBy(reviewer);
        entry.setReviewedAt(LocalDateTime.now());
        entry.setReviewComments(comments);
        entry.setUpdatedAt(LocalDateTime.now());
        
        if (!approved) {
            entry.setVisibility(BuildLogEntry.VisibilityLevel.PRIVATE);
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry flagEntry(Long entryId, String reason) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsFlagged(true);
        entry.setFlagReason(reason);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public BuildLogEntry moderateEntry(Long entryId, TeamMember moderator, boolean approved) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsModerated(true);
        entry.setModeratedBy(moderator);
        entry.setModeratedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        
        if (!approved) {
            entry.setVisibility(BuildLogEntry.VisibilityLevel.PRIVATE);
        } else {
            entry.setIsFlagged(false);
            entry.setFlagReason(null);
        }
        
        return entryRepository.save(entry);
    }

    @Override
    public List<BuildLogEntry> findPendingReview(Integer teamNumber, Integer season) {
        return findActiveEntries(teamNumber, season).stream()
                .filter(e -> !e.getIsReviewed())
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildLogEntry> findPendingModeration(Integer teamNumber, Integer season) {
        return entryRepository.findPendingModeration(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findFlaggedEntries(Integer teamNumber, Integer season) {
        return entryRepository.findFlaggedEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findLowQualityEntries(Integer teamNumber, Integer season, Integer maxScore) {
        return entryRepository.findLowQualityEntries(teamNumber, season, maxScore);
    }

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    @Override
    public Map<BuildLogEntry.EntryType, Long> countByEntryType(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.countByEntryType(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (BuildLogEntry.EntryType) result[0],
                    result -> (Long) result[1]
                ));
    }

    @Override
    public Map<BuildLogEntry.BuildPhase, Long> countByBuildPhase(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.countByBuildPhase(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (BuildLogEntry.BuildPhase) result[0],
                    result -> (Long) result[1]
                ));
    }

    @Override
    public Map<BuildLogEntry.CompletionStatus, Long> countByCompletionStatus(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.countByCompletionStatus(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (BuildLogEntry.CompletionStatus) result[0],
                    result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Long> countBySubsystem(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.countBySubsystem(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1],
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));
    }

    @Override
    public Long calculateTotalPhotoCount(Integer teamNumber, Integer season) {
        return entryRepository.calculateTotalPhotoCount(teamNumber, season).orElse(0L);
    }

    @Override
    public Double calculateAverageContentQuality(Integer teamNumber, Integer season) {
        return entryRepository.calculateAverageContentQuality(teamNumber, season).orElse(0.0);
    }

    @Override
    public Double calculateAverageEngagementScore(Integer teamNumber, Integer season) {
        return entryRepository.calculateAverageEngagementScore(teamNumber, season).orElse(0.0);
    }

    @Override
    public Map<String, Long> findMostUsedMaterials(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.findMostUsedMaterials(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1],
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Long> findMostUsedTools(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.findMostUsedTools(teamNumber, season);
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],
                    result -> (Long) result[1],
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("generatedAt", LocalDateTime.now());
        
        // Basic counts
        Long totalEntries = countActiveEntries(teamNumber, season);
        Long totalPhotos = calculateTotalPhotoCount(teamNumber, season);
        Double totalWorkHours = calculateTotalWorkHours(teamNumber, season);
        
        report.put("totalEntries", totalEntries);
        report.put("totalPhotos", totalPhotos);
        report.put("totalWorkHours", totalWorkHours);
        
        // Quality metrics
        Double avgQuality = calculateAverageContentQuality(teamNumber, season);
        Double avgEngagement = calculateAverageEngagementScore(teamNumber, season);
        Double overallProgress = calculateOverallProgress(teamNumber, season);
        
        report.put("averageContentQuality", avgQuality);
        report.put("averageEngagementScore", avgEngagement);
        report.put("overallProgress", overallProgress);
        
        // Distributions
        report.put("entryTypeDistribution", countByEntryType(teamNumber, season));
        report.put("buildPhaseDistribution", countByBuildPhase(teamNumber, season));
        report.put("completionStatusDistribution", countByCompletionStatus(teamNumber, season));
        report.put("subsystemDistribution", countBySubsystem(teamNumber, season));
        
        // Top items
        report.put("mostCommonTags", findMostCommonTags(teamNumber, season));
        report.put("mostUsedMaterials", findMostUsedMaterials(teamNumber, season));
        report.put("mostUsedTools", findMostUsedTools(teamNumber, season));
        
        // Special entries
        List<BuildLogEntry> milestones = findMilestoneEntries(teamNumber, season);
        List<BuildLogEntry> problematic = findProblematicEntries(teamNumber, season);
        
        report.put("milestoneCount", milestones.size());
        report.put("problematicCount", problematic.size());
        
        return report;
    }

    @Override
    public Map<String, Object> generateDashboardData(Integer teamNumber, Integer season) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Recent activity
        List<BuildLogEntry> recentEntries = findRecentEntries(teamNumber, season, 7);
        List<BuildLogEntry> highlights = findTimelineHighlights(teamNumber, season).stream()
                .limit(5)
                .collect(Collectors.toList());
        
        dashboard.put("recentEntries", recentEntries);
        dashboard.put("recentHighlights", highlights);
        
        // Key metrics
        dashboard.put("totalEntries", countActiveEntries(teamNumber, season));
        dashboard.put("totalPhotos", calculateTotalPhotoCount(teamNumber, season));
        dashboard.put("totalWorkHours", calculateTotalWorkHours(teamNumber, season));
        dashboard.put("overallProgress", calculateOverallProgress(teamNumber, season));
        
        // Status summary
        Map<BuildLogEntry.CompletionStatus, Long> statusCounts = countByCompletionStatus(teamNumber, season);
        dashboard.put("statusSummary", statusCounts);
        
        // Upcoming deadlines
        List<BuildLogEntry> deadlines = findWithApproachingDeadlines(teamNumber, season, 7);
        dashboard.put("upcomingDeadlines", deadlines);
        
        // Featured content
        List<BuildLogEntry> featured = findFeaturedEntries(teamNumber, season).stream()
                .limit(3)
                .collect(Collectors.toList());
        dashboard.put("featuredEntries", featured);
        
        return dashboard;
    }

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    @Override
    public List<BuildLogEntry> findMultiSeasonEntries(Integer teamNumber, List<Integer> seasons) {
        return entryRepository.findMultiSeasonEntries(teamNumber, seasons);
    }

    @Override
    public List<BuildLogEntry> findSubsystemAcrossSeasons(Integer teamNumber, String subsystem) {
        return entryRepository.findSubsystemAcrossSeasons(teamNumber, subsystem);
    }

    @Override
    public List<Object[]> findRecurringBuildPatterns(Integer teamNumber) {
        return entryRepository.findRecurringBuildPatterns(teamNumber);
    }

    @Override
    public Map<String, Object> compareSeasons(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("teamNumber", teamNumber);
        comparison.put("seasons", seasons);
        
        Map<Integer, Map<String, Object>> seasonData = new HashMap<>();
        
        for (Integer season : seasons) {
            Map<String, Object> data = new HashMap<>();
            data.put("totalEntries", countActiveEntries(teamNumber, season));
            data.put("totalPhotos", calculateTotalPhotoCount(teamNumber, season));
            data.put("totalWorkHours", calculateTotalWorkHours(teamNumber, season));
            data.put("averageQuality", calculateAverageContentQuality(teamNumber, season));
            data.put("overallProgress", calculateOverallProgress(teamNumber, season));
            
            seasonData.put(season, data);
        }
        
        comparison.put("seasonData", seasonData);
        
        // Find common patterns
        List<Object[]> patterns = findRecurringBuildPatterns(teamNumber);
        comparison.put("recurringPatterns", patterns);
        
        return comparison;
    }

    @Override
    public Map<String, Object> analyzeTeamEvolution(Integer teamNumber, List<Integer> seasons) {
        Map<String, Object> evolution = new HashMap<>();
        evolution.put("teamNumber", teamNumber);
        evolution.put("seasons", seasons);
        
        // Track quality improvement over seasons
        List<Map<String, Object>> qualityTrend = new ArrayList<>();
        for (Integer season : seasons) {
            Map<String, Object> point = new HashMap<>();
            point.put("season", season);
            point.put("averageQuality", calculateAverageContentQuality(teamNumber, season));
            point.put("totalEntries", countActiveEntries(teamNumber, season));
            qualityTrend.add(point);
        }
        evolution.put("qualityTrend", qualityTrend);
        
        // Track work hours progression
        List<Map<String, Object>> workTrend = new ArrayList<>();
        for (Integer season : seasons) {
            Map<String, Object> point = new HashMap<>();
            point.put("season", season);
            point.put("totalWorkHours", calculateTotalWorkHours(teamNumber, season));
            workTrend.add(point);
        }
        evolution.put("workTrend", workTrend);
        
        return evolution;
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public Long countActiveEntries(Integer teamNumber, Integer season) {
        return entryRepository.countActiveEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> findAllActiveEntries(Integer teamNumber, Integer season) {
        return entryRepository.findAllActiveEntries(teamNumber, season);
    }

    @Override
    public List<BuildLogEntry> createBulkEntries(List<BuildLogEntry> entries) {
        List<BuildLogEntry> created = new ArrayList<>();
        
        for (BuildLogEntry entry : entries) {
            try {
                BuildLogEntry createdEntry = createEntry(entry);
                created.add(createdEntry);
            } catch (Exception e) {
                // Log error but continue with other entries
                System.err.println("Failed to create entry: " + e.getMessage());
            }
        }
        
        return created;
    }

    @Override
    public List<BuildLogEntry> updateBulkEntries(Map<Long, BuildLogEntry> entryUpdates) {
        List<BuildLogEntry> updated = new ArrayList<>();
        
        for (Map.Entry<Long, BuildLogEntry> update : entryUpdates.entrySet()) {
            try {
                BuildLogEntry updatedEntry = updateEntry(update.getKey(), update.getValue());
                updated.add(updatedEntry);
            } catch (Exception e) {
                // Log error but continue with other entries
                System.err.println("Failed to update entry " + update.getKey() + ": " + e.getMessage());
            }
        }
        
        return updated;
    }

    @Override
    public void bulkArchiveEntries(List<Long> entryIds, String reason) {
        for (Long id : entryIds) {
            try {
                archiveEntry(id, reason);
            } catch (Exception e) {
                // Log error but continue with other entries
                System.err.println("Failed to archive entry " + id + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateSearchRankings(Integer teamNumber, Integer season) {
        // This would implement search ranking algorithm
        // For now, set default rankings
        entryRepository.updateSearchRankForAllEntries(teamNumber, season, 50);
    }

    @Override
    public void updateAllContentQuality(Integer teamNumber, Integer season) {
        List<BuildLogEntry> entries = findActiveEntries(teamNumber, season);
        
        for (BuildLogEntry entry : entries) {
            entry.updateContentQuality();
            entryRepository.save(entry);
        }
    }

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    @Override
    public List<BuildLogEntry> findArchivedEntries(Integer teamNumber, Integer season) {
        return entryRepository.findArchivedEntries(teamNumber, season);
    }

    @Override
    public BuildLogEntry restoreArchivedEntry(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        entry.setIsArchived(false);
        entry.setArchiveReason(null);
        entry.setArchivedAt(null);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public void permanentlyDeleteEntry(Long entryId) {
        entryRepository.deleteById(entryId);
    }

    @Override
    public List<BuildLogEntry> findArchivedInDateRange(Integer teamNumber, Integer season, 
                                                      LocalDateTime startDate, LocalDateTime endDate) {
        return entryRepository.findArchivedInDateRange(teamNumber, season, startDate, endDate);
    }

    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================

    @Override
    public Map<String, Object> exportToExternalFormat(Long entryId, String format) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("id", entry.getId());
        exportData.put("title", entry.getTitle());
        exportData.put("description", entry.getDescription());
        exportData.put("workAccomplished", entry.getWorkAccomplished());
        exportData.put("entryDateTime", entry.getEntryDateTime());
        exportData.put("photoUrls", entry.getPhotoUrls());
        exportData.put("participantNames", entry.getParticipantNames());
        exportData.put("materialsUsed", entry.getMaterialsUsed());
        exportData.put("toolsUsed", entry.getToolsUsed());
        exportData.put("tags", entry.getTags());
        
        // Format-specific adjustments
        if ("json".equalsIgnoreCase(format)) {
            exportData.put("format", "json");
        } else if ("xml".equalsIgnoreCase(format)) {
            exportData.put("format", "xml");
        }
        
        return exportData;
    }

    @Override
    public BuildLogEntry importFromExternalSource(Map<String, Object> entryData, String sourceType) {
        BuildLogEntry entry = new BuildLogEntry();
        
        // Map basic fields
        if (entryData.containsKey("title")) {
            entry.setTitle((String) entryData.get("title"));
        }
        if (entryData.containsKey("description")) {
            entry.setDescription((String) entryData.get("description"));
        }
        if (entryData.containsKey("workAccomplished")) {
            entry.setWorkAccomplished((String) entryData.get("workAccomplished"));
        }
        
        // Set defaults for required fields
        entry.setTeamNumber((Integer) entryData.get("teamNumber"));
        entry.setSeason((Integer) entryData.get("season"));
        entry.setEntryType(BuildLogEntry.EntryType.WORK_SESSION);
        entry.setBuildPhase(BuildLogEntry.BuildPhase.FABRICATION);
        
        return createEntry(entry);
    }

    @Override
    public List<Map<String, Object>> generateSitemap(Integer teamNumber, Integer season) {
        List<Object[]> results = entryRepository.findForSitemap(teamNumber, season);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", result[0]);
                    item.put("lastModified", result[1]);
                    item.put("url", "/build-log/" + result[0]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BuildLogEntry syncPhotosWithExternalService(Long entryId, String serviceType) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        // This would implement actual sync with external photo services
        // For now, just update the entry timestamp
        entry.setUpdatedAt(LocalDateTime.now());
        
        return entryRepository.save(entry);
    }

    @Override
    public Map<String, Object> generateRssFeedData(Integer teamNumber, Integer season) {
        List<BuildLogEntry> recentEntries = findRecentEntries(teamNumber, season, 30);
        
        Map<String, Object> feedData = new HashMap<>();
        feedData.put("title", "Team " + teamNumber + " Build Log - " + season);
        feedData.put("description", "Latest build log entries for FRC Team " + teamNumber);
        feedData.put("lastBuildDate", LocalDateTime.now());
        feedData.put("entries", recentEntries);
        
        return feedData;
    }

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    @Override
    public List<String> validateEntry(BuildLogEntry entry) {
        List<String> errors = new ArrayList<>();
        
        if (entry.getTeamNumber() == null) {
            errors.add("Team number is required");
        }
        if (entry.getSeason() == null) {
            errors.add("Season is required");
        }
        if (entry.getTitle() == null || entry.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        if (entry.getEntryType() == null) {
            errors.add("Entry type is required");
        }
        if (entry.getBuildPhase() == null) {
            errors.add("Build phase is required");
        }
        if (entry.getCreatedBy() == null) {
            errors.add("Creator is required");
        }
        
        // Validate progress percentage
        if (entry.getProgressPercentage() != null && 
            (entry.getProgressPercentage() < 0.0 || entry.getProgressPercentage() > 100.0)) {
            errors.add("Progress percentage must be between 0 and 100");
        }
        
        // Validate work session times
        if (entry.getWorkStartTime() != null && entry.getWorkEndTime() != null &&
            entry.getWorkStartTime().isAfter(entry.getWorkEndTime())) {
            errors.add("Work start time must be before end time");
        }
        
        return errors;
    }

    @Override
    public boolean validatePhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            return false;
        }
        
        // Basic URL validation
        return photoUrl.startsWith("http://") || photoUrl.startsWith("https://") || 
               photoUrl.startsWith("/uploads/") || photoUrl.startsWith("data:image/");
    }

    @Override
    public boolean validateUserPermissions(Long entryId, Long userId, String operation) {
        // Implement permission checking based on user roles and entry ownership
        // For now, assume all operations are permitted
        return true;
    }

    @Override
    public Map<String, Object> checkContentQuality(Long entryId) {
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        Map<String, Object> quality = new HashMap<>();
        quality.put("score", entry.getContentQualityScore());
        
        List<String> issues = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        
        // Check for issues
        if (entry.getDescription() == null || entry.getDescription().length() < 50) {
            issues.add("Description is too short or missing");
        }
        if (entry.getPhotoCount() == 0) {
            issues.add("No photos included");
        }
        if (entry.getWorkAccomplished() == null || entry.getWorkAccomplished().trim().isEmpty()) {
            issues.add("Work accomplished not documented");
        }
        
        // Check for strengths
        if (entry.getPhotoCount() >= 3) {
            strengths.add("Good visual documentation");
        }
        if (entry.getTechnicalNotes() != null && !entry.getTechnicalNotes().trim().isEmpty()) {
            strengths.add("Includes technical documentation");
        }
        if (entry.getLessonsLearned() != null && !entry.getLessonsLearned().trim().isEmpty()) {
            strengths.add("Documents lessons learned");
        }
        
        quality.put("issues", issues);
        quality.put("strengths", strengths);
        
        return quality;
    }

    @Override
    public List<String> suggestImprovements(Long entryId) {
        Map<String, Object> qualityCheck = checkContentQuality(entryId);
        
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) qualityCheck.get("issues");
        
        List<String> suggestions = new ArrayList<>();
        
        for (String issue : issues) {
            if (issue.contains("Description")) {
                suggestions.add("Add a more detailed description of the work performed");
            }
            if (issue.contains("photos")) {
                suggestions.add("Include photos to show the work in progress and results");
            }
            if (issue.contains("Work accomplished")) {
                suggestions.add("Document what specific work was completed during this session");
            }
        }
        
        // General suggestions
        BuildLogEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Build log entry not found: " + entryId));
        
        if (entry.getMaterialsUsed() == null || entry.getMaterialsUsed().isEmpty()) {
            suggestions.add("List the materials used during this work");
        }
        if (entry.getToolsUsed() == null || entry.getToolsUsed().isEmpty()) {
            suggestions.add("Document the tools used for this work");
        }
        if (entry.getSafetyNotes() == null || entry.getSafetyNotes().trim().isEmpty()) {
            suggestions.add("Include any safety considerations or notes");
        }
        
        return suggestions;
    }
}