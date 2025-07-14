// src/main/java/org/frcpm/integration/frc/FrcApiService.java

package org.frcpm.integration.frc;

import org.frcpm.models.FrcEvent;
import org.frcpm.models.FrcMatch;
import org.frcpm.models.FrcTeamRanking;
import org.frcpm.repositories.spring.FrcEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * FRC API Service for integrating with FIRST Robotics Competition official APIs.
 * 
 * This service provides real-time access to FRC event data, team rankings,
 * match schedules, and competition information. It handles authentication,
 * rate limiting, caching, and error recovery.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 3A - FRC API Integration
 */

 @ConditionalOnProperty(
    name = "app.frc.api.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
@Service
@Transactional
public class FrcApiService {
    
    private static final Logger LOGGER = Logger.getLogger(FrcApiService.class.getName());
    
    // Configuration from application.yml
    @Value("${app.frc.api.base-url:https://frc-api.firstinspires.org/v3.0}")
    private String apiBaseUrl;
    
    @Value("${app.frc.api.username:}")
    private String apiUsername;
    
    @Value("${app.frc.api.auth-key:}")
    private String apiAuthKey;
    
    @Value("${app.team.default-number:0}")
    private Integer defaultTeamNumber;
    
    @Value("${app.frc.season.current-year:2025}")
    private Integer currentSeasonYear;
    
    @Value("${app.frc.api.rate-limit.requests-per-minute:20}")
    private Integer rateLimitPerMinute;
    
    @Value("${app.frc.api.sync.enabled:true}")
    private boolean syncEnabled;
    
    private final RestTemplate restTemplate;
    private final FrcEventRepository frcEventRepository;
    private final ObjectMapper objectMapper;
    
    // Rate limiting
    private final Map<String, Long> lastRequestTimes = new HashMap<>();
    private final long minRequestInterval; // milliseconds between requests
    
    public FrcApiService(FrcEventRepository frcEventRepository) {
        this.frcEventRepository = frcEventRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.minRequestInterval = 60000 / (rateLimitPerMinute > 0 ? rateLimitPerMinute : 20); // 20 req/min default
    }
    
    /**
     * Initialize API connection and validate credentials.
     */
    public boolean validateApiConnection() {
        if (!isConfigured()) {
            LOGGER.warning("FRC API not configured - missing username or auth key");
            return false;
        }
        
        try {
            // Test API connection with a simple call
            String url = apiBaseUrl + "/" + currentSeasonYear + "/events";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            boolean isValid = response.getStatusCode() == HttpStatus.OK;
            LOGGER.info("FRC API connection validation: " + (isValid ? "SUCCESS" : "FAILED"));
            return isValid;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "FRC API connection validation failed", e);
            return false;
        }
    }
    
    /**
     * Get events for the current season.
     */
    @Cacheable(value = "frcEvents", key = "#seasonYear")
    public List<FrcEvent> getEvents(Integer seasonYear) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }
        
        try {
            enforceRateLimit("events");
            
            String url = apiBaseUrl + "/" + seasonYear + "/events";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FrcEventResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, FrcEventResponse.class);
            
            if (response.getBody() != null && response.getBody().events != null) {
                List<FrcEvent> events = new ArrayList<>();
                for (FrcEventDto dto : response.getBody().events) {
                    events.add(mapEventFromDto(dto, seasonYear));
                }
                
                LOGGER.info(String.format("Retrieved %d events for season %d", events.size(), seasonYear));
                return events;
            }
            
        } catch (RestClientException e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve events from FRC API", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving events", e);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Get team events for a specific team and season.
     */
    public List<FrcEvent> getTeamEvents(Integer teamNumber, Integer seasonYear) {
        if (!isConfigured() || teamNumber == null) {
            return Collections.emptyList();
        }
        
        try {
            enforceRateLimit("team-events-" + teamNumber);
            
            String url = apiBaseUrl + "/" + seasonYear + "/teams/" + teamNumber + "/events";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FrcEventResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, FrcEventResponse.class);
            
            if (response.getBody() != null && response.getBody().events != null) {
                List<FrcEvent> events = new ArrayList<>();
                for (FrcEventDto dto : response.getBody().events) {
                    events.add(mapEventFromDto(dto, seasonYear));
                }
                
                LOGGER.info(String.format("Retrieved %d events for team %d in season %d", 
                                        events.size(), teamNumber, seasonYear));
                return events;
            }
            
        } catch (RestClientException e) {
            LOGGER.log(Level.WARNING, String.format("Failed to retrieve events for team %d", teamNumber), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving team events", e);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Get rankings for a specific event.
     */
    public List<FrcTeamRanking> getEventRankings(String eventCode, Integer seasonYear) {
        if (!isConfigured() || eventCode == null) {
            return Collections.emptyList();
        }
        
        try {
            enforceRateLimit("rankings-" + eventCode);
            
            String url = apiBaseUrl + "/" + seasonYear + "/rankings/" + eventCode;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FrcRankingResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, FrcRankingResponse.class);
            
            if (response.getBody() != null && response.getBody().rankings != null) {
                List<FrcTeamRanking> rankings = new ArrayList<>();
                for (FrcRankingDto dto : response.getBody().rankings) {
                    rankings.add(mapRankingFromDto(dto, eventCode, seasonYear));
                }
                
                LOGGER.info(String.format("Retrieved %d rankings for event %s", rankings.size(), eventCode));
                return rankings;
            }
            
        } catch (RestClientException e) {
            LOGGER.log(Level.WARNING, String.format("Failed to retrieve rankings for event %s", eventCode), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving rankings", e);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Sync all FRC data for the team.
     * This is the main method called by scheduled tasks.
     */
    @Async
    @Scheduled(fixedRateString = "${app.frc.api.sync.auto-sync-interval:3600}000") // Convert to milliseconds
    public CompletableFuture<Void> syncAllData() {
        if (!syncEnabled || !isConfigured()) {
            LOGGER.info("FRC API sync disabled or not configured");
            return CompletableFuture.completedFuture(null);
        }
        
        LOGGER.info("Starting FRC API data synchronization");
        
        try {
            // Sync team events
            if (defaultTeamNumber > 0) {
                List<FrcEvent> teamEvents = getTeamEvents(defaultTeamNumber, currentSeasonYear);
                syncEventsToDatabase(teamEvents);
            }
            
            // Sync current season events
            List<FrcEvent> allEvents = getEvents(currentSeasonYear);
            syncEventsToDatabase(allEvents);
            
            LOGGER.info("FRC API data synchronization completed successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "FRC API synchronization failed", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Sync events to the database.
     */
    private void syncEventsToDatabase(List<FrcEvent> events) {
        for (FrcEvent event : events) {
            try {
                // Check if event already exists
                Optional<FrcEvent> existing = frcEventRepository.findByEventCodeAndSeasonYear(
                    event.getEventCode(), event.getSeasonYear());
                
                if (existing.isPresent()) {
                    // Update existing event
                    FrcEvent existingEvent = existing.get();
                    updateEventFromSync(existingEvent, event);
                    frcEventRepository.save(existingEvent);
                } else {
                    // Create new event
                    event.setLastSynced(LocalDateTime.now());
                    frcEventRepository.save(event);
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, String.format("Failed to sync event %s", event.getEventCode()), e);
            }
        }
    }
    
    /**
     * Update existing event with new data from sync.
     */
    private void updateEventFromSync(FrcEvent existing, FrcEvent updated) {
        existing.setEventName(updated.getEventName());
        existing.setEventType(updated.getEventType());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setLocation(updated.getLocation());
        existing.setVenue(updated.getVenue());
        existing.setCity(updated.getCity());
        existing.setStateProvince(updated.getStateProvince());
        existing.setCountry(updated.getCountry());
        existing.setWebsite(updated.getWebsite());
        existing.setLiveStreamUrl(updated.getLiveStreamUrl());
        existing.setRegistrationOpen(updated.getRegistrationOpen());
        existing.setRegistrationClose(updated.getRegistrationClose());
        existing.setTeamCount(updated.getTeamCount());
        existing.setOfficial(updated.isOfficial());
        existing.setPublic(updated.isPublic());
        existing.setLastSynced(LocalDateTime.now());
    }
    
    /**
     * Create HTTP headers with authentication.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // Basic Authentication for FRC API
        String auth = apiUsername + ":" + apiAuthKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }
    
    /**
     * Enforce rate limiting to respect API limits.
     */
    private void enforceRateLimit(String endpoint) {
        long now = System.currentTimeMillis();
        Long lastRequest = lastRequestTimes.get(endpoint);
        
        if (lastRequest != null) {
            long timeSinceLastRequest = now - lastRequest;
            if (timeSinceLastRequest < minRequestInterval) {
                long sleepTime = minRequestInterval - timeSinceLastRequest;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Rate limiting interrupted", e);
                }
            }
        }
        
        lastRequestTimes.put(endpoint, System.currentTimeMillis());
    }
    
    /**
     * Check if API is properly configured.
     */
    private boolean isConfigured() {
        return apiUsername != null && !apiUsername.isEmpty() &&
               apiAuthKey != null && !apiAuthKey.isEmpty();
    }
    
    /**
     * Map FRC Event DTO to domain model.
     */
    private FrcEvent mapEventFromDto(FrcEventDto dto, Integer seasonYear) {
        FrcEvent event = new FrcEvent();
        event.setEventCode(dto.code);
        event.setEventName(dto.name);
        event.setEventType(FrcEvent.EventType.fromString(dto.type));
        event.setSeasonYear(seasonYear);
        
        // Parse dates
        if (dto.dateStart != null) {
            event.setStartDate(java.time.LocalDate.parse(dto.dateStart));
        }
        if (dto.dateEnd != null) {
            event.setEndDate(java.time.LocalDate.parse(dto.dateEnd));
        }
        
        // Location information
        event.setLocation(dto.address);
        event.setVenue(dto.venue);
        event.setCity(dto.city);
        event.setStateProvince(dto.stateProv);
        event.setCountry(dto.country);
        event.setWebsite(dto.website);
        
        // Registration dates
        if (dto.regOpen != null) {
            event.setRegistrationOpen(LocalDateTime.parse(dto.regOpen, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (dto.regClose != null) {
            event.setRegistrationClose(LocalDateTime.parse(dto.regClose, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        // Metadata
        event.setTeamCount(dto.teamCount);
        event.setLastSynced(LocalDateTime.now());
        
        return event;
    }
    
    /**
     * Map FRC Ranking DTO to domain model.
     */
    private FrcTeamRanking mapRankingFromDto(FrcRankingDto dto, String eventCode, Integer seasonYear) {
        FrcTeamRanking ranking = new FrcTeamRanking();
        ranking.setTeamNumber(dto.teamNumber);
        ranking.setRank(dto.rank);
        ranking.setWins(dto.wins);
        ranking.setLosses(dto.losses);
        ranking.setTies(dto.ties);
        
        if (dto.rankingPoints != null) {
            ranking.setRankingPoints(new java.math.BigDecimal(dto.rankingPoints));
        }
        
        return ranking;
    }
    
    // DTO Classes for JSON mapping
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FrcEventResponse {
        @JsonProperty("Events")
        public List<FrcEventDto> events;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FrcEventDto {
        @JsonProperty("code")
        public String code;
        
        @JsonProperty("name")
        public String name;
        
        @JsonProperty("type")
        public String type;
        
        @JsonProperty("dateStart")
        public String dateStart;
        
        @JsonProperty("dateEnd")
        public String dateEnd;
        
        @JsonProperty("address")
        public String address;
        
        @JsonProperty("venue")
        public String venue;
        
        @JsonProperty("city")
        public String city;
        
        @JsonProperty("stateProv")
        public String stateProv;
        
        @JsonProperty("country")
        public String country;
        
        @JsonProperty("website")
        public String website;
        
        @JsonProperty("regOpen")
        public String regOpen;
        
        @JsonProperty("regClose")
        public String regClose;
        
        @JsonProperty("teamCount")
        public Integer teamCount;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FrcRankingResponse {
        @JsonProperty("Rankings")
        public List<FrcRankingDto> rankings;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FrcRankingDto {
        @JsonProperty("teamNumber")
        public Integer teamNumber;
        
        @JsonProperty("rank")
        public Integer rank;
        
        @JsonProperty("wins")
        public Integer wins;
        
        @JsonProperty("losses")
        public Integer losses;
        
        @JsonProperty("ties")
        public Integer ties;
        
        @JsonProperty("rankingPoints")
        public Double rankingPoints;
    }
}