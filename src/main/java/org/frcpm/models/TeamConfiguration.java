// src/main/java/org/frcpm/models/TeamConfiguration.java
// Team Configuration Management

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents team configuration settings for customization.
 * Allows teams to configure team details and external integrations.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-Config
 * @since Configuration Management
 */
@Entity
@Table(name = "team_configuration", indexes = {
    @Index(name = "idx_config_category", columnList = "category"),
    @Index(name = "idx_config_key", columnList = "configKey"),
    @Index(name = "idx_config_active", columnList = "isActive")
})
public class TeamConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Configuration category for grouping related settings.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Configuration category is required")
    private ConfigurationCategory category;
    
    /**
     * Unique configuration key within the category.
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Configuration key is required")
    @Size(max = 100, message = "Configuration key must not exceed 100 characters")
    private String configKey;
    
    /**
     * Configuration value (stored as string, parsed as needed).
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Configuration value must not exceed 2000 characters")
    private String configValue;
    
    /**
     * Display name for the configuration setting.
     */
    @Column(length = 200)
    @Size(max = 200, message = "Display name must not exceed 200 characters")
    private String displayName;
    
    /**
     * Description of what this configuration controls.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    /**
     * Data type of the configuration value.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Data type is required")
    private ConfigurationDataType dataType = ConfigurationDataType.STRING;
    
    /**
     * Whether this configuration is required.
     */
    @Column(nullable = false)
    @NotNull(message = "Required status is required")
    private Boolean isRequired = false;
    
    /**
     * Whether this configuration is currently active.
     */
    @Column(nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
    
    /**
     * Whether this configuration is encrypted in storage.
     */
    @Column(nullable = false)
    @NotNull(message = "Encrypted status is required")
    private Boolean isEncrypted = false;
    
    /**
     * Default value for this configuration.
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "Default value must not exceed 2000 characters")
    private String defaultValue;
    
    /**
     * Validation pattern for the configuration value.
     */
    @Column(length = 500)
    @Size(max = 500, message = "Validation pattern must not exceed 500 characters")
    private String validationPattern;
    
    /**
     * Timestamp when this configuration was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this configuration was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * User who created this configuration.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    /**
     * User who last updated this configuration.
     */
    @Column(length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;
    
    /**
     * Enum defining configuration categories.
     */
    public enum ConfigurationCategory {
        TEAM_INFO("Team Information", "Basic team details and contact information"),
        FRC_API("FRC API", "FRC Official API integration settings"),
        TBA_API("The Blue Alliance API", "The Blue Alliance API integration settings"),
        DATABASE("Database", "Database connection and configuration settings"),
        EXTERNAL_APIS("External APIs", "Third-party API integrations"),
        DISCORD("Discord", "Discord bot and notification settings"),
        GOOGLE_WORKSPACE("Google Workspace", "Google Calendar and Drive integration"),
        GITHUB("GitHub", "GitHub repository integration"),
        NOTIFICATIONS("Notifications", "Notification preferences and settings"),
        SECURITY("Security", "Security and authentication settings"),
        UI_PREFERENCES("UI Preferences", "User interface customization"),
        COMPETITION("Competition", "Competition and event settings"),
        MANUFACTURING("Manufacturing", "Manufacturing and workshop settings"),
        INVENTORY("Inventory", "Parts and inventory management settings"),
        REPORTING("Reporting", "Reporting and analytics settings");
        
        private final String displayName;
        private final String description;
        
        ConfigurationCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Enum defining configuration data types.
     */
    public enum ConfigurationDataType {
        STRING("String", "Text value"),
        INTEGER("Integer", "Whole number"),
        DOUBLE("Double", "Decimal number"),
        BOOLEAN("Boolean", "True/false value"),
        URL("URL", "Web address"),
        EMAIL("Email", "Email address"),
        PASSWORD("Password", "Encrypted password"),
        JSON("JSON", "JSON object or array"),
        LIST("List", "Comma-separated list of values"),
        DATE("Date", "Date value"),
        DATETIME("DateTime", "Date and time value"),
        COLOR("Color", "Color hex code"),
        FILE_PATH("File Path", "File system path");
        
        private final String displayName;
        private final String description;
        
        ConfigurationDataType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Constructors
    public TeamConfiguration() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TeamConfiguration(ConfigurationCategory category, String configKey, String configValue) {
        this();
        this.category = category;
        this.configKey = configKey;
        this.configValue = configValue;
    }
    
    public TeamConfiguration(ConfigurationCategory category, String configKey, String configValue, 
                           ConfigurationDataType dataType, String displayName, String description) {
        this(category, configKey, configValue);
        this.dataType = dataType;
        this.displayName = displayName;
        this.description = description;
    }
    
    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business Logic Methods
    
    /**
     * Gets the configuration value as a string.
     */
    public String getStringValue() {
        return configValue;
    }
    
    /**
     * Gets the configuration value as an integer.
     */
    public Integer getIntegerValue() {
        if (configValue == null || configValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(configValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Gets the configuration value as a double.
     */
    public Double getDoubleValue() {
        if (configValue == null || configValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(configValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Gets the configuration value as a boolean.
     */
    public Boolean getBooleanValue() {
        if (configValue == null || configValue.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(configValue.trim());
    }
    
    /**
     * Sets the configuration value from an object.
     */
    public void setValue(Object value) {
        if (value == null) {
            this.configValue = null;
        } else {
            this.configValue = value.toString();
        }
    }
    
    /**
     * Checks if this configuration has a valid value.
     */
    public boolean hasValidValue() {
        if (isRequired && (configValue == null || configValue.trim().isEmpty())) {
            return false;
        }
        
        if (validationPattern != null && configValue != null) {
            return configValue.matches(validationPattern);
        }
        
        return true;
    }
    
    /**
     * Gets the effective value (configured value or default).
     */
    public String getEffectiveValue() {
        if (configValue != null && !configValue.trim().isEmpty()) {
            return configValue;
        }
        return defaultValue;
    }
    
    /**
     * Checks if this configuration is properly set up.
     */
    public boolean isComplete() {
        return displayName != null && 
               description != null && 
               dataType != null && 
               hasValidValue();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ConfigurationCategory getCategory() { return category; }
    public void setCategory(ConfigurationCategory category) { this.category = category; }
    
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ConfigurationDataType getDataType() { return dataType; }
    public void setDataType(ConfigurationDataType dataType) { this.dataType = dataType; }
    
    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsEncrypted() { return isEncrypted; }
    public void setIsEncrypted(Boolean isEncrypted) { this.isEncrypted = isEncrypted; }
    
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    
    public String getValidationPattern() { return validationPattern; }
    public void setValidationPattern(String validationPattern) { this.validationPattern = validationPattern; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamConfiguration that = (TeamConfiguration) o;
        return Objects.equals(id, that.id) ||
               (Objects.equals(category, that.category) && Objects.equals(configKey, that.configKey));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(category, configKey);
    }
    
    @Override
    public String toString() {
        return String.format("TeamConfiguration{id=%d, category=%s, key='%s', value='%s'}",
                id, category, configKey, 
                isEncrypted && configValue != null ? "[ENCRYPTED]" : configValue);
    }
}