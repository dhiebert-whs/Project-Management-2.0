package org.frcpm.integration.frc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class FRCSeasonDto {
    
    @JsonProperty("year")
    private Integer year;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("gameName")
    private String gameName;
    
    @JsonProperty("kickoff")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate kickoff;
    
    @JsonProperty("stopBuildDay")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stopBuildDay;
    
    @JsonProperty("firstCompetitionDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstCompetitionDate;
    
    @JsonProperty("championshipDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate championshipDate;
    
    @JsonProperty("current")
    private Boolean current;
    
    @JsonProperty("upcoming")
    private Boolean upcoming;
    
    @JsonProperty("complete")
    private Boolean complete;
    
    @JsonProperty("gameManualUrl")
    private String gameManualUrl;
    
    @JsonProperty("resourcesUrl")
    private String resourcesUrl;
    
    @JsonProperty("rulesUrl")
    private String rulesUrl;
    
    // Constructors
    public FRCSeasonDto() {}
    
    // Business methods
    public boolean isCurrent() {
        return current != null && current;
    }
    
    public boolean isUpcoming() {
        return upcoming != null && upcoming;
    }
    
    public boolean isComplete() {
        return complete != null && complete;
    }
    
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return kickoff != null && stopBuildDay != null && 
               now.isAfter(kickoff) && now.isBefore(stopBuildDay);
    }
    
    public boolean isBuildSeasonComplete() {
        return stopBuildDay != null && LocalDate.now().isAfter(stopBuildDay);
    }
    
    public long getDaysUntilStopBuild() {
        if (stopBuildDay == null) return -1;
        return LocalDate.now().until(stopBuildDay).getDays();
    }
    
    public long getBuildSeasonDuration() {
        if (kickoff == null || stopBuildDay == null) return -1;
        return kickoff.until(stopBuildDay).getDays();
    }
    
    public double getBuildSeasonProgress() {
        if (kickoff == null || stopBuildDay == null) return 0.0;
        
        LocalDate now = LocalDate.now();
        if (now.isBefore(kickoff)) {
            return 0.0;
        }
        if (now.isAfter(stopBuildDay)) {
            return 100.0;
        }
        
        long totalDays = getBuildSeasonDuration();
        long elapsedDays = kickoff.until(now).getDays();
        return (double) elapsedDays / totalDays * 100.0;
    }
    
    public String getDisplayName() {
        if (gameName != null && !gameName.isEmpty()) {
            return year + " - " + gameName;
        }
        return name != null ? name : year.toString();
    }
    
    // Getters and setters
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    
    public LocalDate getKickoff() { return kickoff; }
    public void setKickoff(LocalDate kickoff) { this.kickoff = kickoff; }
    
    public LocalDate getStopBuildDay() { return stopBuildDay; }
    public void setStopBuildDay(LocalDate stopBuildDay) { this.stopBuildDay = stopBuildDay; }
    
    public LocalDate getFirstCompetitionDate() { return firstCompetitionDate; }
    public void setFirstCompetitionDate(LocalDate firstCompetitionDate) { this.firstCompetitionDate = firstCompetitionDate; }
    
    public LocalDate getChampionshipDate() { return championshipDate; }
    public void setChampionshipDate(LocalDate championshipDate) { this.championshipDate = championshipDate; }
    
    public Boolean getCurrent() { return current; }
    public void setCurrent(Boolean current) { this.current = current; }
    
    public Boolean getUpcoming() { return upcoming; }
    public void setUpcoming(Boolean upcoming) { this.upcoming = upcoming; }
    
    public Boolean getComplete() { return complete; }
    public void setComplete(Boolean complete) { this.complete = complete; }
    
    public String getGameManualUrl() { return gameManualUrl; }
    public void setGameManualUrl(String gameManualUrl) { this.gameManualUrl = gameManualUrl; }
    
    public String getResourcesUrl() { return resourcesUrl; }
    public void setResourcesUrl(String resourcesUrl) { this.resourcesUrl = resourcesUrl; }
    
    public String getRulesUrl() { return rulesUrl; }
    public void setRulesUrl(String rulesUrl) { this.rulesUrl = rulesUrl; }
    
    @Override
    public String toString() {
        return String.format("FRCSeasonDto{year=%d, name='%s', gameName='%s', current=%s}",
                           year, name, gameName, current);
    }
}