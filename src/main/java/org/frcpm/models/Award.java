// src/main/java/org/frcpm/models/Award.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Award model for tracking FRC team awards and achievements.
 * 
 * Represents awards earned by FRC teams at competitions, including
 * regional awards, district points, special recognitions, and championship honors.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-Awards
 * @since Phase 4A.5 Regional Award Tracking System
 */
@Entity
@Table(name = "awards")
public class Award {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 150)
    private String awardName;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AwardType awardType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AwardLevel awardLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionType competitionType;
    
    // Competition and event details
    @Column(nullable = false, length = 100)
    private String eventName;
    
    @Column(nullable = false, length = 20)
    private String eventCode; // Official FRC event code
    
    @Column(nullable = false)
    private LocalDate eventDate;
    
    @Column(length = 100)
    private String eventLocation;
    
    @Column(nullable = false)
    private Integer season; // FRC season year (e.g., 2024)
    
    @Column(length = 50)
    private String gameName; // Game name for the season
    
    // Team information
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(length = 100)
    private String teamName;
    
    // Award specifics
    @Column(nullable = false)
    private Integer placement = 1; // 1st, 2nd, 3rd place, or ranking
    
    @Column(nullable = false)
    private Integer districtPoints = 0; // District championship points earned
    
    @Column(nullable = false)
    private Boolean isDistrictPointsEarning = false;
    
    @Column(nullable = false)
    private Boolean isChampionshipQualifying = false;
    
    @Column(nullable = false)
    private Boolean isSpecialRecognition = false;
    
    // Award details and recognition
    @Column(length = 1000)
    private String awardCriteria;
    
    @Column(length = 1000)
    private String achievementDescription; // What the team did to earn this award
    
    @Column(length = 1000)
    private String judgeComments; // Comments from judges or event organizers
    
    @Column(length = 500)
    private String presentedBy; // Who presented the award
    
    // Administrative fields
    @Column(nullable = false)
    private Boolean isVerified = false; // Verified against official FRC records
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private TeamMember recordedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private TeamMember verifiedBy;
    
    @Column
    private LocalDateTime verifiedAt;
    
    // Associated team members (for individual recognition awards)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "award_team_members",
        joinColumns = @JoinColumn(name = "award_id"),
        inverseJoinColumns = @JoinColumn(name = "team_member_id")
    )
    private List<TeamMember> recognizedMembers = new ArrayList<>();
    
    // Media and documentation
    @Column(length = 500)
    private String photoUrl; // URL to award photo
    
    @Column(length = 500)
    private String certificateUrl; // URL to certificate or official document
    
    @Column(length = 500)
    private String videoUrl; // URL to award ceremony video
    
    @Column(length = 1000)
    private String additionalNotes;
    
    /**
     * Types of FRC awards
     */
    public enum AwardType {
        // Competition Performance Awards
        WINNER("Winner", "1st Place Alliance"),
        FINALIST("Finalist", "2nd Place Alliance"),
        SEMIFINALIST("Semifinalist", "Reached semifinals"),
        QUARTERFINALIST("Quarterfinalist", "Reached quarterfinals"),
        
        // Judged Awards
        CHAIRMANS("Chairman's Award", "Highest honor in FIRST"),
        ENGINEERING_INSPIRATION("Engineering Inspiration", "Celebrates outstanding success in advancing respect and appreciation for engineering"),
        ROOKIE_ALL_STAR("Rookie All Star", "Outstanding rookie team"),
        ROOKIE_INSPIRATION("Rookie Inspiration", "Rookie team that is a role model"),
        GRACIOUS_PROFESSIONALISM("Gracious Professionalism", "Embodies FIRST values"),
        QUALITY("Quality Award", "Machine design and fabrication excellence"),
        SAFETY("Safety Award", "Comprehensive safety program"),
        SPORTSMANSHIP("Sportsmanship Award", "Outstanding display of good sportsmanship"),
        CREATIVITY("Creativity Award", "Creative design and innovative thinking"),
        ENGINEERING_EXCELLENCE("Engineering Excellence", "Excellence in engineering design and functionality"),
        ENTREPRENEURSHIP("Entrepreneurship Award", "Entrepreneurship in developing team sustainability"),
        INDUSTRIAL_DESIGN("Industrial Design Award", "Excellence in industrial design"),
        IMAGERY("Imagery Award", "Excellence in promoting FIRST through visual communications"),
        MEDIA_AND_TECHNOLOGY("Media and Technology Innovation", "Innovation in media and technology"),
        
        // Team Spirit and Community Awards
        TEAM_SPIRIT("Team Spirit Award", "Exceptional enthusiasm and spirit"),
        VOLUNTEER_OF_THE_YEAR("Volunteer of the Year", "Outstanding volunteer contribution"),
        WOODIE_FLOWERS("Woodie Flowers Finalist Award", "Outstanding mentor"),
        DEANS_LIST("Dean's List Finalist", "Outstanding student leadership"),
        
        // Technical and Innovation Awards
        INNOVATION_IN_CONTROL("Innovation in Control Award", "Innovative use of control systems"),
        EXCELLENCE_IN_DESIGN("Excellence in Design Award", "Computer-Aided Design excellence"),
        INNOVATION_IN_DESIGN("Innovation in Design Award", "Innovative design approaches"),
        
        // Special Recognition
        DISTRICT_CHAMPIONSHIP("District Championship", "Won district championship"),
        REGIONAL_CHAMPIONSHIP("Regional Championship", "Won regional championship"),
        DIVISION_WINNER("Division Winner", "Won championship division"),
        WORLD_CHAMPIONSHIP_SUBDIVISION("World Championship Subdivision Winner", "Won championship subdivision"),
        WORLD_CHAMPIONSHIP("World Championship", "FIRST Championship winner"),
        
        // Other Recognition
        JUDGES_AWARD("Judges' Award", "Special recognition by judges"),
        SCHOLARSHIP("Scholarship Award", "Educational scholarship recognition"),
        HALL_OF_FAME("Hall of Fame", "Inducted into FRC Hall of Fame"),
        OTHER("Other Award", "Other recognition or award");
        
        private final String displayName;
        private final String description;
        
        AwardType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Award levels in the FRC competition hierarchy
     */
    public enum AwardLevel {
        WORLD_CHAMPIONSHIP("World Championship", "FIRST Championship level"),
        DIVISION("Division", "Championship division level"),
        DISTRICT_CHAMPIONSHIP("District Championship", "District championship level"),
        REGIONAL("Regional", "Regional competition level"),
        DISTRICT_EVENT("District Event", "District event level"),
        OFF_SEASON("Off-Season", "Off-season competition"),
        SCRIMMAGE("Scrimmage", "Practice or scrimmage event"),
        SPECIAL("Special Event", "Special recognition or event");
        
        private final String displayName;
        private final String description;
        
        AwardLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Types of FRC competitions
     */
    public enum CompetitionType {
        REGIONAL("Regional Competition", "Traditional regional competition format"),
        DISTRICT("District Competition", "District competition format"),
        DISTRICT_CHAMPIONSHIP("District Championship", "District championship event"),
        CHAMPIONSHIP_DIVISION("Championship Division", "World championship division"),
        EINSTEIN_FIELD("Einstein Field", "Championship finals field"),
        OFF_SEASON("Off-Season Event", "Off-season competition"),
        PRESEASON("Preseason Event", "Preseason kickoff or event"),
        SCRIMMAGE("Scrimmage", "Practice event"),
        DEMONSTRATION("Demonstration", "Public demonstration"),
        SPECIAL_EVENT("Special Event", "Special competition or event");
        
        private final String displayName;
        private final String description;
        
        CompetitionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Constructors
    public Award() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Award(String awardName, AwardType awardType, AwardLevel awardLevel,
                CompetitionType competitionType, String eventName, String eventCode,
                LocalDate eventDate, Integer teamNumber, Integer season) {
        this();
        this.awardName = awardName;
        this.awardType = awardType;
        this.awardLevel = awardLevel;
        this.competitionType = competitionType;
        this.eventName = eventName;
        this.eventCode = eventCode;
        this.eventDate = eventDate;
        this.teamNumber = teamNumber;
        this.season = season;
    }
    
    // Business Methods
    
    /**
     * Determines if this is a championship-qualifying award.
     */
    public boolean isChampionshipQualifier() {
        return isChampionshipQualifying || 
               awardType == AwardType.CHAIRMANS ||
               awardType == AwardType.ENGINEERING_INSPIRATION ||
               awardType == AwardType.ROOKIE_ALL_STAR ||
               awardType == AwardType.WINNER ||
               awardLevel == AwardLevel.DISTRICT_CHAMPIONSHIP ||
               awardLevel == AwardLevel.REGIONAL;
    }
    
    /**
     * Calculates the prestige score of this award.
     */
    public int getPrestigeScore() {
        int score = 0;
        
        // Base award type scoring
        score += switch (awardType) {
            case CHAIRMANS -> 100;
            case WORLD_CHAMPIONSHIP -> 95;
            case DIVISION_WINNER -> 90;
            case WORLD_CHAMPIONSHIP_SUBDIVISION -> 85;
            case DISTRICT_CHAMPIONSHIP, REGIONAL_CHAMPIONSHIP -> 80;
            case WINNER -> 75;
            case ENGINEERING_INSPIRATION -> 70;
            case FINALIST -> 65;
            case ENGINEERING_EXCELLENCE -> 60;
            case ROOKIE_ALL_STAR -> 55;
            case QUALITY, SAFETY -> 50;
            case SEMIFINALIST -> 45;
            case INNOVATION_IN_CONTROL, INNOVATION_IN_DESIGN -> 40;
            case CREATIVITY, ENTREPRENEURSHIP -> 35;
            case QUARTERFINALIST -> 30;
            case SPORTSMANSHIP, TEAM_SPIRIT -> 25;
            case GRACIOUS_PROFESSIONALISM -> 20;
            default -> 10;
        };
        
        // Award level multiplier
        score = (int) (score * switch (awardLevel) {
            case WORLD_CHAMPIONSHIP -> 1.5;
            case DIVISION -> 1.4;
            case DISTRICT_CHAMPIONSHIP -> 1.3;
            case REGIONAL -> 1.2;
            case DISTRICT_EVENT -> 1.0;
            case OFF_SEASON -> 0.8;
            case SCRIMMAGE -> 0.5;
            case SPECIAL -> 1.1;
        });
        
        return score;
    }
    
    /**
     * Determines if this award contributes to district championship qualification.
     */
    public boolean contributesToDistrictChampionship() {
        return isDistrictPointsEarning && districtPoints > 0;
    }
    
    /**
     * Gets the display text for the award placement.
     */
    public String getPlacementText() {
        if (placement == 1) return "1st Place";
        if (placement == 2) return "2nd Place";
        if (placement == 3) return "3rd Place";
        return placement + "th Place";
    }
    
    /**
     * Checks if this award is from the current season.
     */
    public boolean isCurrentSeason() {
        return season != null && season == LocalDate.now().getYear();
    }
    
    /**
     * Gets the award's age in years.
     */
    public int getAwardAge() {
        return LocalDate.now().getYear() - (season != null ? season : LocalDate.now().getYear());
    }
    
    /**
     * Determines if this award needs verification.
     */
    public boolean needsVerification() {
        return !isVerified && (
            awardType == AwardType.CHAIRMANS ||
            awardType == AwardType.WINNER ||
            awardType == AwardType.DISTRICT_CHAMPIONSHIP ||
            awardType == AwardType.REGIONAL_CHAMPIONSHIP ||
            districtPoints > 50
        );
    }
    
    /**
     * Verifies the award with official records.
     */
    public void verify(TeamMember verifier) {
        this.isVerified = true;
        this.verifiedBy = verifier;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Adds a team member to the recognition list.
     */
    public void addRecognizedMember(TeamMember member) {
        if (!recognizedMembers.contains(member)) {
            recognizedMembers.add(member);
        }
    }
    
    /**
     * Removes a team member from the recognition list.
     */
    public void removeRecognizedMember(TeamMember member) {
        recognizedMembers.remove(member);
    }
    
    /**
     * Creates a summary description of the award.
     */
    public String getAwardSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(awardName);
        
        if (placement > 1) {
            summary.append(" (").append(getPlacementText()).append(")");
        }
        
        summary.append(" at ").append(eventName);
        
        if (eventDate != null) {
            summary.append(" (").append(eventDate.getYear()).append(")");
        }
        
        if (districtPoints > 0) {
            summary.append(" - ").append(districtPoints).append(" district points");
        }
        
        return summary.toString();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAwardName() { return awardName; }
    public void setAwardName(String awardName) { this.awardName = awardName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public AwardType getAwardType() { return awardType; }
    public void setAwardType(AwardType awardType) { this.awardType = awardType; }
    
    public AwardLevel getAwardLevel() { return awardLevel; }
    public void setAwardLevel(AwardLevel awardLevel) { this.awardLevel = awardLevel; }
    
    public CompetitionType getCompetitionType() { return competitionType; }
    public void setCompetitionType(CompetitionType competitionType) { this.competitionType = competitionType; }
    
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    
    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public Integer getPlacement() { return placement; }
    public void setPlacement(Integer placement) { this.placement = placement; }
    
    public Integer getDistrictPoints() { return districtPoints; }
    public void setDistrictPoints(Integer districtPoints) { this.districtPoints = districtPoints; }
    
    public Boolean getIsDistrictPointsEarning() { return isDistrictPointsEarning; }
    public void setIsDistrictPointsEarning(Boolean isDistrictPointsEarning) { this.isDistrictPointsEarning = isDistrictPointsEarning; }
    
    public Boolean getIsChampionshipQualifying() { return isChampionshipQualifying; }
    public void setIsChampionshipQualifying(Boolean isChampionshipQualifying) { this.isChampionshipQualifying = isChampionshipQualifying; }
    
    public Boolean getIsSpecialRecognition() { return isSpecialRecognition; }
    public void setIsSpecialRecognition(Boolean isSpecialRecognition) { this.isSpecialRecognition = isSpecialRecognition; }
    
    public String getAwardCriteria() { return awardCriteria; }
    public void setAwardCriteria(String awardCriteria) { this.awardCriteria = awardCriteria; }
    
    public String getAchievementDescription() { return achievementDescription; }
    public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }
    
    public String getJudgeComments() { return judgeComments; }
    public void setJudgeComments(String judgeComments) { this.judgeComments = judgeComments; }
    
    public String getPresentedBy() { return presentedBy; }
    public void setPresentedBy(String presentedBy) { this.presentedBy = presentedBy; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getRecordedBy() { return recordedBy; }
    public void setRecordedBy(TeamMember recordedBy) { this.recordedBy = recordedBy; }
    
    public TeamMember getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(TeamMember verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    
    public List<TeamMember> getRecognizedMembers() { return recognizedMembers; }
    public void setRecognizedMembers(List<TeamMember> recognizedMembers) { this.recognizedMembers = recognizedMembers; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    
    @Override
    public String toString() {
        return String.format("Award{id=%d, name='%s', type=%s, level=%s, team=%d, event='%s', season=%d}", 
                           id, awardName, awardType, awardLevel, teamNumber, eventName, season);
    }
}