// src/main/java/org/frcpm/services/impl/VideoTutorialServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.VideoTutorial;
import org.frcpm.models.VideoTutorialSeries;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Task;
import org.frcpm.models.Project;
import org.frcpm.models.WikiPage;
import org.frcpm.repositories.spring.VideoTutorialRepository;
import org.frcpm.repositories.spring.VideoTutorialSeriesRepository;
import org.frcpm.services.VideoTutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * Implementation of VideoTutorialService.
 * 
 * Provides comprehensive video tutorial management services including
 * content organization, learning analytics, engagement tracking, and
 * educational content management for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.2 Video Tutorial Library Integration
 */
@Service
@Transactional
public class VideoTutorialServiceImpl implements VideoTutorialService {

    @Autowired
    private VideoTutorialRepository tutorialRepository;
    
    @Autowired
    private VideoTutorialSeriesRepository seriesRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public VideoTutorial create(VideoTutorial tutorial) {
        return createTutorial(tutorial);
    }

    @Override
    public VideoTutorial update(Long id, VideoTutorial tutorial) {
        return updateTutorial(id, tutorial);
    }

    @Override
    public void delete(Long id) {
        archiveTutorial(id, "Deleted by user");
    }

    @Override
    public Optional<VideoTutorial> findById(Long id) {
        return tutorialRepository.findById(id);
    }

    @Override
    public List<VideoTutorial> findAll() {
        return tutorialRepository.findAll().stream()
                .filter(VideoTutorial::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return tutorialRepository.existsById(id);
    }

    @Override
    public long count() {
        return tutorialRepository.count();
    }

    // =========================================================================
    // TUTORIAL MANAGEMENT
    // =========================================================================

    @Override
    public VideoTutorial createTutorial(VideoTutorial tutorial) {
        validateTutorial(tutorial);
        
        // Set defaults
        if (tutorial.getStatus() == null) {
            tutorial.setStatus(VideoTutorial.TutorialStatus.DRAFT);
        }
        if (tutorial.getVisibility() == null) {
            tutorial.setVisibility(VideoTutorial.VisibilityLevel.TEAM_ONLY);
        }
        if (tutorial.getCreatedAt() == null) {
            tutorial.setCreatedAt(LocalDateTime.now());
        }
        
        // Generate slug if not provided
        if (tutorial.getSlug() == null || tutorial.getSlug().isEmpty()) {
            tutorial.setSlug(generateSlug(tutorial.getTitle(), tutorial.getTeamNumber(), tutorial.getSeason()));
        }
        
        // Initialize analytics
        tutorial.setViewCount(0L);
        tutorial.setCompletionCount(0L);
        tutorial.setRatingCount(0);
        tutorial.setAverageRating(0.0);
        tutorial.setEngagementScore(0.0);
        tutorial.setCompletionRate(0.0);
        
        // Process content features
        processContentFeatures(tutorial.getId());
        
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial createTutorial(Integer teamNumber, Integer season, String title, String videoUrl,
                                       VideoTutorial.TutorialCategory category, VideoTutorial.SkillLevel skillLevel,
                                       TeamMember createdBy) {
        VideoTutorial tutorial = new VideoTutorial();
        tutorial.setTeamNumber(teamNumber);
        tutorial.setSeason(season);
        tutorial.setTitle(title);
        tutorial.setVideoUrl(videoUrl);
        tutorial.setCategory(category);
        tutorial.setSkillLevel(skillLevel);
        tutorial.setCreatedBy(createdBy);
        tutorial.setInstructor(createdBy);
        tutorial.setCreatedAt(LocalDateTime.now());
        tutorial.setUpdatedAt(LocalDateTime.now());
        
        return createTutorial(tutorial);
    }

    @Override
    public VideoTutorial updateTutorial(Long tutorialId, VideoTutorial tutorial) {
        Optional<VideoTutorial> existingOpt = tutorialRepository.findById(tutorialId);
        if (!existingOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial existing = existingOpt.get();
        validateTutorial(tutorial);
        
        // Update fields
        existing.setTitle(tutorial.getTitle());
        existing.setDescription(tutorial.getDescription());
        existing.setSummary(tutorial.getSummary());
        existing.setCategory(tutorial.getCategory());
        existing.setSkillLevel(tutorial.getSkillLevel());
        existing.setVideoUrl(tutorial.getVideoUrl());
        existing.setThumbnailUrl(tutorial.getThumbnailUrl());
        existing.setTags(tutorial.getTags());
        existing.setTopics(tutorial.getTopics());
        existing.setLearningObjectives(tutorial.getLearningObjectives());
        existing.setSkillsTaught(tutorial.getSkillsTaught());
        existing.setUpdatedBy(tutorial.getUpdatedBy());
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Increment version if major content changes
        if (!Objects.equals(existing.getVideoUrl(), tutorial.getVideoUrl()) ||
            !Objects.equals(existing.getDescription(), tutorial.getDescription())) {
            existing.setVersion(existing.getVersion() + 1);
        }
        
        existing.touch();
        updateEngagementMetrics(existing.getId());
        
        return tutorialRepository.save(existing);
    }

    @Override
    public void archiveTutorial(Long tutorialId, String reason) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (tutorialOpt.isPresent()) {
            VideoTutorial tutorial = tutorialOpt.get();
            tutorial.archive(reason);
            tutorialRepository.save(tutorial);
        }
    }

    @Override
    public List<VideoTutorial> findActiveTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    @Override
    public List<VideoTutorial> searchByTitle(Integer teamNumber, Integer season, String searchTerm) {
        return tutorialRepository.searchByTitle(teamNumber, season, searchTerm);
    }

    @Override
    public List<VideoTutorial> searchByContent(Integer teamNumber, Integer season, String searchTerm) {
        return tutorialRepository.searchByContent(teamNumber, season, searchTerm);
    }

    @Override
    public List<VideoTutorial> fullTextSearch(Integer teamNumber, Integer season, String searchTerm) {
        return tutorialRepository.fullTextSearch(teamNumber, season, searchTerm);
    }

    @Override
    public List<VideoTutorial> findByCategory(Integer teamNumber, Integer season, VideoTutorial.TutorialCategory category) {
        return tutorialRepository.findByTeamNumberAndSeasonAndCategoryAndIsActiveTrue(teamNumber, season, category);
    }

    @Override
    public List<VideoTutorial> findBySkillLevel(Integer teamNumber, Integer season, VideoTutorial.SkillLevel skillLevel) {
        return tutorialRepository.findByTeamNumberAndSeasonAndSkillLevelAndIsActiveTrue(teamNumber, season, skillLevel);
    }

    @Override
    public List<VideoTutorial> findByTag(Integer teamNumber, Integer season, String tag) {
        return tutorialRepository.findByTag(teamNumber, season, tag);
    }

    @Override
    public List<VideoTutorial> findByTopic(Integer teamNumber, Integer season, String topic) {
        return tutorialRepository.findByTopic(teamNumber, season, topic);
    }

    @Override
    public List<VideoTutorial> findBySkillTaught(Integer teamNumber, Integer season, String skill) {
        return tutorialRepository.findBySkillTaught(teamNumber, season, skill);
    }

    @Override
    public List<VideoTutorial> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria) {
        List<VideoTutorial> results = findActiveTutorials(teamNumber, season);
        
        // Apply filters based on criteria
        if (criteria.containsKey("category")) {
            VideoTutorial.TutorialCategory category = (VideoTutorial.TutorialCategory) criteria.get("category");
            results = results.stream()
                    .filter(t -> t.getCategory() == category)
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("skillLevel")) {
            VideoTutorial.SkillLevel skillLevel = (VideoTutorial.SkillLevel) criteria.get("skillLevel");
            results = results.stream()
                    .filter(t -> t.getSkillLevel() == skillLevel)
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("hasQuiz")) {
            Boolean hasQuiz = (Boolean) criteria.get("hasQuiz");
            results = results.stream()
                    .filter(t -> t.getHasQuiz().equals(hasQuiz))
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("minRating")) {
            Double minRating = (Double) criteria.get("minRating");
            results = results.stream()
                    .filter(t -> t.getAverageRating() >= minRating)
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("maxDuration")) {
            Duration maxDuration = (Duration) criteria.get("maxDuration");
            results = results.stream()
                    .filter(t -> t.getDuration() != null && t.getDuration().compareTo(maxDuration) <= 0)
                    .collect(Collectors.toList());
        }
        
        return results;
    }

    // =========================================================================
    // SERIES MANAGEMENT
    // =========================================================================

    @Override
    public List<VideoTutorial> findBySeries(VideoTutorialSeries series) {
        return tutorialRepository.findBySeriesAndIsActiveTrueOrderBySeriesOrderAsc(series);
    }

    @Override
    public List<VideoTutorial> findStandaloneTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findStandaloneTutorials(teamNumber, season);
    }

