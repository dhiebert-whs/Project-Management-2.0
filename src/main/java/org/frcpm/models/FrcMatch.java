package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * FRC Match entity representing individual competition matches.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 3A - FRC API Integration
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
    
    // Enums
    public enum CompetitionLevel {
        PRACTICE, QUALIFICATION, PLAYOFF
    }
    
    public enum MatchResult {
        RED_WIN, BLUE_WIN, TIE, NO_RESULT
    }
    
    // Constructors
    public FrcMatch() {}
    
    public FrcMatch(Integer matchNumber, CompetitionLevel competitionLevel, FrcEvent frcEvent) {
        this.matchNumber = matchNumber;
        this.competitionLevel = competitionLevel;
        this.frcEvent = frcEvent;
    }
    
    // Business Logic Methods
    
    /**
     * Determines if the match has been played.
     */
    public boolean isCompleted() {
        return matchResult != null && matchResult != MatchResult.NO_RESULT;
    }
    
    /**
     * Gets the winning alliance.
     */
    public String getWinningAlliance() {
        if (matchResult == null) return "TBD";
        switch (matchResult) {
            case RED_WIN: return "Red";
            case BLUE_WIN: return "Blue";
            case TIE: return "Tie";
            default: return "TBD";
        }
    }
    
    /**
     * Gets the match display name.
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(competitionLevel.toString());
        if (setNumber != null && setNumber > 1) {
            sb.append(" ").append(setNumber);
        }
        sb.append(" Match ").append(matchNumber);
        return sb.toString();
    }
    
    // Standard Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getMatchNumber() {
        return matchNumber;
    }
    
    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }
    
    public CompetitionLevel getCompetitionLevel() {
        return competitionLevel;
    }
    
    public void setCompetitionLevel(CompetitionLevel competitionLevel) {
        this.competitionLevel = competitionLevel;
    }
    
    public Integer getSetNumber() {
        return setNumber;
    }
    
    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public LocalDateTime getActualTime() {
        return actualTime;
    }
    
    public void setActualTime(LocalDateTime actualTime) {
        this.actualTime = actualTime;
    }
    
    public String getRedAllianceTeams() {
        return redAllianceTeams;
    }
    
    public void setRedAllianceTeams(String redAllianceTeams) {
        this.redAllianceTeams = redAllianceTeams;
    }
    
    public String getBlueAllianceTeams() {
        return blueAllianceTeams;
    }
    
    public void setBlueAllianceTeams(String blueAllianceTeams) {
        this.blueAllianceTeams = blueAllianceTeams;
    }
    
    public Integer getRedScore() {
        return redScore;
    }
    
    public void setRedScore(Integer redScore) {
        this.redScore = redScore;
    }
    
    public Integer getBlueScore() {
        return blueScore;
    }
    
    public void setBlueScore(Integer blueScore) {
        this.blueScore = blueScore;
    }
    
    public MatchResult getMatchResult() {
        return matchResult;
    }
    
    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }
    
    public FrcEvent getFrcEvent() {
        return frcEvent;
    }
    
    public void setFrcEvent(FrcEvent frcEvent) {
        this.frcEvent = frcEvent;
    }
    
    @Override
    public String toString() {
        return getDisplayName() + " - " + frcEvent.getEventCode();
    }
}