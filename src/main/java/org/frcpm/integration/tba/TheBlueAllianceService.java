package org.frcpm.integration.tba;

import org.frcpm.integration.tba.dto.TBAEventDto;
import org.frcpm.integration.tba.dto.TBATeamDto;
import org.frcpm.integration.tba.dto.TBAMatchDto;
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
public class TheBlueAllianceService {
    
    private static final Logger logger = LoggerFactory.getLogger(TheBlueAllianceService.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String teamNumber;
    
    public TheBlueAllianceService(RestTemplate restTemplate,
                                 @Value("${app.integrations.tba.base-url:https://www.thebluealliance.com/api/v3}") String baseUrl,
                                 @Value("${app.integrations.tba.api-key:}") String apiKey,
                                 @Value("${app.team.number:}") String teamNumber) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.teamNumber = teamNumber;
    }
    
    public Optional<TBATeamDto> getTeam(String teamKey) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TBATeamDto> response = restTemplate.exchange(
                baseUrl + "/team/" + teamKey,
                HttpMethod.GET,
                entity,
                TBATeamDto.class
            );
            
            logger.info("Successfully retrieved team {} from TBA", teamKey);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Team {} not found in TBA", teamKey);
            } else {
                logger.error("HTTP error retrieving team {} from TBA: {} - {}", 
                           teamKey, e.getStatusCode(), e.getMessage());
            }
            return Optional.empty();
        } catch (ResourceAccessException e) {
            logger.error("Network error retrieving team {} from TBA: {}", teamKey, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error retrieving team {} from TBA: {}", teamKey, e.getMessage());
            return Optional.empty();
        }
    }
    
    public Optional<TBATeamDto> getCurrentTeam() {
        if (teamNumber == null || teamNumber.isEmpty()) {
            logger.warn("No team number configured, cannot retrieve current team from TBA");
            return Optional.empty();
        }
        return getTeam("frc" + teamNumber);
    }
    
    public List<TBAEventDto> getTeamEvents(String teamKey, int year) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TBAEventDto[]> response = restTemplate.exchange(
                baseUrl + "/team/" + teamKey + "/events/" + year,
                HttpMethod.GET,
                entity,
                TBAEventDto[].class
            );
            
            logger.info("Successfully retrieved {} events for team {} in {} from TBA", 
                       response.getBody().length, teamKey, year);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving events for team {} in {} from TBA: {} - {}", 
                        teamKey, year, e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Error retrieving events for team {} in {} from TBA: {}", 
                        teamKey, year, e.getMessage());
            return List.of();
        }
    }
    
    public List<TBAEventDto> getCurrentTeamEvents(int year) {
        if (teamNumber == null || teamNumber.isEmpty()) {
            logger.warn("No team number configured, cannot retrieve team events from TBA");
            return List.of();
        }
        return getTeamEvents("frc" + teamNumber, year);
    }
    
    public List<TBAMatchDto> getTeamMatches(String teamKey, String eventKey) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TBAMatchDto[]> response = restTemplate.exchange(
                baseUrl + "/team/" + teamKey + "/event/" + eventKey + "/matches",
                HttpMethod.GET,
                entity,
                TBAMatchDto[].class
            );
            
            logger.info("Successfully retrieved {} matches for team {} at event {} from TBA", 
                       response.getBody().length, teamKey, eventKey);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving matches for team {} at event {} from TBA: {} - {}", 
                        teamKey, eventKey, e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Error retrieving matches for team {} at event {} from TBA: {}", 
                        teamKey, eventKey, e.getMessage());
            return List.of();
        }
    }
    
    public List<TBAMatchDto> getCurrentTeamMatches(String eventKey) {
        if (teamNumber == null || teamNumber.isEmpty()) {
            logger.warn("No team number configured, cannot retrieve team matches from TBA");
            return List.of();
        }
        return getTeamMatches("frc" + teamNumber, eventKey);
    }
    
    public Optional<TBAEventDto> getEvent(String eventKey) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TBAEventDto> response = restTemplate.exchange(
                baseUrl + "/event/" + eventKey,
                HttpMethod.GET,
                entity,
                TBAEventDto.class
            );
            
            logger.info("Successfully retrieved event {} from TBA", eventKey);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Event {} not found in TBA", eventKey);
            } else {
                logger.error("HTTP error retrieving event {} from TBA: {} - {}", 
                           eventKey, e.getStatusCode(), e.getMessage());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving event {} from TBA: {}", eventKey, e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<TBAEventDto> getEventsByYear(int year) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TBAEventDto[]> response = restTemplate.exchange(
                baseUrl + "/events/" + year,
                HttpMethod.GET,
                entity,
                TBAEventDto[].class
            );
            
            logger.info("Successfully retrieved {} events for year {} from TBA", 
                       response.getBody().length, year);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving events for year {} from TBA: {} - {}", 
                        year, e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Error retrieving events for year {} from TBA: {}", year, e.getMessage());
            return List.of();
        }
    }
    
    public List<TBAMatchDto> getEventMatches(String eventKey) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TBAMatchDto[]> response = restTemplate.exchange(
                baseUrl + "/event/" + eventKey + "/matches",
                HttpMethod.GET,
                entity,
                TBAMatchDto[].class
            );
            
            logger.info("Successfully retrieved {} matches for event {} from TBA", 
                       response.getBody().length, eventKey);
            return List.of(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error retrieving matches for event {} from TBA: {} - {}", 
                        eventKey, e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            logger.error("Error retrieving matches for event {} from TBA: {}", eventKey, e.getMessage());
            return List.of();
        }
    }
    
    // Team statistics and rankings
    public Optional<Object> getTeamEventStatus(String teamKey, String eventKey) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl + "/team/" + teamKey + "/event/" + eventKey + "/status",
                HttpMethod.GET,
                entity,
                Object.class
            );
            
            logger.info("Successfully retrieved status for team {} at event {} from TBA", teamKey, eventKey);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Status for team {} at event {} not found in TBA", teamKey, eventKey);
            } else {
                logger.error("HTTP error retrieving status for team {} at event {} from TBA: {} - {}", 
                           teamKey, eventKey, e.getStatusCode(), e.getMessage());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving status for team {} at event {} from TBA: {}", 
                        teamKey, eventKey, e.getMessage());
            return Optional.empty();
        }
    }
    
    public Optional<Object> getCurrentTeamEventStatus(String eventKey) {
        if (teamNumber == null || teamNumber.isEmpty()) {
            logger.warn("No team number configured, cannot retrieve team event status from TBA");
            return Optional.empty();
        }
        return getTeamEventStatus("frc" + teamNumber, eventKey);
    }
    
    // Configuration and utility methods
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    public boolean hasTeamNumber() {
        return teamNumber != null && !teamNumber.isEmpty();
    }
    
    public String getConfiguredTeamNumber() {
        return teamNumber;
    }
    
    public String getTeamKey() {
        return teamNumber != null ? "frc" + teamNumber : null;
    }
    
    // Rate limiting helper
    public boolean isRateLimited(HttpClientErrorException e) {
        return e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-TBA-Auth-Key", apiKey);
        }
        
        return headers;
    }
}