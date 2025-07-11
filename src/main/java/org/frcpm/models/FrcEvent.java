package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FRC Event entity representing competitions from FIRST API.
 * 
 * This entity stores FRC competition events and can be linked to projects
 * for deadline management and timeline integration.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 3A - FRC API Integration
 */
@Entity
@Table(name = "frc_events")
public class FrcEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "event_code", unique = true, nullable = false)
    private String eventCode;
    
    @NotBlank
    @Column(name = "event_name", nullable = false)
    private String eventName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
    
    @NotNull
    @Column(name = "season_year", nullable = false)
    private Integer seasonYear;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "venue")
    private String venue;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state_province")
    private String stateProvince;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "live_stream_url")
    private String liveStreamUrl;
    
    @Column(name = "registration_open")
    private LocalDateTime registrationOpen;
    
    @Column(name = "registration_close")
    private LocalDateTime registrationClose;
    
    @Column(name = "team_count")
    private Integer teamCount;
    
    @Column(name = "is_official")
    private boolean isOfficial = true;
    
    @Column(name = "is_public")
    private boolean isPublic = true;
    
    // API metadata
    @Column(name = "last_synced")
    private LocalDateTime lastSynced;
    
    @Column(name = "api_response_hash")
    private String apiResponseHash;
    
    // Relationships
    @OneToMany(mappedBy = "frcEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FrcMatch> matches = new ArrayList<>();
    
    @OneToMany(mappedBy = "frcEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FrcTeamRanking> rankings = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_project_id")
    private Project linkedProject;
    
    // Event Types
    public enum EventType {
        KICKOFF("Kickoff"),
        REGIONAL("Regional"),
        DISTRICT("District"),
        DISTRICT_CHAMPIONSHIP("District Championship"),
        CHAMPIONSHIP_DIVISION("Championship Division"),
        CHAMPIONSHIP("Championship"),
        FESTIVAL_OF_CHAMPIONS("Festival of Champions"),
        OFFSEASON("Offseason"),
        PRESEASON("Preseason"),
        UNLABELED("Unlabeled");
        
        private final String displayName;
        
        EventType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static EventType fromString(String value) {
            if (value == null) return UNLABELED;
            
            String normalized = value.toUpperCase().replace(" ", "_");
            try {
                return EventType.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                return UNLABELED;
            }
        }
    }
    
    // Constructors
    public FrcEvent() {}
    
    public FrcEvent(String eventCode, String eventName, EventType eventType, Integer seasonYear) {
        this.eventCode = eventCode;
        this.eventName = eventName;
        this.eventType = eventType;
        this.seasonYear = seasonYear;
    }
    
    // Business Logic Methods
    
    /**
     * Determines if this event is in the current season.
     */
    public boolean isCurrentSeason() {
        int currentYear = LocalDate.now().getYear();
        return seasonYear != null && seasonYear == currentYear;
    }
    
    /**
     * Determines if this event is upcoming (starts in the future).
     */
    public boolean isUpcoming() {
        return startDate != null && startDate.isAfter(LocalDate.now());
    }
    
    /**
     * Determines if this event is currently active.
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return startDate != null && endDate != null &&
               !startDate.isAfter(today) && !endDate.isBefore(today);
    }
    
    /**
     * Determines if this event has concluded.
     */
    public boolean isCompleted() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }
    
    /**
     * Gets the duration of the event in days.
     */
    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    /**
     * Gets days until the event starts.
     */
    public long getDaysUntilStart() {
        if (startDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), startDate);
    }
    
    /**
     * Determines if registration is currently open.
     */
    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return registrationOpen != null && registrationClose != null &&
               !registrationOpen.isAfter(now) && !registrationClose.isBefore(now);
    }
    
    /**
     * Gets the full display name including location.
     */
    public String getFullDisplayName() {
        StringBuilder sb = new StringBuilder(eventName);
        if (city != null && !city.isEmpty()) {
            sb.append(" - ").append(city);
            if (stateProvince != null && !stateProvince.isEmpty()) {
                sb.append(", ").append(stateProvince);
            }
        }
        return sb.toString();
    }
    
    // Standard Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Integer getSeasonYear() {
        return seasonYear;
    }

    public void setSeasonYear(Integer seasonYear) {
        this.seasonYear = seasonYear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLiveStreamUrl() {
        return liveStreamUrl;
    }

    public void setLiveStreamUrl(String liveStreamUrl) {
        this.liveStreamUrl = liveStreamUrl;
    }

    public LocalDateTime getRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(LocalDateTime registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public LocalDateTime getRegistrationClose() {
        return registrationClose;
    }

    public void setRegistrationClose(LocalDateTime registrationClose) {
        this.registrationClose = registrationClose;
    }

    public Integer getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(Integer teamCount) {
        this.teamCount = teamCount;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        isOfficial = official;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getLastSynced() {
        return lastSynced;
    }

    public void setLastSynced(LocalDateTime lastSynced) {
        this.lastSynced = lastSynced;
    }

    public String getApiResponseHash() {
        return apiResponseHash;
    }

    public void setApiResponseHash(String apiResponseHash) {
        this.apiResponseHash = apiResponseHash;
    }

    public List<FrcMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<FrcMatch> matches) {
        this.matches = matches;
    }

    public List<FrcTeamRanking> getRankings() {
        return rankings;
    }

    public void setRankings(List<FrcTeamRanking> rankings) {
        this.rankings = rankings;
    }

    public Project getLinkedProject() {
        return linkedProject;
    }

    public void setLinkedProject(Project linkedProject) {
        this.linkedProject = linkedProject;
    }
    
    @Override
    public String toString() {
        return eventName + " (" + eventCode + ")";
    }
}

// src/main/java/org/frcpm/models/FrcMatch.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * FRC Match entity representing individual competition matches.
 */
@Entity
@Table(name = "frc_matches")
public class FrcMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "match_number", nullable = false)
    private Integer matchNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "competition_level", nullable = false)
    private CompetitionLevel competitionLevel;
    
    @Column(name = "set_number")
    private Integer setNumber;
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Column(name = "actual_time")
    private LocalDateTime actualTime;
    
    @Column(name = "red_alliance_teams")
    private String redAllianceTeams;  // JSON array of team numbers
    
    @Column(name = "blue_alliance_teams")
    private String blueAllianceTeams; // JSON array of team numbers
    
    @Column(name = "red_score")
    private Integer redScore;
    
    @Column(name = "blue_score")
    private Integer blueScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "match_result")
    private MatchResult matchResult;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frc_event_id", nullable = false)
    private FrcEvent frcEvent;
    
    public enum CompetitionLevel {
        PRACTICE, QUALIFICATION, PLAYOFF
    }
    
    public enum MatchResult {
        RED_WIN, BLUE_WIN, TIE, NO_RESULT
    }
    
    // Constructors, getters, setters
    public FrcMatch() {}
    
    // Standard getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getMatchNumber() { return matchNumber; }
    public void setMatchNumber(Integer matchNumber) { this.matchNumber = matchNumber; }
    
    public CompetitionLevel getCompetitionLevel() { return competitionLevel; }
    public void setCompetitionLevel(CompetitionLevel competitionLevel) { this.competitionLevel = competitionLevel; }
    
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public FrcEvent getFrcEvent() { return frcEvent; }
    public void setFrcEvent(FrcEvent frcEvent) { this.frcEvent = frcEvent; }
    
    // Additional getters/setters omitted for brevity
}

