package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "competition_results")
@EntityListeners(AuditingEntityListener.class)
public class CompetitionResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;
    
    private Integer qualificationRank;
    private Integer allianceRank;
    private Integer finalRank;
    
    private Double qualificationScore;
    private Double eliminationScore;
    private Double averageScore;
    
    private Integer wins;
    private Integer losses;
    private Integer ties;
    
    private String awards; // JSON string for multiple awards
    private String notes;
    
    private boolean rookieAllStar = false;
    private boolean chairmansAward = false;
    private boolean engineeringInspiration = false;
    private boolean graciousprofessionalism = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public CompetitionResult() {}
    
    public CompetitionResult(Competition competition) {
        this.competition = competition;
    }
    
    // Business methods
    public double getWinPercentage() {
        int totalMatches = wins + losses + ties;
        if (totalMatches == 0) return 0.0;
        return (double) wins / totalMatches * 100.0;
    }
    
    public boolean hasAnyAward() {
        return rookieAllStar || chairmansAward || engineeringInspiration || graciousprofessionalism;
    }
    
    public String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (finalRank != null) {
            summary.append("Final Rank: ").append(finalRank);
        } else if (qualificationRank != null) {
            summary.append("Qualification Rank: ").append(qualificationRank);
        }
        
        if (wins != null && losses != null) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("Record: ").append(wins).append("-").append(losses);
            if (ties != null && ties > 0) {
                summary.append("-").append(ties);
            }
        }
        
        return summary.toString();
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Competition getCompetition() { return competition; }
    public void setCompetition(Competition competition) { this.competition = competition; }
    
    public Integer getQualificationRank() { return qualificationRank; }
    public void setQualificationRank(Integer qualificationRank) { this.qualificationRank = qualificationRank; }
    
    public Integer getAllianceRank() { return allianceRank; }
    public void setAllianceRank(Integer allianceRank) { this.allianceRank = allianceRank; }
    
    public Integer getFinalRank() { return finalRank; }
    public void setFinalRank(Integer finalRank) { this.finalRank = finalRank; }
    
    public Double getQualificationScore() { return qualificationScore; }
    public void setQualificationScore(Double qualificationScore) { this.qualificationScore = qualificationScore; }
    
    public Double getEliminationScore() { return eliminationScore; }
    public void setEliminationScore(Double eliminationScore) { this.eliminationScore = eliminationScore; }
    
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    
    public Integer getWins() { return wins; }
    public void setWins(Integer wins) { this.wins = wins; }
    
    public Integer getLosses() { return losses; }
    public void setLosses(Integer losses) { this.losses = losses; }
    
    public Integer getTies() { return ties; }
    public void setTies(Integer ties) { this.ties = ties; }
    
    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isRookieAllStar() { return rookieAllStar; }
    public void setRookieAllStar(boolean rookieAllStar) { this.rookieAllStar = rookieAllStar; }
    
    public boolean isChairmansAward() { return chairmansAward; }
    public void setChairmansAward(boolean chairmansAward) { this.chairmansAward = chairmansAward; }
    
    public boolean isEngineeringInspiration() { return engineeringInspiration; }
    public void setEngineeringInspiration(boolean engineeringInspiration) { this.engineeringInspiration = engineeringInspiration; }
    
    public boolean isGraciousprofessionalism() { return graciousprofessionalism; }
    public void setGraciousprofessionalism(boolean graciousprofessionalism) { this.graciousprofessionalism = graciousprofessionalism; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}