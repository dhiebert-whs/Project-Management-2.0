package org.frcpm.integration.tba.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TBATeamDto {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("team_number")
    private Integer teamNumber;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("school_name")
    private String schoolName;
    
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
    
    @JsonProperty("gmaps_place_id")
    private String gmapsPlaceId;
    
    @JsonProperty("gmaps_url")
    private String gmapsUrl;
    
    @JsonProperty("lat")
    private Double latitude;
    
    @JsonProperty("lng")
    private Double longitude;
    
    @JsonProperty("location_name")
    private String locationName;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("rookie_year")
    private Integer rookieYear;
    
    @JsonProperty("motto")
    private String motto;
    
    @JsonProperty("home_championship")
    private Object homeChampionship;
    
    // Constructors
    public TBATeamDto() {}
    
    // Business methods
    public boolean isRookie() {
        return rookieYear != null && rookieYear == java.time.Year.now().getValue();
    }
    
    public int getYearsActive() {
        if (rookieYear == null) return 0;
        return java.time.Year.now().getValue() - rookieYear + 1;
    }
    
    public String getDisplayName() {
        if (nickname != null && !nickname.isEmpty()) {
            return teamNumber + " - " + nickname;
        }
        return teamNumber != null ? teamNumber.toString() : "Unknown Team";
    }
    
    public String getFullDisplayName() {
        if (name != null && !name.isEmpty()) {
            return teamNumber + " - " + name;
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
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        
        if (this.address != null && !this.address.isEmpty()) {
            address.append(this.address);
        }
        
        if (city != null && !city.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        
        if (stateProv != null && !stateProv.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(stateProv);
        }
        
        if (postalCode != null && !postalCode.isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }
        
        if (country != null && !country.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(country);
        }
        
        return address.toString();
    }
    
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
    
    public boolean hasWebsite() {
        return website != null && !website.isEmpty();
    }
    
    public String getTeamKey() {
        return key;
    }
    
    public boolean isUS() {
        return "USA".equals(country);
    }
    
    public boolean isInternational() {
        return country != null && !"USA".equals(country);
    }
    
    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    
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
    
    public String getGmapsPlaceId() { return gmapsPlaceId; }
    public void setGmapsPlaceId(String gmapsPlaceId) { this.gmapsPlaceId = gmapsPlaceId; }
    
    public String getGmapsUrl() { return gmapsUrl; }
    public void setGmapsUrl(String gmapsUrl) { this.gmapsUrl = gmapsUrl; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public Integer getRookieYear() { return rookieYear; }
    public void setRookieYear(Integer rookieYear) { this.rookieYear = rookieYear; }
    
    public String getMotto() { return motto; }
    public void setMotto(String motto) { this.motto = motto; }
    
    public Object getHomeChampionship() { return homeChampionship; }
    public void setHomeChampionship(Object homeChampionship) { this.homeChampionship = homeChampionship; }
    
    @Override
    public String toString() {
        return String.format("TBATeamDto{key='%s', teamNumber=%d, nickname='%s', city='%s', rookieYear=%d}",
                           key, teamNumber, nickname, city, rookieYear);
    }
}