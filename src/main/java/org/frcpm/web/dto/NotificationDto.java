// src/main/java/org/frcpm/web/dto/NotificationDto.java
// Phase 2E-E: Enhanced Real-time Features - Notification DTO

package org.frcpm.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for notifications.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-2E-E
 * @since Phase 2E-E - Enhanced Real-time Features
 */
public class NotificationDto {
    
    private Long id;
    private String title;
    private String message;
    private String type;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private boolean read;
    private Map<String, Object> data;
    
    // Constructors
    public NotificationDto() {}
    
    public NotificationDto(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @JsonProperty("isRead")
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}