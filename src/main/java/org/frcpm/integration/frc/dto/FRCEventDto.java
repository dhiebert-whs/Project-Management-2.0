package org.frcpm.integration.frc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class FRCEventDto {
    
    @JsonProperty("eventCode")
    private String eventCode;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("eventTypeString")
    private String type;
    
    @JsonProperty("dateStart")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonProperty("dateEnd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @JsonProperty("venue")
    private String venue;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("stateprov")
    private String stateProv;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("livestream")
    private String livestream;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("timezone")
    private String timezone;
    
    @JsonProperty("weekNumber")
    private Integer weekNumber;
    
    @JsonProperty("districtCode")
    private String districtCode;
    
    @JsonProperty("published")
    private Boolean published;
    
    // Constructors
    public FRCEventDto() {}
    
    // Business methods
    public boolean isPublished() {
        return published != null && published;
    }
    
    public boolean isDistrictEvent() {
        return districtCode != null && !districtCode.isEmpty();
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        
        if (venue != null && !venue.isEmpty()) {
            address.append(venue);
        }
        
        if (city != null && !city.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        
        if (stateProv != null && !stateProv.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(stateProv);
        }
        
        if (country != null && !country.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(country);
        }
        
        return address.toString();
    }
    
    public String getDisplayName() {
        return name + " (" + eventCode + ")";
    }
    
    // Getters and setters
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getStateProv() { return stateProv; }
    public void setStateProv(String stateProv) { this.stateProv = stateProv; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getLivestream() { return livestream; }
    public void setLivestream(String livestream) { this.livestream = livestream; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    
    public String getDistrictCode() { return districtCode; }
    public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
    
    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
    
    @Override
    public String toString() {
        return String.format("FRCEventDto{eventCode='%s', name='%s', type='%s', startDate=%s, endDate=%s}",
                           eventCode, name, type, startDate, endDate);
    }
}