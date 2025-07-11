package org.frcpm.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * FRC Team Ranking entity for competition standings.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 3A - FRC API Integration
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
    
    // Constructors
    public FrcTeamRanking() {}
    
    public FrcTeamRanking(Integer teamNumber, Integer rank, FrcEvent frcEvent) {
        this.teamNumber = teamNumber;
        this.rank = rank;
        this.frcEvent = frcEvent;
    }
    
    // Business Logic Methods
    
    /**
     * Gets the team's win-loss-tie record as a formatted string.
     */
    public String getRecord() {
        return String.format("%d-%d-%d", 
                            wins != null ? wins : 0,
                            losses != null ? losses : 0,
                            ties != null ? ties : 0);
    }
    
    /**
     * Calculates the total number of matches played.
     */
    public int getTotalMatches() {
        return (wins != null ? wins : 0) + 
               (losses != null ? losses : 0) + 
               (ties != null ? ties : 0);
    }
    
    /**
     * Calculates the win percentage.
     */
    public double getWinPercentage() {
        int total = getTotalMatches();
        if (total == 0) return 0.0;
        return ((double) (wins != null ? wins : 0)) / total * 100.0;
    }
    
    /**
     * Determines if this is a qualifying position for playoffs.
     */
    public boolean isPlayoffQualified() {
        // Typically top 8 teams qualify for playoffs
        return rank != null && rank <= 8;
    }
    
    /**
     * Gets a display string for ranking points.
     */
    public String getRankingPointsDisplay() {
        if (rankingPoints == null) return "0.00";
        return rankingPoints.toString();
    }
    
    /**
     * Gets a display string for average points.
     */
    public String getAveragePointsDisplay() {
        if (averagePoints == null) return "0.00";
        return averagePoints.toString();
    }
    
    // Standard Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getTeamNumber() {
        return teamNumber;
    }
    
    public void setTeamNumber(Integer teamNumber) {
        this.teamNumber = teamNumber;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    public Integer getWins() {
        return wins;
    }
    
    public void setWins(Integer wins) {
        this.wins = wins;
    }
    
    public Integer getLosses() {
        return losses;
    }
    
    public void setLosses(Integer losses) {
        this.losses = losses;
    }
    
    public Integer getTies() {
        return ties;
    }
    
    public void setTies(Integer ties) {
        this.ties = ties;
    }
    
    public BigDecimal getRankingPoints() {
        return rankingPoints;
    }
    
    public void setRankingPoints(BigDecimal rankingPoints) {
        this.rankingPoints = rankingPoints;
    }
    
    public BigDecimal getAveragePoints() {
        return averagePoints;
    }
    
    public void setAveragePoints(BigDecimal averagePoints) {
        this.averagePoints = averagePoints;
    }
    
    public FrcEvent getFrcEvent() {
        return frcEvent;
    }
    
    public void setFrcEvent(FrcEvent frcEvent) {
        this.frcEvent = frcEvent;
    }
    
    @Override
    public String toString() {
        return String.format("Team %d - Rank %d (%s)", 
                           teamNumber, rank, getRecord());
    }
}