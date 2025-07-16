package org.frcpm.integration.frc;

import org.frcpm.integration.frc.dto.FRCEventDto;
import org.frcpm.integration.frc.dto.FRCTeamDto;
import org.frcpm.integration.frc.dto.FRCSeasonDto;
import org.frcpm.models.Competition;
import org.frcpm.models.CompetitionSeason;
import org.frcpm.models.CompetitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;

@Service
public class FRCEventsService {
    
    private static final Logger logger = LoggerFactory.getLogger(FRCEventsService.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String teamNumber;
    
    public FRCEventsService(RestTemplate restTemplate,
                           @Value("${app.integrations.frc.events.base-url:https://frc-api.firstinspires.org/v3.0}") String baseUrl,
                           @Value("${app.integrations.frc.events.api-key:}") String apiKey,
                           @Value("${app.team.number:}") String teamNumber) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.teamNumber = teamNumber;
    }
    
    public List<FRCSeasonDto> getSeasons() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FRCSeasonDto[]> response = restTemplate.exchange(
                baseUrl + "/seasons",
                HttpMethod.GET,
                entity,
                FRCSeasonDto[].class
            );
            
            logger.info("Successfully retrieved {} seasons from FRC API", response.getBody().length);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving seasons: {} - {}", e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (ResourceAccessException e) {
            logger.error("Network error retrieving seasons: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Unexpected error retrieving seasons: {}", e.getMessage());
            return List.of();
        }
    }
    
    public Optional<FRCSeasonDto> getCurrentSeason() {
        try {
            List<FRCSeasonDto> seasons = getSeasons();
            Optional<FRCSeasonDto> currentSeason = seasons.stream()
                    .filter(season -> season.isCurrent())
                    .findFirst();
            
            if (currentSeason.isPresent()) {
                logger.info("Found current season: {}", currentSeason.get().getName());
            } else {
                logger.warn("No current season found");
            }
            
            return currentSeason;
        } catch (Exception e) {
            logger.error("Error finding current season: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<FRCEventDto> getEventsBySeason(int year) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FRCEventDto[]> response = restTemplate.exchange(
                baseUrl + "/" + year + "/events",
                HttpMethod.GET,
                entity,
                FRCEventDto[].class
            );
            
            logger.info("Successfully retrieved {} events for season {}", response.getBody().length, year);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving events for season {}: {} - {}", year, e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Error retrieving events for season {}: {}", year, e.getMessage());
            return List.of();
        }
    }
    
    public List<FRCEventDto> getEventsForTeam(int year, String teamNumber) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FRCEventDto[]> response = restTemplate.exchange(
                baseUrl + "/" + year + "/teams/" + teamNumber + "/events",
                HttpMethod.GET,
                entity,
                FRCEventDto[].class
            );
            
            logger.info("Successfully retrieved {} events for team {} in season {}", 
                       response.getBody().length, teamNumber, year);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving events for team {} in season {}: {} - {}", 
                        teamNumber, year, e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Error retrieving events for team {} in season {}: {}", 
                        teamNumber, year, e.getMessage());
            return List.of();
        }
    }
    
    public List<FRCEventDto> getEventsForCurrentTeam(int year) {
        if (teamNumber == null || teamNumber.isEmpty()) {
            logger.warn("No team number configured, cannot retrieve team events");
            return List.of();
        }
        return getEventsForTeam(year, teamNumber);
    }
    
    public Optional<FRCEventDto> getEvent(int year, String eventCode) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FRCEventDto> response = restTemplate.exchange(
                baseUrl + "/" + year + "/events/" + eventCode,
                HttpMethod.GET,
                entity,
                FRCEventDto.class
            );
            
            logger.info("Successfully retrieved event {} for season {}", eventCode, year);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Event {} not found for season {}", eventCode, year);
            } else {
                logger.error("HTTP error retrieving event {} for season {}: {} - {}", 
                           eventCode, year, e.getStatusCode(), e.getMessage());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving event {} for season {}: {}", eventCode, year, e.getMessage());
            return Optional.empty();
        }
    }
    
    public Optional<FRCTeamDto> getTeam(String teamNumber) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FRCTeamDto> response = restTemplate.exchange(
                baseUrl + "/teams/" + teamNumber,
                HttpMethod.GET,
                entity,
                FRCTeamDto.class
            );
            
            logger.info("Successfully retrieved team {}", teamNumber);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Team {} not found", teamNumber);
            } else {
                logger.error("HTTP error retrieving team {}: {} - {}", 
                           teamNumber, e.getStatusCode(), e.getMessage());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving team {}: {}", teamNumber, e.getMessage());
            return Optional.empty();
        }
    }
    
    public Optional<FRCTeamDto> getCurrentTeam() {
        if (teamNumber == null || teamNumber.isEmpty()) {
            logger.warn("No team number configured, cannot retrieve current team");
            return Optional.empty();
        }
        return getTeam(teamNumber);
    }
    
    // Conversion methods for domain objects
    public Competition convertToCompetition(FRCEventDto eventDto) {
        if (eventDto == null) {
            return null;
        }
        
        Competition competition = new Competition();
        competition.setName(eventDto.getName());
        competition.setEventCode(eventDto.getEventCode());
        competition.setStartDate(eventDto.getStartDate());
        competition.setEndDate(eventDto.getEndDate());
        competition.setCity(eventDto.getCity());
        competition.setState(eventDto.getStateProv());
        competition.setCountry(eventDto.getCountry());
        competition.setWebsiteUrl(eventDto.getWebsite());
        competition.setVenue(eventDto.getVenue());
        
        // Map FRC event type to our enum
        competition.setType(mapEventType(eventDto.getType()));
        
        return competition;
    }
    
    public CompetitionSeason convertToSeason(FRCSeasonDto seasonDto) {
        if (seasonDto == null) {
            return null;
        }
        
        CompetitionSeason season = new CompetitionSeason();
        season.setName(seasonDto.getName());
        season.setGameTitle(seasonDto.getGameName());
        season.setKickoffDate(seasonDto.getKickoff());
        season.setStopBuildDate(seasonDto.getStopBuildDay());
        season.setFirstCompetitionDate(seasonDto.getFirstCompetitionDate());
        season.setChampionshipDate(seasonDto.getChampionshipDate());
        
        return season;
    }
    
    // Configuration check methods
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    public boolean hasTeamNumber() {
        return teamNumber != null && !teamNumber.isEmpty();
    }
    
    public String getConfiguredTeamNumber() {
        return teamNumber;
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Authorization", "Basic " + apiKey);
        }
        
        return headers;
    }
    
    private CompetitionType mapEventType(String frcEventType) {
        if (frcEventType == null) {
            return CompetitionType.SCRIMMAGE;
        }
        
        return switch (frcEventType.toLowerCase()) {
            case "regional" -> CompetitionType.REGIONAL;
            case "district" -> CompetitionType.DISTRICT;
            case "district championship", "dcmp" -> CompetitionType.DISTRICT_CHAMPIONSHIP;
            case "championship", "cmp" -> CompetitionType.CHAMPIONSHIP;
            case "offseason" -> CompetitionType.OFF_SEASON;
            default -> CompetitionType.SCRIMMAGE;
        };
    }
}