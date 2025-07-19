// src/main/java/org/frcpm/services/TeamConfigurationService.java
// Team Configuration Management

package org.frcpm.services;

import org.frcpm.models.TeamConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for TeamConfiguration management.
 * Provides team configuration and customization capabilities.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-Config
 * @since Configuration Management
 */
public interface TeamConfigurationService extends Service<TeamConfiguration, Long> {
    
    // =========================================================================
    // BASIC CONFIGURATION OPERATIONS
    // =========================================================================
    
    /**
     * Creates a new configuration setting.
     */
    TeamConfiguration createConfiguration(TeamConfiguration.ConfigurationCategory category,
                                        String configKey, String configValue,
                                        TeamConfiguration.ConfigurationDataType dataType,
                                        String displayName, String description);
    
    /**
     * Creates a simple configuration setting.
     */
    TeamConfiguration createConfiguration(TeamConfiguration.ConfigurationCategory category,
                                        String configKey, String configValue);
    
    /**
     * Updates a configuration value.
     */
    TeamConfiguration updateConfigurationValue(TeamConfiguration.ConfigurationCategory category,
                                             String configKey, String newValue);
    
    /**
     * Updates a configuration by ID.
     */
    TeamConfiguration updateConfiguration(Long configId, String configValue, String displayName, String description);
    