    @Override
    public VideoTutorial addToSeries(Long tutorialId, VideoTutorialSeries series, Integer order) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setSeries(series);
        tutorial.setSeriesOrder(order);
        tutorial.touch();
        
        // Update series metrics
        series.addTutorial(tutorial);
        seriesRepository.save(series);
        
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial removeFromSeries(Long tutorialId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        VideoTutorialSeries series = tutorial.getSeries();
        
        tutorial.setSeries(null);
        tutorial.setSeriesOrder(null);
        tutorial.touch();
        
        // Update series metrics
        if (series != null) {
            series.removeTutorial(tutorial);
            seriesRepository.save(series);
        }
        
        return tutorialRepository.save(tutorial);
    }

    @Override
    public void reorderSeriesTutorials(VideoTutorialSeries series, Map<Long, Integer> newOrders) {
        List<VideoTutorial> tutorials = findBySeries(series);
        
        for (VideoTutorial tutorial : tutorials) {
            if (newOrders.containsKey(tutorial.getId())) {
                tutorial.setSeriesOrder(newOrders.get(tutorial.getId()));
                tutorialRepository.save(tutorial);
            }
        }
        
        series.updateSeriesMetrics();
        seriesRepository.save(series);
    }

    @Override
    public Optional<VideoTutorial> findNextInSeries(VideoTutorial tutorial) {
        if (tutorial.getSeries() == null) {
            return Optional.empty();
        }
        return tutorialRepository.findNextInSeries(tutorial.getSeries(), tutorial.getSeriesOrder());
    }

