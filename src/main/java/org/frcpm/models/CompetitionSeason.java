package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "competition_seasons")
@EntityListeners(AuditingEntityListener.class)
public class CompetitionSeason {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name; // e.g., "2024-2025 CRESCENDO"
    
    @NotBlank
    private String gameTitle; // e.g., "CRESCENDO"
    
    @NotNull
    private LocalDate kickoffDate;
    
    @NotNull
    private LocalDate stopBuildDate;
    
    @NotNull
    private LocalDate bagAndTagDate; // deprecated but some teams still track
    
    @NotNull
    private LocalDate firstCompetitionDate;
    
    private LocalDate championshipDate;
    
    @Enumerated(EnumType.STRING)
    private SeasonStatus status;
    
    private String gameManualUrl;
    private String resourcesUrl;
    private String rulesUrl;
    
    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Competition> competitions;
    
    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Project> projects;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public CompetitionSeason() {}
    
    public CompetitionSeason(String name, String gameTitle, LocalDate kickoffDate, 
                           LocalDate stopBuildDate, LocalDate firstCompetitionDate) {
        this.name = name;
        this.gameTitle = gameTitle;
        this.kickoffDate = kickoffDate;
        this.stopBuildDate = stopBuildDate;
        this.firstCompetitionDate = firstCompetitionDate;
        this.status = SeasonStatus.UPCOMING;
    }
    
    // Business methods
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return now.isAfter(kickoffDate) && now.isBefore(stopBuildDate);
    }
    
    public boolean isBuildSeasonComplete() {
        return LocalDate.now().isAfter(stopBuildDate);
    }
    
    public long getDaysUntilStopBuild() {
        return LocalDate.now().until(stopBuildDate).getDays();
    }
    
    public long getBuildSeasonDuration() {
        return kickoffDate.until(stopBuildDate).getDays();
    }
    
    public double getBuildSeasonProgress() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(kickoffDate)) {
            return 0.0;
        }
        if (now.isAfter(stopBuildDate)) {
            return 100.0;
        }
        
        long totalDays = getBuildSeasonDuration();
        long elapsedDays = kickoffDate.until(now).getDays();
        return (double) elapsedDays / totalDays * 100.0;
    }
    
    public boolean isCompetitionSeason() {
        LocalDate now = LocalDate.now();
        return now.isAfter(stopBuildDate) && now.isBefore(championshipDate);
    }
    
    public boolean isKickoffPending() {
        return LocalDate.now().isBefore(kickoffDate);
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }
    
    public LocalDate getKickoffDate() { return kickoffDate; }
    public void setKickoffDate(LocalDate kickoffDate) { this.kickoffDate = kickoffDate; }
    
    public LocalDate getStopBuildDate() { return stopBuildDate; }
    public void setStopBuildDate(LocalDate stopBuildDate) { this.stopBuildDate = stopBuildDate; }
    
    public LocalDate getBagAndTagDate() { return bagAndTagDate; }
    public void setBagAndTagDate(LocalDate bagAndTagDate) { this.bagAndTagDate = bagAndTagDate; }
    
    public LocalDate getFirstCompetitionDate() { return firstCompetitionDate; }
    public void setFirstCompetitionDate(LocalDate firstCompetitionDate) { this.firstCompetitionDate = firstCompetitionDate; }
    
    public LocalDate getChampionshipDate() { return championshipDate; }
    public void setChampionshipDate(LocalDate championshipDate) { this.championshipDate = championshipDate; }
    
    public SeasonStatus getStatus() { return status; }
    public void setStatus(SeasonStatus status) { this.status = status; }
    
    public String getGameManualUrl() { return gameManualUrl; }
    public void setGameManualUrl(String gameManualUrl) { this.gameManualUrl = gameManualUrl; }
    
    public String getResourcesUrl() { return resourcesUrl; }
    public void setResourcesUrl(String resourcesUrl) { this.resourcesUrl = resourcesUrl; }
    
    public String getRulesUrl() { return rulesUrl; }
    public void setRulesUrl(String rulesUrl) { this.rulesUrl = rulesUrl; }
    
    public List<Competition> getCompetitions() { return competitions; }
    public void setCompetitions(List<Competition> competitions) { this.competitions = competitions; }
    
    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}