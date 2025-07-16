package org.frcpm.integration.tba.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TBAMatchDto {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("comp_level")
    private String compLevel;
    
    @JsonProperty("set_number")
    private Integer setNumber;
    
    @JsonProperty("match_number")
    private Integer matchNumber;
    
    @JsonProperty("event_key")
    private String eventKey;
    
    @JsonProperty("time")
    private Long time;
    
    @JsonProperty("actual_time")
    private Long actualTime;
    
    @JsonProperty("predicted_time")
    private Long predictedTime;
    
    @JsonProperty("post_result_time")
    private Long postResultTime;
    
    @JsonProperty("score_breakdown")
    private Map<String, Object> scoreBreakdown;
    
    @JsonProperty("alliances")
    private Map<String, Alliance> alliances;
    
    @JsonProperty("videos")
    private Video[] videos;
    
    @JsonProperty("winning_alliance")
    private String winningAlliance;
    
    // Nested classes for complex objects
    public static class Alliance {
        @JsonProperty("score")
        private Integer score;
        
        @JsonProperty("team_keys")
        private String[] teamKeys;
        
        @JsonProperty("surrogate_team_keys")
        private String[] surrogateTeamKeys;
        
        @JsonProperty("dq_team_keys")
        private String[] dqTeamKeys;
        
        // Getters and setters
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        
        public String[] getTeamKeys() { return teamKeys; }
        public void setTeamKeys(String[] teamKeys) { this.teamKeys = teamKeys; }
        
        public String[] getSurrogateTeamKeys() { return surrogateTeamKeys; }
        public void setSurrogateTeamKeys(String[] surrogateTeamKeys) { this.surrogateTeamKeys = surrogateTeamKeys; }
        
        public String[] getDqTeamKeys() { return dqTeamKeys; }
        public void setDqTeamKeys(String[] dqTeamKeys) { this.dqTeamKeys = dqTeamKeys; }
        
        public boolean hasSurrogates() {
            return surrogateTeamKeys != null && surrogateTeamKeys.length > 0;
        }
        
        public boolean hasDisqualifications() {
            return dqTeamKeys != null && dqTeamKeys.length > 0;
        }
        
        public boolean containsTeam(String teamKey) {
            if (teamKeys == null) return false;
            for (String key : teamKeys) {
                if (key.equals(teamKey)) return true;
            }
            return false;
        }
    }
    
    public static class Video {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("key")
        private String key;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }
    
    // Constructors
    public TBAMatchDto() {}
    
    // Business methods
    public boolean isQualificationMatch() {
        return "qm".equals(compLevel);
    }
    
    public boolean isPlayoffMatch() {
        return "ef".equals(compLevel) || "qf".equals(compLevel) || 
               "sf".equals(compLevel) || "f".equals(compLevel);
    }
    
    public boolean isEliminationMatch() {
        return isPlayoffMatch();
    }
    
    public boolean isFinalMatch() {
        return "f".equals(compLevel);
    }
    
    public boolean hasBeenPlayed() {
        return actualTime != null || winningAlliance != null;
    }
    
    public boolean hasVideo() {
        return videos != null && videos.length > 0;
    }
    
    public String getMatchDisplayName() {
        return switch (compLevel) {
            case "qm" -> "Qualification " + matchNumber;
            case "ef" -> "Eighth-Final " + setNumber + " Match " + matchNumber;
            case "qf" -> "Quarterfinal " + setNumber + " Match " + matchNumber;
            case "sf" -> "Semifinal " + setNumber + " Match " + matchNumber;
            case "f" -> "Final " + matchNumber;
            default -> "Match " + matchNumber;
        };
    }
    
    public Alliance getRedAlliance() {
        return alliances != null ? alliances.get("red") : null;
    }
    
    public Alliance getBlueAlliance() {
        return alliances != null ? alliances.get("blue") : null;
    }
    
    public boolean teamWon(String teamKey) {
        if (winningAlliance == null) return false;
        
        Alliance winningAll = alliances.get(winningAlliance);
        return winningAll != null && winningAll.containsTeam(teamKey);
    }
    
    public boolean teamLost(String teamKey) {
        if (winningAlliance == null) return false;
        
        String losingAllianceColor = "red".equals(winningAlliance) ? "blue" : "red";
        Alliance losingAll = alliances.get(losingAllianceColor);
        return losingAll != null && losingAll.containsTeam(teamKey);
    }
    
    public String getTeamAlliance(String teamKey) {
        if (getRedAlliance() != null && getRedAlliance().containsTeam(teamKey)) {
            return "red";
        }
        if (getBlueAlliance() != null && getBlueAlliance().containsTeam(teamKey)) {
            return "blue";
        }
        return null;
    }
    
    public boolean isUpcoming() {
        return time != null && time > System.currentTimeMillis() / 1000;
    }
    
    public boolean isInProgress() {
        return time != null && time <= System.currentTimeMillis() / 1000 && !hasBeenPlayed();
    }
    
    public boolean isComplete() {
        return hasBeenPlayed();
    }
    
    public java.time.Instant getScheduledTime() {
        return time != null ? java.time.Instant.ofEpochSecond(time) : null;
    }
    
    public java.time.Instant getActualStartTime() {
        return actualTime != null ? java.time.Instant.ofEpochSecond(actualTime) : null;
    }
    
    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getCompLevel() { return compLevel; }
    public void setCompLevel(String compLevel) { this.compLevel = compLevel; }
    
    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }
    
    public Integer getMatchNumber() { return matchNumber; }
    public void setMatchNumber(Integer matchNumber) { this.matchNumber = matchNumber; }
    
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    
    public Long getTime() { return time; }
    public void setTime(Long time) { this.time = time; }
    
    public Long getActualTime() { return actualTime; }
    public void setActualTime(Long actualTime) { this.actualTime = actualTime; }
    
    public Long getPredictedTime() { return predictedTime; }
    public void setPredictedTime(Long predictedTime) { this.predictedTime = predictedTime; }
    
    public Long getPostResultTime() { return postResultTime; }
    public void setPostResultTime(Long postResultTime) { this.postResultTime = postResultTime; }
    
    public Map<String, Object> getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(Map<String, Object> scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
    
    public Map<String, Alliance> getAlliances() { return alliances; }
    public void setAlliances(Map<String, Alliance> alliances) { this.alliances = alliances; }
    
    public Video[] getVideos() { return videos; }
    public void setVideos(Video[] videos) { this.videos = videos; }
    
    public String getWinningAlliance() { return winningAlliance; }
    public void setWinningAlliance(String winningAlliance) { this.winningAlliance = winningAlliance; }
    
    @Override
    public String toString() {
        return String.format("TBAMatchDto{key='%s', compLevel='%s', matchNumber=%d, eventKey='%s'}",
                           key, compLevel, matchNumber, eventKey);
    }
}