    @Override
    public Optional<VideoTutorial> findPreviousInSeries(VideoTutorial tutorial) {
        if (tutorial.getSeries() == null) {
            return Optional.empty();
        }
        return tutorialRepository.findPreviousInSeries(tutorial.getSeries(), tutorial.getSeriesOrder());
    }

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    @Override
    public List<VideoTutorial> findViewableTutorials(Integer teamNumber, Integer season, VideoTutorial.AccessLevel accessLevel) {
        return tutorialRepository.findByTeamNumberAndSeasonAndViewPermissionAndIsActiveTrue(teamNumber, season, accessLevel);
    }

    @Override
    public List<VideoTutorial> findViewableTutorialsByUser(Integer teamNumber, Integer season, Long userId, String userRole) {
        List<VideoTutorial> tutorials = findActiveTutorials(teamNumber, season);
        return tutorials.stream()
                .filter(t -> canUserViewTutorial(t.getId(), userId, userRole))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canUserViewTutorial(Long tutorialId, Long userId, String userRole) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            return false;
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        return tutorial.isViewableBy(userId, userRole);
    }

    @Override
    public VideoTutorial addAuthorizedViewer(Long tutorialId, Long userId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        if (!tutorial.getAuthorizedViewers().contains(userId)) {
            tutorial.getAuthorizedViewers().add(userId);
            tutorial.touch();
            return tutorialRepository.save(tutorial);
        }
        
        return tutorial;
    }

    @Override
    public VideoTutorial removeAuthorizedViewer(Long tutorialId, Long userId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.getAuthorizedViewers().remove(userId);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public List<VideoTutorial> findPublicTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findPublicTutorials(teamNumber, season);
    }

    // =========================================================================
    // CREATOR AND INSTRUCTOR MANAGEMENT
    // =========================================================================

