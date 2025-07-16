package org.frcpm.integration.frc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FRCTeamDto {
    
    @JsonProperty("teamNumber")
    private Integer teamNumber;
    
    @JsonProperty("nameShort")
    private String nameShort;
    
    @JsonProperty("nameFull")
    private String nameFull;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("stateProv")
    private String stateProv;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("rookieYear")
    private Integer rookieYear;
    
    @JsonProperty("schoolName")
    private String schoolName;
    
    @JsonProperty("districtCode")
    private String districtCode;
    
    @JsonProperty("districtName")
    private String districtName;
    
    @JsonProperty("homeCMP")
    private String homeCMP;
    
    @JsonProperty("postalCode")
    private String postalCode;
    
    @JsonProperty("gmapsPlaceId")
    private String gmapsPlaceId;
    
    @JsonProperty("gmapsUrl")
    private String gmapsUrl;
    
    // Constructors
    public FRCTeamDto() {}
    
    // Business methods
    public boolean isRookie() {
        return rookieYear != null && rookieYear == java.time.Year.now().getValue();
    }
    
    public boolean isDistrictTeam() {
        return districtCode != null && !districtCode.isEmpty();
    }
    
    public int getYearsActive() {
        if (rookieYear == null) return 0;
        return java.time.Year.now().getValue() - rookieYear + 1;
    }
    
    public String getDisplayName() {
        if (nameShort != null && !nameShort.isEmpty()) {
            return teamNumber + " - " + nameShort;
        }
        return teamNumber != null ? teamNumber.toString() : "Unknown Team";
    }
    
    public String getFullDisplayName() {
        if (nameFull != null && !nameFull.isEmpty()) {
            return teamNumber + " - " + nameFull;
        }
        return getDisplayName();
    }
    
    public String getLocationString() {
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
    
    public String getDistrictDisplayName() {
        if (districtName != null && !districtName.isEmpty()) {
            return districtName;
        }
        if (districtCode != null && !districtCode.isEmpty()) {
            return districtCode;
        }
        return "No District";
    }
    
    // Getters and setters
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public String getNameShort() { return nameShort; }
    public void setNameShort(String nameShort) { this.nameShort = nameShort; }
    
    public String getNameFull() { return nameFull; }
    public void setNameFull(String nameFull) { this.nameFull = nameFull; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getStateProv() { return stateProv; }
    public void setStateProv(String stateProv) { this.stateProv = stateProv; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public Integer getRookieYear() { return rookieYear; }
    public void setRookieYear(Integer rookieYear) { this.rookieYear = rookieYear; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    
    public String getDistrictCode() { return districtCode; }
    public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
    
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    
    public String getHomeCMP() { return homeCMP; }
    public void setHomeCMP(String homeCMP) { this.homeCMP = homeCMP; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getGmapsPlaceId() { return gmapsPlaceId; }
    public void setGmapsPlaceId(String gmapsPlaceId) { this.gmapsPlaceId = gmapsPlaceId; }
    
    public String getGmapsUrl() { return gmapsUrl; }
    public void setGmapsUrl(String gmapsUrl) { this.gmapsUrl = gmapsUrl; }
    
    @Override
    public String toString() {
        return String.format("FRCTeamDto{teamNumber=%d, nameShort='%s', city='%s', stateProv='%s', rookieYear=%d}",
                           teamNumber, nameShort, city, stateProv, rookieYear);
    }
}