// src/main/java/org/frcpm/models/FrcTeamRanking.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * FRC Team Ranking entity for competition standings.
 */
@Entity
@Table(name = "frc_team_rankings")
public class FrcTeamRanking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "team_number", nullable = false)
    private Integer teamNumber;
    
    @Column(name = "rank", nullable = false)
    private Integer rank;
    
    @Column(name = "wins")
    private Integer wins;
    
    @Column(name = "losses")
    private Integer losses;
    
    @Column(name = "ties")
    private Integer ties;
    
    @Column(name = "ranking_points", precision = 10, scale = 2)
    private BigDecimal rankingPoints;
    
    @Column(name = "average_points", precision = 10, scale = 2)
    private BigDecimal averagePoints;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frc_event_id", nullable = false)
    private FrcEvent frcEvent;
    
    // Constructors, getters, setters
    public FrcTeamRanking() {}
    
    // Business logic
    public String getRecord() {
        return String.format("%d-%d-%d", 
                            wins != null ? wins : 0,
                            losses != null ? losses : 0,
                            ties != null ? ties : 0);
    }
    
    // Standard getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    
    public FrcEvent getFrcEvent() { return frcEvent; }
    public void setFrcEvent(FrcEvent frcEvent) { this.frcEvent = frcEvent; }
    
    // Additional getters/setters omitted for brevity
}