    @Override
    public List<VideoTutorial> findByCreatedBy(TeamMember createdBy, Integer teamNumber, Integer season) {
        return tutorialRepository.findByCreatedByAndTeamNumberAndSeasonAndIsActiveTrue(createdBy, teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findByInstructor(TeamMember instructor, Integer teamNumber, Integer season) {
        return tutorialRepository.findByInstructorAndTeamNumberAndSeasonAndIsActiveTrue(instructor, teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findByContributor(Integer teamNumber, Integer season, Long contributorId) {
        return tutorialRepository.findByContributor(teamNumber, season, contributorId);
    }

    @Override
    public VideoTutorial addContributor(Long tutorialId, Long contributorId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        if (!tutorial.getContributors().contains(contributorId)) {
            tutorial.getContributors().add(contributorId);
            tutorial.touch();
            return tutorialRepository.save(tutorial);
        }
        
        return tutorial;
    }

    @Override
    public VideoTutorial removeContributor(Long tutorialId, Long contributorId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.getContributors().remove(contributorId);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial setInstructor(Long tutorialId, TeamMember instructor) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setInstructor(instructor);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    @Override
    public void recordView(Long tutorialId, Long userId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (tutorialOpt.isPresent()) {
            VideoTutorial tutorial = tutorialOpt.get();
            tutorial.recordView(userId);
            tutorialRepository.save(tutorial);
        }
    }

    @Override
    public void recordCompletion(Long tutorialId, Long userId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (tutorialOpt.isPresent()) {
            VideoTutorial tutorial = tutorialOpt.get();
            tutorial.recordCompletion(userId);
            tutorialRepository.save(tutorial);
        }
    }

    @Override
    public VideoTutorial rateTutorial(Long tutorialId, TeamMember rater, Integer rating) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.addRating(rating);
        return tutorialRepository.save(tutorial);
    }

    @Override
    public List<VideoTutorial> findMostViewed(Integer teamNumber, Integer season, Integer limit) {
        List<VideoTutorial> tutorials = tutorialRepository.findMostViewed(teamNumber, season);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findMostCompleted(Integer teamNumber, Integer season, Integer limit) {
        List<VideoTutorial> tutorials = tutorialRepository.findMostCompleted(teamNumber, season);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findHighestRated(Integer teamNumber, Integer season, Integer minRatingCount, Integer limit) {
        List<VideoTutorial> tutorials = tutorialRepository.findHighestRated(teamNumber, season, minRatingCount);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findHighEngagement(Integer teamNumber, Integer season, Double minScore, Integer limit) {
        List<VideoTutorial> tutorials = tutorialRepository.findHighEngagement(teamNumber, season, minScore);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findRecentlyViewed(Integer teamNumber, Integer season, Integer limit) {
        List<VideoTutorial> tutorials = tutorialRepository.findRecentlyViewed(teamNumber, season);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public void updateEngagementMetrics(Long tutorialId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (tutorialOpt.isPresent()) {
            VideoTutorial tutorial = tutorialOpt.get();
            tutorial.updateEngagementMetrics();
            tutorialRepository.save(tutorial);
        }
    }

    // =========================================================================
    // FEATURED AND SPECIAL TUTORIALS
    // =========================================================================

    @Override
    public List<VideoTutorial> findFeaturedTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findFeatured(teamNumber, season);
    }

    @Override
    public VideoTutorial setTutorialFeatured(Long tutorialId, boolean featured) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setIsFeatured(featured);
        if (featured) {
            tutorial.setFeaturedAt(LocalDateTime.now());
        }
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public List<VideoTutorial> findInteractiveTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findInteractive(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findTutorialsWithResources(Integer teamNumber, Integer season) {
        return tutorialRepository.findWithResources(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findTutorialsWithSubtitles(Integer teamNumber, Integer season) {
        return tutorialRepository.findWithSubtitles(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findTutorialsWithTranscripts(Integer teamNumber, Integer season) {
        return tutorialRepository.findWithTranscripts(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findBeginnerFriendlyTutorials(Integer teamNumber, Integer season, Integer limit) {
        List<VideoTutorial> tutorials = tutorialRepository.findBeginnerFriendlyTutorials(teamNumber, season);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    // =========================================================================
    // VIDEO SOURCE AND TECHNICAL MANAGEMENT
    // =========================================================================

    @Override
    public List<VideoTutorial> findByVideoSource(Integer teamNumber, Integer season, VideoTutorial.VideoSource videoSource) {
        return tutorialRepository.findByTeamNumberAndSeasonAndVideoSourceAndIsActiveTrue(teamNumber, season, videoSource);
    }

    @Override
    public Optional<VideoTutorial> findByExternalVideoId(String externalVideoId) {
        return tutorialRepository.findByExternalVideoIdAndIsActiveTrue(externalVideoId);
    }

    @Override
    public VideoTutorial updateVideoMetadata(Long tutorialId, Duration duration, VideoTutorial.VideoQuality quality) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setDuration(duration);
        tutorial.setVideoQuality(quality);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial addSubtitle(Long tutorialId, String subtitleUrl) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setHasSubtitles(true);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial addTranscript(Long tutorialId, String transcript) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setTranscript(transcript);
        tutorial.setHasTranscript(true);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial processContentFeatures(Long tutorialId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            return null;
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        
        // Analyze content features
        if (tutorial.getDescription() != null) {
            String content = tutorial.getDescription().toLowerCase();
            tutorial.setHasInteractiveElements(!tutorial.getQuizQuestions().isEmpty() || !tutorial.getChapters().isEmpty());
            tutorial.setHasQuiz(!tutorial.getQuizQuestions().isEmpty());
            tutorial.setHasDownloadableResources(!tutorial.getResourceUrls().isEmpty());
        }
        
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    // =========================================================================
    // MODERATION AND QUALITY CONTROL
    // =========================================================================

    @Override
    public List<VideoTutorial> findFlaggedTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findFlagged(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findPendingModeration(Integer teamNumber, Integer season) {
        return tutorialRepository.findPendingModeration(teamNumber, season);
    }

    @Override
    public VideoTutorial flagTutorial(Long tutorialId, String reason) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setIsFlagged(true);
        tutorial.setFlagReason(reason);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial moderateTutorial(Long tutorialId, TeamMember moderator, boolean approved) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setIsModerated(true);
        tutorial.setModeratedBy(moderator);
        tutorial.setModeratedAt(LocalDateTime.now());
        
        if (approved) {
            tutorial.setStatus(VideoTutorial.TutorialStatus.PUBLISHED);
            tutorial.setIsFlagged(false);
            tutorial.setFlagReason(null);
        } else {
            tutorial.setStatus(VideoTutorial.TutorialStatus.DRAFT);
        }
        
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial setQualityScore(Long tutorialId, Integer qualityScore) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setQualityScore(qualityScore);
        tutorial.setIsQualityChecked(true);
        tutorial.touch();
        updateEngagementMetrics(tutorialId);
        return tutorialRepository.save(tutorial);
    }

    @Override
    public List<VideoTutorial> findByQualityScoreRange(Integer teamNumber, Integer season, Integer minScore, Integer maxScore) {
        return tutorialRepository.findByQualityScoreRange(teamNumber, season, minScore, maxScore);
    }

    @Override
    public List<String> validateTutorialContent(VideoTutorial tutorial) {
        List<String> issues = new ArrayList<>();
        
        if (tutorial.getTitle() == null || tutorial.getTitle().trim().isEmpty()) {
            issues.add("Title is required");
        }
        
        if (tutorial.getVideoUrl() == null || tutorial.getVideoUrl().trim().isEmpty()) {
            issues.add("Video URL is required");
        }
        
        if (tutorial.getCategory() == null) {
            issues.add("Category is required");
        }
        
        if (tutorial.getSkillLevel() == null) {
            issues.add("Skill level is required");
        }
        
        if (tutorial.getVideoUrl() != null && !validateVideoUrl(tutorial.getVideoUrl())) {
            issues.add("Invalid video URL format");
        }
        
        return issues;
    }

    // =========================================================================
    // TIME-BASED OPERATIONS
    // =========================================================================

    @Override
    public List<VideoTutorial> findCreatedInDateRange(Integer teamNumber, Integer season, LocalDateTime startDate, LocalDateTime endDate) {
        return tutorialRepository.findCreatedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<VideoTutorial> findRecentlyCreated(Integer teamNumber, Integer season, Integer days, Integer limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<VideoTutorial> tutorials = tutorialRepository.findRecentlyCreated(teamNumber, season, since);
        return tutorials.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findStaleTutorials(Integer teamNumber, Integer season, Integer days, Long minViews) {
        LocalDateTime staleDate = LocalDateTime.now().minusDays(days);
        return tutorialRepository.findStaleTutorials(teamNumber, season, staleDate, minViews);
    }

    @Override
    public List<VideoTutorial> findTutorialsNeedingUpdate(Integer teamNumber, Integer season) {
        return tutorialRepository.findNeedingUpdate(teamNumber, season);
    }

    @Override
    public VideoTutorial markTutorialForUpdate(Long tutorialId, String reason) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setNeedsUpdate(true);
        tutorial.setUpdateReason(reason);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    // =========================================================================
    // INTERACTIVE FEATURES
    // =========================================================================

    @Override
    public VideoTutorial addQuiz(Long tutorialId, List<String> quizQuestions) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setQuizQuestions(quizQuestions);
        tutorial.setHasQuiz(!quizQuestions.isEmpty());
        tutorial.setHasInteractiveElements(true);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial addChapters(Long tutorialId, List<String> chapters) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setChapters(chapters);
        tutorial.setHasInteractiveElements(true);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial addKeyTimestamps(Long tutorialId, List<String> timestamps) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.setKeyTimestamps(timestamps);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial addResource(Long tutorialId, String resourceUrl, String description) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.getResourceUrls().add(resourceUrl);
        tutorial.setHasDownloadableResources(true);
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public VideoTutorial removeResource(Long tutorialId, String resourceUrl) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.getResourceUrls().remove(resourceUrl);
        tutorial.setHasDownloadableResources(!tutorial.getResourceUrls().isEmpty());
        tutorial.touch();
        return tutorialRepository.save(tutorial);
    }

    // =========================================================================
    // CONTENT RELATIONSHIPS
    // =========================================================================

    @Override
    public VideoTutorial associateWithTask(Long tutorialId, Task task) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        if (!tutorial.getRelatedTasks().contains(task)) {
            tutorial.getRelatedTasks().add(task);
            tutorial.touch();
            return tutorialRepository.save(tutorial);
        }
        
        return tutorial;
    }

    @Override
    public VideoTutorial associateWithProject(Long tutorialId, Project project) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        if (!tutorial.getRelatedProjects().contains(project)) {
            tutorial.getRelatedProjects().add(project);
            tutorial.touch();
            return tutorialRepository.save(tutorial);
        }
        
        return tutorial;
    }

    @Override
    public VideoTutorial associateWithWikiPage(Long tutorialId, WikiPage wikiPage) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        if (!tutorial.getRelatedWikiPages().contains(wikiPage)) {
            tutorial.getRelatedWikiPages().add(wikiPage);
            tutorial.touch();
            return tutorialRepository.save(tutorial);
        }
        
        return tutorial;
    }

    @Override
    public List<VideoTutorial> findByRelatedTask(Long taskId) {
        return tutorialRepository.findByRelatedTask(taskId);
    }

    @Override
    public List<VideoTutorial> findByRelatedProject(Long projectId) {
        return tutorialRepository.findByRelatedProject(projectId);
    }

    @Override
    public List<VideoTutorial> findByRelatedWikiPage(Long wikiPageId) {
        return tutorialRepository.findByRelatedWikiPage(wikiPageId);
    }

    @Override
    public List<VideoTutorial> findUnrelatedTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findUnrelatedTutorials(teamNumber, season);
    }

    // =========================================================================
    // RECOMMENDATIONS
    // =========================================================================

    @Override
    public List<VideoTutorial> findSimilarTutorials(Long tutorialId, Integer limit) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            return new ArrayList<>();
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        List<VideoTutorial> similar = tutorialRepository.findSimilarTutorials(
                tutorial.getTeamNumber(), tutorial.getSeason(), 
                tutorial.getCategory(), tutorial.getSkillLevel(), tutorialId);
        
        return similar.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findRecommendedTutorials(Integer teamNumber, Integer season, Long userId, Integer limit) {
        // This would involve complex recommendation algorithms
        // For now, return a mix of popular and recently created tutorials
        List<VideoTutorial> popular = findMostViewed(teamNumber, season, limit / 2);
        List<VideoTutorial> recent = findRecentlyCreated(teamNumber, season, 30, limit / 2);
        
        Set<VideoTutorial> recommendations = new LinkedHashSet<>(popular);
        recommendations.addAll(recent);
        
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findTrendingTutorials(Integer teamNumber, Integer season, Integer days, Integer limit) {
        LocalDateTime recentCutoff = LocalDateTime.now().minusDays(days);
        List<VideoTutorial> trending = tutorialRepository.findTrendingTutorials(teamNumber, season, recentCutoff);
        return trending.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<VideoTutorial> findTutorialsByLearningPath(Integer teamNumber, Integer season, String skillPath) {
        // Implementation would define learning paths and return appropriate tutorials
        List<VideoTutorial> tutorials = findActiveTutorials(teamNumber, season);
        
        // Filter by skill path (simplified implementation)
        return tutorials.stream()
                .filter(t -> t.getSkillsTaught().contains(skillPath) || t.getTags().contains(skillPath))
                .sorted((t1, t2) -> {
                    // Sort by skill level progression
                    return t1.getSkillLevel().compareTo(t2.getSkillLevel());
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    @Override
    public Map<VideoTutorial.TutorialCategory, Long> countByCategory(Integer teamNumber, Integer season) {
        List<Object[]> results = tutorialRepository.countByCategory(teamNumber, season);
        Map<VideoTutorial.TutorialCategory, Long> counts = new HashMap<>();
        
        for (Object[] result : results) {
            VideoTutorial.TutorialCategory category = (VideoTutorial.TutorialCategory) result[0];
            Long count = (Long) result[1];
            counts.put(category, count);
        }
        
        return counts;
    }

    @Override
    public Map<VideoTutorial.SkillLevel, Long> countBySkillLevel(Integer teamNumber, Integer season) {
        List<Object[]> results = tutorialRepository.countBySkillLevel(teamNumber, season);
        Map<VideoTutorial.SkillLevel, Long> counts = new HashMap<>();
        
        for (Object[] result : results) {
            VideoTutorial.SkillLevel skillLevel = (VideoTutorial.SkillLevel) result[0];
            Long count = (Long) result[1];
            counts.put(skillLevel, count);
        }
        
        return counts;
    }

    @Override
    public Map<VideoTutorial.TutorialStatus, Long> countByStatus(Integer teamNumber, Integer season) {
        List<Object[]> results = tutorialRepository.countByStatus(teamNumber, season);
        Map<VideoTutorial.TutorialStatus, Long> counts = new HashMap<>();
        
        for (Object[] result : results) {
            VideoTutorial.TutorialStatus status = (VideoTutorial.TutorialStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }
        
        return counts;
    }

    @Override
    public Long calculateTotalViewCount(Integer teamNumber, Integer season) {
        return tutorialRepository.findTotalViewCount(teamNumber, season).orElse(0L);
    }

    @Override
    public Long calculateTotalCompletionCount(Integer teamNumber, Integer season) {
        return tutorialRepository.findTotalCompletionCount(teamNumber, season).orElse(0L);
    }

    @Override
    public Double calculateAverageEngagementScore(Integer teamNumber, Integer season) {
        return tutorialRepository.findAverageEngagementScore(teamNumber, season).orElse(0.0);
    }

    @Override
    public Double calculateAverageRating(Integer teamNumber, Integer season) {
        return tutorialRepository.findAverageRating(teamNumber, season).orElse(0.0);
    }

    @Override
    public Map<String, Long> findMostCommonTags(Integer teamNumber, Integer season) {
        List<Object[]> results = tutorialRepository.findMostCommonTags(teamNumber, season);
        Map<String, Long> tags = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String tag = (String) result[0];
            Long count = (Long) result[1];
            tags.put(tag, count);
        }
        
        return tags;
    }

    @Override
    public Map<String, Long> findMostCommonTopics(Integer teamNumber, Integer season) {
        List<Object[]> results = tutorialRepository.findMostCommonTopics(teamNumber, season);
        Map<String, Long> topics = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String topic = (String) result[0];
            Long count = (Long) result[1];
            topics.put(topic, count);
        }
        
        return topics;
    }

    @Override
    public Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("generatedAt", LocalDateTime.now());
        
        // Basic counts
        report.put("totalTutorials", countActiveTutorials(teamNumber, season));
        report.put("tutorialsByCategory", countByCategory(teamNumber, season));
        report.put("tutorialsBySkillLevel", countBySkillLevel(teamNumber, season));
        report.put("tutorialsByStatus", countByStatus(teamNumber, season));
        
        // Engagement metrics
        report.put("totalViews", calculateTotalViewCount(teamNumber, season));
        report.put("totalCompletions", calculateTotalCompletionCount(teamNumber, season));
        report.put("averageEngagement", calculateAverageEngagementScore(teamNumber, season));
        report.put("averageRating", calculateAverageRating(teamNumber, season));
        
        // Popular content
        report.put("mostCommonTags", findMostCommonTags(teamNumber, season));
        report.put("mostCommonTopics", findMostCommonTopics(teamNumber, season));
        report.put("featuredTutorials", findFeaturedTutorials(teamNumber, season));
        report.put("mostViewed", findMostViewed(teamNumber, season, 10));
        
        // Quality metrics
        report.put("flaggedTutorials", findFlaggedTutorials(teamNumber, season).size());
        report.put("pendingModeration", findPendingModeration(teamNumber, season).size());
        report.put("staleTutorials", findStaleTutorials(teamNumber, season, 180, 5L).size());
        
        return report;
    }

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    @Override
    public List<VideoTutorial> findMultiSeasonTutorials(Integer teamNumber, List<Integer> seasons) {
        return tutorialRepository.findMultiSeasonTutorials(teamNumber, seasons);
    }

    @Override
    public List<VideoTutorial> findByTitleAcrossSeasons(Integer teamNumber, String title) {
        return tutorialRepository.findByTitleAcrossSeasons(teamNumber, title);
    }

    @Override
    public Map<String, Long> findEvergreenTutorials(Integer teamNumber) {
        List<Object[]> results = tutorialRepository.findEvergreenTutorials(teamNumber);
        Map<String, Long> evergreen = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String title = (String) result[0];
            Long seasonCount = (Long) result[1];
            evergreen.put(title, seasonCount);
        }
        
        return evergreen;
    }

    @Override
    public VideoTutorial copyToNewSeason(Long tutorialId, Integer newSeason) {
        Optional<VideoTutorial> originalOpt = tutorialRepository.findById(tutorialId);
        if (!originalOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial original = originalOpt.get();
        VideoTutorial copy = new VideoTutorial();
        
        // Copy properties
        copyTutorialProperties(original, copy);
        copy.setId(null);
        copy.setSeason(newSeason);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());
        copy.setVersion(1);
        
        // Reset analytics
        copy.setViewCount(0L);
        copy.setCompletionCount(0L);
        copy.setRatingCount(0);
        copy.setAverageRating(0.0);
        copy.setEngagementScore(0.0);
        copy.setCompletionRate(0.0);
        
        return tutorialRepository.save(copy);
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public Long countActiveTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.countActiveTutorials(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> findAllActiveTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findAllActiveTutorials(teamNumber, season);
    }

    @Override
    public List<VideoTutorial> createBulkTutorials(List<VideoTutorial> tutorials) {
        List<VideoTutorial> created = new ArrayList<>();
        for (VideoTutorial tutorial : tutorials) {
            created.add(createTutorial(tutorial));
        }
        return created;
    }

    @Override
    public List<VideoTutorial> updateBulkTutorials(Map<Long, VideoTutorial> tutorialUpdates) {
        List<VideoTutorial> updated = new ArrayList<>();
        for (Map.Entry<Long, VideoTutorial> entry : tutorialUpdates.entrySet()) {
            updated.add(updateTutorial(entry.getKey(), entry.getValue()));
        }
        return updated;
    }

    @Override
    public void bulkArchiveTutorials(List<Long> tutorialIds, String reason) {
        for (Long tutorialId : tutorialIds) {
            archiveTutorial(tutorialId, reason);
        }
    }

    @Override
    public void updateSearchRankings(Integer teamNumber, Integer season) {
        List<VideoTutorial> tutorials = findActiveTutorials(teamNumber, season);
        
        for (VideoTutorial tutorial : tutorials) {
            int rank = calculateSearchRank(tutorial);
            tutorial.setSearchRank(rank);
            tutorialRepository.save(tutorial);
        }
    }

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    @Override
    public List<VideoTutorial> findArchivedTutorials(Integer teamNumber, Integer season) {
        return tutorialRepository.findArchivedTutorials(teamNumber, season);
    }

    @Override
    public VideoTutorial restoreArchivedTutorial(Long tutorialId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        tutorial.restore();
        return tutorialRepository.save(tutorial);
    }

    @Override
    public void permanentlyDeleteTutorial(Long tutorialId) {
        tutorialRepository.deleteById(tutorialId);
    }

    // =========================================================================
    // INTEGRATION AND EXTERNAL SYSTEMS
    // =========================================================================

    @Override
    public VideoTutorial importFromExternalSource(Map<String, Object> tutorialData, String sourceType) {
        VideoTutorial tutorial = new VideoTutorial();
        
        // Parse based on source type
        if ("youtube".equals(sourceType)) {
            tutorial.setTitle((String) tutorialData.get("title"));
            tutorial.setDescription((String) tutorialData.get("description"));
            tutorial.setVideoUrl((String) tutorialData.get("url"));
            tutorial.setExternalVideoId((String) tutorialData.get("videoId"));
            tutorial.setVideoSource(VideoTutorial.VideoSource.YOUTUBE);
            tutorial.setThumbnailUrl((String) tutorialData.get("thumbnail"));
            
            // Parse duration
            if (tutorialData.containsKey("duration")) {
                tutorial.setDuration(Duration.parse((String) tutorialData.get("duration")));
            }
        }
        
        tutorial.setTeamNumber((Integer) tutorialData.get("teamNumber"));
        tutorial.setSeason((Integer) tutorialData.get("season"));
        tutorial.setIsOriginalContent(false);
        tutorial.setSourceAttribution(sourceType);
        
        return createTutorial(tutorial);
    }

    @Override
    public Map<String, Object> exportToExternalFormat(Long tutorialId, String format) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            throw new RuntimeException("Tutorial not found with id: " + tutorialId);
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        Map<String, Object> export = new HashMap<>();
        
        if ("json".equals(format)) {
            export.put("id", tutorial.getId());
            export.put("title", tutorial.getTitle());
            export.put("description", tutorial.getDescription());
            export.put("videoUrl", tutorial.getVideoUrl());
            export.put("category", tutorial.getCategory());
            export.put("skillLevel", tutorial.getSkillLevel());
            export.put("tags", tutorial.getTags());
            export.put("topics", tutorial.getTopics());
            export.put("duration", tutorial.getFormattedDuration());
            export.put("viewCount", tutorial.getViewCount());
            export.put("rating", tutorial.getAverageRating());
        }
        
        return export;
    }

    @Override
    public VideoTutorial syncWithExternalPlatform(Long tutorialId) {
        // Implementation would sync with external video platforms
        return findById(tutorialId).orElse(null);
    }

    @Override
    public List<Map<String, Object>> generateSitemap(Integer teamNumber, Integer season) {
        return tutorialRepository.findForSitemap(teamNumber, season).stream()
                .map(result -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("slug", result[0]);
                    entry.put("lastModified", result[1]);
                    return entry;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // LEARNING PROGRESS TRACKING
    // =========================================================================

    @Override
    public void recordLearningProgress(Long tutorialId, Long userId, Double progressPercentage) {
        // Implementation would record progress in a separate UserProgress entity
        // For now, we'll just record as completed if 100%
        if (progressPercentage >= 100.0) {
            recordCompletion(tutorialId, userId);
        }
    }

    @Override
    public Double getUserProgress(Long tutorialId, Long userId) {
        // Implementation would query UserProgress entity
        return 0.0; // Placeholder
    }

    @Override
    public List<VideoTutorial> findTutorialsInProgress(Integer teamNumber, Integer season, Long userId) {
        // Implementation would query UserProgress entity
        return new ArrayList<>(); // Placeholder
    }

    @Override
    public List<VideoTutorial> findCompletedTutorials(Integer teamNumber, Integer season, Long userId) {
        // Implementation would query UserProgress entity
        return new ArrayList<>(); // Placeholder
    }

    @Override
    public Map<String, Object> generateUserLearningReport(Integer teamNumber, Integer season, Long userId) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("userId", userId);
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("generatedAt", LocalDateTime.now());
        
        // Placeholder data - would be populated from actual progress tracking
        report.put("tutorialsCompleted", 0);
        report.put("totalWatchTime", Duration.ZERO);
        report.put("skillsLearned", new ArrayList<>());
        report.put("certificationsEarned", new ArrayList<>());
        
        return report;
    }

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    @Override
    public List<String> validateTutorial(VideoTutorial tutorial) {
        return validateTutorialContent(tutorial);
    }

    @Override
    public boolean validateVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return false;
        }
        
        // Basic URL validation
        Pattern urlPattern = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be|vimeo\\.com).*",
            Pattern.CASE_INSENSITIVE
        );
        
        return urlPattern.matcher(videoUrl).matches();
    }

    @Override
    public boolean validateUserPermissions(Long tutorialId, Long userId, String operation) {
        switch (operation.toLowerCase()) {
            case "view":
                return canUserViewTutorial(tutorialId, userId, "STUDENT");
            case "edit":
                Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
                if (tutorialOpt.isPresent()) {
                    VideoTutorial tutorial = tutorialOpt.get();
                    return tutorial.getCreatedBy() != null && tutorial.getCreatedBy().getId().equals(userId);
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public Map<String, Object> checkContentQuality(Long tutorialId) {
        Optional<VideoTutorial> tutorialOpt = tutorialRepository.findById(tutorialId);
        if (!tutorialOpt.isPresent()) {
            return new HashMap<>();
        }
        
        VideoTutorial tutorial = tutorialOpt.get();
        Map<String, Object> quality = new HashMap<>();
        
        int score = 100;
        List<String> issues = new ArrayList<>();
        
        // Check basic requirements
        if (tutorial.getDescription() == null || tutorial.getDescription().length() < 100) {
            score -= 20;
            issues.add("Description too short");
        }
        
        if (tutorial.getThumbnailUrl() == null) {
            score -= 10;
            issues.add("No thumbnail");
        }
        
        if (tutorial.getTags().isEmpty()) {
            score -= 10;
            issues.add("No tags");
        }
        
        if (tutorial.getLearningObjectives().isEmpty()) {
            score -= 15;
            issues.add("No learning objectives");
        }
        
        if (!tutorial.getHasSubtitles()) {
            score -= 5;
            issues.add("No subtitles");
        }
        
        quality.put("score", Math.max(0, score));
        quality.put("issues", issues);
        quality.put("hasInteractiveElements", tutorial.hasInteractiveContent());
        quality.put("engagementScore", tutorial.getEngagementScore());
        
        return quality;
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private String generateSlug(String title, Integer teamNumber, Integer season) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        String slug = baseSlug;
        int counter = 1;
        
        // Ensure uniqueness
        while (tutorialRepository.findByTeamNumberAndSeasonAndSlugAndIsActiveTrue(teamNumber, season, slug).isPresent()) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }

    private void copyTutorialProperties(VideoTutorial source, VideoTutorial target) {
        target.setTeamNumber(source.getTeamNumber());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setSummary(source.getSummary());
        target.setCategory(source.getCategory());
        target.setSkillLevel(source.getSkillLevel());
        target.setVideoUrl(source.getVideoUrl());
        target.setThumbnailUrl(source.getThumbnailUrl());
        target.setVideoSource(source.getVideoSource());
        target.setExternalVideoId(source.getExternalVideoId());
        target.setDuration(source.getDuration());
        target.setVideoQuality(source.getVideoQuality());
        target.setTags(new ArrayList<>(source.getTags()));
        target.setTopics(new ArrayList<>(source.getTopics()));
        target.setLearningObjectives(new ArrayList<>(source.getLearningObjectives()));
        target.setSkillsTaught(new ArrayList<>(source.getSkillsTaught()));
        target.setCreatedBy(source.getCreatedBy());
        target.setInstructor(source.getInstructor());
        target.setVisibility(source.getVisibility());
        target.setViewPermission(source.getViewPermission());
    }

    private int calculateSearchRank(VideoTutorial tutorial) {
        int rank = 0;
        
        // Base rank on view count
        rank += Math.min(tutorial.getViewCount().intValue() / 20, 30);
        
        // Completion rate contribution
        rank += (int) (tutorial.getCompletionRate() / 10.0);
        
        // Rating contribution
        if (tutorial.getRatingCount() > 0) {
            rank += (int) (tutorial.getAverageRating() * 4);
        }
        
        // Recent activity boost
        if (tutorial.getLastViewed() != null && tutorial.getLastViewed().isAfter(LocalDateTime.now().minusDays(30))) {
            rank += 10;
        }
        
        // Featured boost
        if (tutorial.getIsFeatured()) {
            rank += 20;
        }
        
        // Quality score boost
        if (tutorial.getQualityScore() != null) {
            rank += tutorial.getQualityScore() / 5;
        }
        
        return Math.min(100, rank);
    }
}