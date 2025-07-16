package org.frcpm.integration.tba.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Map;

public class TBAEventDto {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("event_code")
    private String eventCode;
    
    @JsonProperty("event_type")
    private Integer eventType;
    
    @JsonProperty("event_type_string")
    private String eventTypeString;
    
    @JsonProperty("year")
    private Integer year;
    
    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("state_prov")
    private String stateProv;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("postal_code")
    private String postalCode;
    
    @JsonProperty("timezone")
    private String timezone;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("first_event_id")
    private String firstEventId;
    
    @JsonProperty("first_event_code")
    private String firstEventCode;
    
    @JsonProperty("webcasts")
    private Object[] webcasts;
    
    @JsonProperty("division_keys")
    private String[] divisionKeys;
    
    @JsonProperty("parent_event_key")
    private String parentEventKey;
    
    @JsonProperty("playoff_type")
    private Integer playoffType;
    
    @JsonProperty("playoff_type_string")
    private String playoffTypeString;
    
    @JsonProperty("district")
    private Map<String, Object> district;
    
    @JsonProperty("short_name")
    private String shortName;
    
    @JsonProperty("week")
    private Integer week;
    
    // Constructors
    public TBAEventDto() {}
    
    // Business methods
    public boolean isDistrictEvent() {
        return district != null && !district.isEmpty();
    }
    
    public boolean isRegionalEvent() {
        return eventType != null && eventType == 1;
    }
    
    public boolean isDistrictChampionship() {
        return eventType != null && eventType == 2;
    }
    
    public boolean isChampionshipEvent() {
        return eventType != null && eventType == 3;
    }
    
    public boolean isOffseasonEvent() {
        return eventType != null && eventType == 99;
    }
    
    public boolean hasWebcast() {
        return webcasts != null && webcasts.length > 0;
    }
    
    public boolean isMultiDivision() {
        return divisionKeys != null && divisionKeys.length > 0;
    }
    
    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        
        if (city != null && !city.isEmpty()) {
            location.append(city);
        }
        
        if (stateProv != null && !stateProv.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(stateProv);
        }
        
        if (country != null && !country.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(country);
        }
        
        return location.toString();
    }
    
    public String getDisplayName() {
        return name + " (" + eventCode + ")";
    }
    
    public String getDistrictName() {
        if (district != null && district.containsKey("display_name")) {
            return (String) district.get("display_name");
        }
        return null;
    }
    
    public String getDistrictKey() {
        if (district != null && district.containsKey("key")) {
            return (String) district.get("key");
        }
        return null;
    }
    
    public boolean isCurrentWeek() {
        if (week == null) return false;
        // This would need to be calculated based on current date and FRC season start
        return false; // Placeholder
    }
    
    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    
    public Integer getEventType() { return eventType; }
    public void setEventType(Integer eventType) { this.eventType = eventType; }
    
    public String getEventTypeString() { return eventTypeString; }
    public void setEventTypeString(String eventTypeString) { this.eventTypeString = eventTypeString; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getStateProv() { return stateProv; }
    public void setStateProv(String stateProv) { this.stateProv = stateProv; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getFirstEventId() { return firstEventId; }
    public void setFirstEventId(String firstEventId) { this.firstEventId = firstEventId; }
    
    public String getFirstEventCode() { return firstEventCode; }
    public void setFirstEventCode(String firstEventCode) { this.firstEventCode = firstEventCode; }
    
    public Object[] getWebcasts() { return webcasts; }
    public void setWebcasts(Object[] webcasts) { this.webcasts = webcasts; }
    
    public String[] getDivisionKeys() { return divisionKeys; }
    public void setDivisionKeys(String[] divisionKeys) { this.divisionKeys = divisionKeys; }
    
    public String getParentEventKey() { return parentEventKey; }
    public void setParentEventKey(String parentEventKey) { this.parentEventKey = parentEventKey; }
    
    public Integer getPlayoffType() { return playoffType; }
    public void setPlayoffType(Integer playoffType) { this.playoffType = playoffType; }
    
    public String getPlayoffTypeString() { return playoffTypeString; }
    public void setPlayoffTypeString(String playoffTypeString) { this.playoffTypeString = playoffTypeString; }
    
    public Map<String, Object> getDistrict() { return district; }
    public void setDistrict(Map<String, Object> district) { this.district = district; }
    
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    
    public Integer getWeek() { return week; }
    public void setWeek(Integer week) { this.week = week; }
    
    @Override
    public String toString() {
        return String.format("TBAEventDto{key='%s', name='%s', eventType=%d, startDate=%s, endDate=%s}",
                           key, name, eventType, startDate, endDate);
    }
}