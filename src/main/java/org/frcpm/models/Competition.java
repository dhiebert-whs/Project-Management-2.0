package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "competitions")
@EntityListeners(AuditingEntityListener.class)
public class Competition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String eventCode; // FRC event code (e.g., "2024TXHOU")
    
    @Enumerated(EnumType.STRING)
    private CompetitionType type;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    private LocalTime loadInTime;
    private LocalTime inspectionDeadline;
    
    private String venue;
    private String address;
    private String city;
    private String state;
    private String country;
    
    private String websiteUrl;
    private String livestreamUrl;
    
    @ManyToOne
    @JoinColumn(name = "season_id")
    private CompetitionSeason season;
    
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL)
    private List<CompetitionResult> results;
    
    private boolean isAttending = false;
    private String notes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Competition() {}
    
    public Competition(String name, String eventCode, CompetitionType type, 
                      LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.eventCode = eventCode;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Business methods
    public boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }
    
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
    
    public boolean isPast() {
        return LocalDate.now().isAfter(endDate);
    }
    
    public long getDaysUntilEvent() {
        return LocalDate.now().until(startDate).getDays();
    }
    
    public long getDuration() {
        return startDate.until(endDate).getDays() + 1; // Include both start and end dates
    }
    
    public boolean isMultiDay() {
        return !startDate.equals(endDate);
    }
    
    public boolean requiresTravel() {
        return venue != null && !venue.isEmpty();
    }
    
    public String getDisplayName() {
        return name + " (" + eventCode + ")";
    }
    
    public String getLocationString() {
        StringBuilder location = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            location.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        if (country != null && !country.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(country);
        }
        return location.toString();
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    
    public CompetitionType getType() { return type; }
    public void setType(CompetitionType type) { this.type = type; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public LocalTime getLoadInTime() { return loadInTime; }
    public void setLoadInTime(LocalTime loadInTime) { this.loadInTime = loadInTime; }
    
    public LocalTime getInspectionDeadline() { return inspectionDeadline; }
    public void setInspectionDeadline(LocalTime inspectionDeadline) { this.inspectionDeadline = inspectionDeadline; }
    
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    
    public String getLivestreamUrl() { return livestreamUrl; }
    public void setLivestreamUrl(String livestreamUrl) { this.livestreamUrl = livestreamUrl; }
    
    public CompetitionSeason getSeason() { return season; }
    public void setSeason(CompetitionSeason season) { this.season = season; }
    
    public List<CompetitionResult> getResults() { return results; }
    public void setResults(List<CompetitionResult> results) { this.results = results; }
    
    public boolean isAttending() { return isAttending; }
    public void setAttending(boolean attending) { isAttending = attending; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}