    /**
     * Deletes a configuration.
     */
    boolean deleteConfiguration(TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Activates a configuration.
     */
    TeamConfiguration activateConfiguration(Long configId);
    
    /**
     * Deactivates a configuration.
     */
    TeamConfiguration deactivateConfiguration(Long configId);
    
    // =========================================================================
    // CONFIGURATION RETRIEVAL
    // =========================================================================
    
    /**
     * Gets a configuration value by category and key.
     */
    Optional<TeamConfiguration> getConfiguration(TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Gets a configuration value as string.
     */
    String getConfigValue(TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Gets a configuration value as string with default.
     */
    String getConfigValue(TeamConfiguration.ConfigurationCategory category, String configKey, String defaultValue);
    
    /**
     * Gets a configuration value as integer.
     */
    Integer getIntegerValue(TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Gets a configuration value as integer with default.
     */
    Integer getIntegerValue(TeamConfiguration.ConfigurationCategory category, String configKey, Integer defaultValue);
    
    /**
     * Gets a configuration value as double.
     */
    Double getDoubleValue(TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Gets a configuration value as double with default.
     */
    Double getDoubleValue(TeamConfiguration.ConfigurationCategory category, String configKey, Double defaultValue);
    
    /**
     * Gets a configuration value as boolean.
     */
    Boolean getBooleanValue(TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Gets a configuration value as boolean with default.
     */
    Boolean getBooleanValue(TeamConfiguration.ConfigurationCategory category, String configKey, Boolean defaultValue);
    
    // =========================================================================
    // CATEGORY-BASED OPERATIONS
    // =========================================================================
    
    /**
     * Gets all configurations for a category.
     */
    List<TeamConfiguration> getConfigurationsByCategory(TeamConfiguration.ConfigurationCategory category);
    
    /**
     * Gets configurations for multiple categories.
     */
    List<TeamConfiguration> getConfigurationsByCategories(List<TeamConfiguration.ConfigurationCategory> categories);
    
    /**
     * Gets configuration map for a category (key -> value).
     */
    Map<String, String> getConfigurationMap(TeamConfiguration.ConfigurationCategory category);
    
    /**
     * Gets typed configuration map for a category.
     */
    Map<String, Object> getTypedConfigurationMap(TeamConfiguration.ConfigurationCategory category);
    
    /**
     * Updates multiple configurations for a category.
     */
    List<TeamConfiguration> updateCategoryConfigurations(TeamConfiguration.ConfigurationCategory category,
                                                        Map<String, String> configUpdates);
    
    /**
     * Resets category configurations to defaults.
     */
    int resetCategoryToDefaults(TeamConfiguration.ConfigurationCategory category);
    
    // =========================================================================
    // TEAM INFORMATION MANAGEMENT
    // =========================================================================
    
    /**
     * Sets team number.
     */
    TeamConfiguration setTeamNumber(Integer teamNumber);
    
    /**
     * Gets team number.
     */
    Integer getTeamNumber();
    
    /**
     * Sets team name.
     */
    TeamConfiguration setTeamName(String teamName);
    
    /**
     * Gets team name.
     */
    String getTeamName();
    
    /**
     * Sets team contact information.
     */
    TeamConfiguration setTeamContact(String contactType, String contactValue);
    
    /**
     * Gets team contact information.
     */
    Map<String, String> getTeamContacts();
    
    /**
     * Sets team location.
     */
    TeamConfiguration setTeamLocation(String city, String state, String country);
    
    /**
     * Gets team location.
     */
    Map<String, String> getTeamLocation();
    
    // =========================================================================
    // API INTEGRATION MANAGEMENT
    // =========================================================================
    
    /**
     * Configures FRC API settings.
     */
    Map<String, TeamConfiguration> configureFrcApi(String username, String authKey);
    
    /**
     * Gets FRC API configuration.
     */
    Map<String, String> getFrcApiConfiguration();
    
    /**
     * Tests FRC API connection.
     */
    boolean testFrcApiConnection();
    
    /**
     * Configures The Blue Alliance API.
     */
    TeamConfiguration configureTbaApi(String authKey);
    
    /**
     * Gets TBA API configuration.
     */
    Map<String, String> getTbaApiConfiguration();
    
    /**
     * Tests TBA API connection.
     */
    boolean testTbaApiConnection();
    
    /**
     * Configures Discord integration.
     */
    Map<String, TeamConfiguration> configureDiscord(String botToken, String guildId, String channelId);
    
    /**
     * Gets Discord configuration.
     */
    Map<String, String> getDiscordConfiguration();
    
    /**
     * Tests Discord connection.
     */
    boolean testDiscordConnection();
    
    /**
     * Configures Google Workspace integration.
     */
    Map<String, TeamConfiguration> configureGoogleWorkspace(String clientId, String clientSecret, String calendarId);
    
    /**
     * Gets Google Workspace configuration.
     */
    Map<String, String> getGoogleWorkspaceConfiguration();
    
    /**
     * Tests Google Workspace connection.
     */
    boolean testGoogleWorkspaceConnection();
    
    /**
     * Configures GitHub integration.
     */
    Map<String, TeamConfiguration> configureGitHub(String accessToken, String organizationName, String repositoryName);
    
    /**
     * Gets GitHub configuration.
     */
    Map<String, String> getGitHubConfiguration();
    
    /**
     * Tests GitHub connection.
     */
    boolean testGitHubConnection();
    
    // =========================================================================
    // DATABASE CONFIGURATION
    // =========================================================================
    
    /**
     * Configures database settings.
     */
    Map<String, TeamConfiguration> configureDatabaseSettings(String databaseUrl, String username, String password);
    
    /**
     * Gets database configuration (passwords masked).
     */
    Map<String, String> getDatabaseConfiguration();
    
    /**
     * Tests database connection.
     */
    boolean testDatabaseConnection();
    
    /**
     * Updates database connection pool settings.
     */
    Map<String, TeamConfiguration> updateDatabasePoolSettings(Integer maxPoolSize, Integer minPoolSize, Integer connectionTimeout);
    
    // =========================================================================
    // VALIDATION AND SETUP
    // =========================================================================
    
    /**
     * Validates all configurations.
     */
    Map<String, List<String>> validateAllConfigurations();
    
    /**
     * Gets setup completion status.
     */
    Map<String, Object> getSetupCompletionStatus();
    
    /**
     * Gets required configurations that are not set.
     */
    List<TeamConfiguration> getRequiredConfigurationsNotSet();
    
    /**
     * Gets configuration readiness by category.
     */
    Map<TeamConfiguration.ConfigurationCategory, Map<String, Object>> getConfigurationReadiness();
    
    /**
     * Checks if team setup is complete.
     */
    boolean isTeamSetupComplete();
    
    /**
     * Checks if API integrations are ready.
     */
    Map<String, Boolean> getApiIntegrationStatus();
    
    /**
     * Gets system health status based on configurations.
     */
    Map<String, Object> getSystemHealthStatus();
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches configurations by term.
     */
    List<TeamConfiguration> searchConfigurations(String searchTerm);
    
    /**
     * Gets configurations by data type.
     */
    List<TeamConfiguration> getConfigurationsByDataType(TeamConfiguration.ConfigurationDataType dataType);
    
    /**
     * Gets required configurations.
     */
    List<TeamConfiguration> getRequiredConfigurations();
    
    /**
     * Gets encrypted configurations.
     */
    List<TeamConfiguration> getEncryptedConfigurations();
    
    /**
     * Gets configurations with validation patterns.
     */
    List<TeamConfiguration> getConfigurationsWithValidation();
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Creates multiple configurations.
     */
    List<TeamConfiguration> createBulkConfigurations(List<TeamConfiguration> configurations);
    
    /**
     * Updates multiple configurations.
     */
    List<TeamConfiguration> updateBulkConfigurations(Map<Long, String> configUpdates);
    
    /**
     * Imports configurations from map.
     */
    List<TeamConfiguration> importConfigurations(Map<String, Object> configurationData);
    
    /**
     * Exports configurations to map.
     */
    Map<String, Object> exportConfigurations(List<TeamConfiguration.ConfigurationCategory> categories);
    
    /**
     * Resets all configurations to defaults.
     */
    int resetAllConfigurationsToDefaults();
    
    /**
     * Activates multiple configurations.
     */
    int activateBulkConfigurations(List<Long> configIds);
    
    /**
     * Deactivates multiple configurations.
     */
    int deactivateBulkConfigurations(List<Long> configIds);
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    /**
     * Gets configuration statistics by category.
     */
    Map<TeamConfiguration.ConfigurationCategory, Map<String, Object>> getConfigurationStatistics();
    
    /**
     * Gets data type distribution.
     */
    Map<TeamConfiguration.ConfigurationDataType, Long> getDataTypeDistribution();
    
    /**
     * Gets configuration usage metrics.
     */
    Map<String, Object> getConfigurationUsageMetrics();
    
    /**
     * Gets security metrics (encrypted vs non-encrypted sensitive data).
     */
    Map<String, Object> getSecurityMetrics();
    
    /**
     * Gets configuration completeness metrics.
     */
    Map<String, Object> getCompletenessMetrics();
    
    // =========================================================================
    // INITIALIZATION AND DEFAULTS
    // =========================================================================
    
    /**
     * Initializes default configurations for a new team.
     */
    List<TeamConfiguration> initializeDefaultConfigurations();
    
    /**
     * Creates team information template configurations.
     */
    List<TeamConfiguration> createTeamInfoTemplate();
    
    /**
     * Creates API integration template configurations.
     */
    List<TeamConfiguration> createApiIntegrationTemplate();
    
    /**
     * Creates notification template configurations.
     */
    List<TeamConfiguration> createNotificationTemplate();
    
    /**
     * Creates security template configurations.
     */
    List<TeamConfiguration> createSecurityTemplate();
    
    /**
     * Creates UI preferences template configurations.
     */
    List<TeamConfiguration> createUiPreferencesTemplate();
    
    /**
     * Applies configuration templates.
     */
    List<TeamConfiguration> applyConfigurationTemplates(List<String> templateNames);
    
    // =========================================================================
    // ENCRYPTION AND SECURITY
    // =========================================================================
    
    /**
     * Encrypts sensitive configuration values.
     */
    TeamConfiguration encryptConfiguration(Long configId);
    
    /**
     * Decrypts configuration value (for authorized access).
     */
    String decryptConfigurationValue(Long configId);
    
    /**
     * Encrypts all password-type configurations.
     */
    int encryptAllPasswordConfigurations();
    
    /**
     * Updates encryption key (re-encrypts all encrypted values).
     */
    int updateEncryptionKey(String newEncryptionKey);
    
    /**
     * Gets configurations that should be encrypted but aren't.
     */
    List<TeamConfiguration> getUnencryptedSensitiveConfigurations();
}