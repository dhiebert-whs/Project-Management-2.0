// src/main/java/org/frcpm/repositories/spring/TeamConfigurationRepository.java
// Team Configuration Management

package org.frcpm.repositories.spring;

import org.frcpm.models.TeamConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for TeamConfiguration entities.
 * Provides team configuration management functionality.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-Config
 * @since Configuration Management
 */
@Repository
public interface TeamConfigurationRepository extends JpaRepository<TeamConfiguration, Long> {
    
    // Category-based Queries
    
    /**
     * Finds all configurations for a specific category.
     */
    List<TeamConfiguration> findByCategoryAndIsActiveTrueOrderByConfigKeyAsc(
            TeamConfiguration.ConfigurationCategory category);
    
    /**
     * Finds all configurations for multiple categories.
     */
    List<TeamConfiguration> findByCategoryInAndIsActiveTrueOrderByCategoryAscConfigKeyAsc(
            List<TeamConfiguration.ConfigurationCategory> categories);
    
    /**
     * Counts configurations by category.
     */
    long countByCategoryAndIsActiveTrue(TeamConfiguration.ConfigurationCategory category);
    
    // Key-based Queries
    
    /**
     * Finds a specific configuration by category and key.
     */
    Optional<TeamConfiguration> findByCategoryAndConfigKeyAndIsActiveTrue(
            TeamConfiguration.ConfigurationCategory category, String configKey);
    
    /**
     * Finds configurations by key pattern.
     */
    List<TeamConfiguration> findByConfigKeyContainingIgnoreCaseAndIsActiveTrueOrderByConfigKeyAsc(String keyPattern);
    
    /**
     * Checks if a configuration exists.
     */
    boolean existsByCategoryAndConfigKeyAndIsActiveTrue(
            TeamConfiguration.ConfigurationCategory category, String configKey);
    
    // Value-based Queries
    
    /**
     * Finds configurations with specific value.
     */
    List<TeamConfiguration> findByConfigValueAndIsActiveTrueOrderByCategoryAscConfigKeyAsc(String configValue);
    
    /**
     * Finds configurations with value containing pattern.
     */
    List<TeamConfiguration> findByConfigValueContainingIgnoreCaseAndIsActiveTrueOrderByCategoryAscConfigKeyAsc(String valuePattern);
    
    /**
     * Finds configurations with null or empty values.
     */
    @Query("SELECT tc FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "AND (tc.configValue IS NULL OR TRIM(tc.configValue) = '') " +
           "ORDER BY tc.category ASC, tc.configKey ASC")
    List<TeamConfiguration> findConfigurationsWithEmptyValues();
    
    // Data Type Queries
    
    /**
     * Finds configurations by data type.
     */
    List<TeamConfiguration> findByDataTypeAndIsActiveTrueOrderByCategoryAscConfigKeyAsc(
            TeamConfiguration.ConfigurationDataType dataType);
    
    /**
     * Finds configurations by multiple data types.
     */
    List<TeamConfiguration> findByDataTypeInAndIsActiveTrueOrderByCategoryAscConfigKeyAsc(
            List<TeamConfiguration.ConfigurationDataType> dataTypes);
    
    // Requirement and Security Queries
    
    /**
     * Finds required configurations.
     */
    List<TeamConfiguration> findByIsRequiredTrueAndIsActiveTrueOrderByCategoryAscConfigKeyAsc();
    
    /**
     * Finds required configurations that are not set.
     */
    @Query("SELECT tc FROM TeamConfiguration tc WHERE tc.isRequired = true AND tc.isActive = true " +
           "AND (tc.configValue IS NULL OR TRIM(tc.configValue) = '') " +
           "ORDER BY tc.category ASC, tc.configKey ASC")
    List<TeamConfiguration> findRequiredConfigurationsNotSet();
    
    /**
     * Finds encrypted configurations.
     */
    List<TeamConfiguration> findByIsEncryptedTrueAndIsActiveTrueOrderByCategoryAscConfigKeyAsc();
    
    /**
     * Finds configurations requiring encryption that aren't encrypted.
     */
    @Query("SELECT tc FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "AND tc.dataType = 'PASSWORD' AND tc.isEncrypted = false " +
           "ORDER BY tc.category ASC, tc.configKey ASC")
    List<TeamConfiguration> findPasswordConfigurationsNotEncrypted();
    
    // Search and Filter Queries
    
    /**
     * Searches configurations by display name.
     */
    List<TeamConfiguration> findByDisplayNameContainingIgnoreCaseAndIsActiveTrueOrderByDisplayNameAsc(String searchTerm);
    
    /**
     * Searches configurations by description.
     */
    List<TeamConfiguration> findByDescriptionContainingIgnoreCaseAndIsActiveTrueOrderByDisplayNameAsc(String searchTerm);
    
    // Note: searchConfigurations query removed - LIKE CONCAT validation issues in H2
    
    // Time-based Queries
    
    /**
     * Finds configurations created within a date range.
     */
    List<TeamConfiguration> findByCreatedAtBetweenAndIsActiveTrueOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds configurations updated within a date range.
     */
    List<TeamConfiguration> findByUpdatedAtBetweenAndIsActiveTrueOrderByUpdatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds recently updated configurations.
     */
    List<TeamConfiguration> findByUpdatedAtAfterAndIsActiveTrueOrderByUpdatedAtDesc(LocalDateTime since);
    
    // User-based Queries
    
    /**
     * Finds configurations created by a user.
     */
    List<TeamConfiguration> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Finds configurations updated by a user.
     */
    List<TeamConfiguration> findByUpdatedByAndIsActiveTrueOrderByUpdatedAtDesc(String updatedBy);
    
    // Validation Queries
    
    /**
     * Finds configurations with validation patterns.
     */
    List<TeamConfiguration> findByValidationPatternIsNotNullAndIsActiveTrueOrderByCategoryAscConfigKeyAsc();
    
    /**
     * Finds configurations with default values.
     */
    List<TeamConfiguration> findByDefaultValueIsNotNullAndIsActiveTrueOrderByCategoryAscConfigKeyAsc();
    
    /**
     * Validates all configurations.
     */
    @Query("SELECT tc FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "AND tc.isRequired = true " +
           "AND (tc.configValue IS NULL OR TRIM(tc.configValue) = '')")
    List<TeamConfiguration> findInvalidConfigurations();
    
    // Statistical Queries
    
    /**
     * Gets configuration statistics by category.
     */
    @Query("SELECT tc.category, COUNT(tc), " +
           "SUM(CASE WHEN tc.configValue IS NOT NULL AND TRIM(tc.configValue) != '' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN tc.isRequired = true THEN 1 ELSE 0 END) " +
           "FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "GROUP BY tc.category " +
           "ORDER BY tc.category ASC")
    List<Object[]> getConfigurationStatisticsByCategory();
    
    /**
     * Gets data type distribution.
     */
    @Query("SELECT tc.dataType, COUNT(tc) " +
           "FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "GROUP BY tc.dataType " +
           "ORDER BY COUNT(tc) DESC")
    List<Object[]> getDataTypeDistribution();
    
    /**
     * Gets setup completion status.
     */
    @Query("SELECT " +
           "COUNT(tc) as totalConfigurations, " +
           "SUM(CASE WHEN tc.configValue IS NOT NULL AND TRIM(tc.configValue) != '' THEN 1 ELSE 0 END) as configuredCount, " +
           "SUM(CASE WHEN tc.isRequired = true THEN 1 ELSE 0 END) as requiredCount, " +
           "SUM(CASE WHEN tc.isRequired = true AND (tc.configValue IS NOT NULL AND TRIM(tc.configValue) != '') THEN 1 ELSE 0 END) as requiredConfiguredCount " +
           "FROM TeamConfiguration tc WHERE tc.isActive = true")
    Object[] getSetupCompletionStatus();
    
    // Specific Configuration Queries
    
    /**
     * Gets team information configurations by category.
     */
    List<TeamConfiguration> findByCategoryOrderByConfigKeyAsc(
            TeamConfiguration.ConfigurationCategory category);
    
    /**
     * Gets API configuration status.
     */
    @Query("SELECT tc.category, tc.configKey, " +
           "CASE WHEN tc.configValue IS NOT NULL AND TRIM(tc.configValue) != '' THEN 'CONFIGURED' ELSE 'NOT_CONFIGURED' END " +
           "FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "AND tc.category IN ('FRC_API', 'TBA_API', 'EXTERNAL_APIS') " +
           "ORDER BY tc.category ASC, tc.configKey ASC")
    List<Object[]> getApiConfigurationStatus();
    
    /**
     * Gets integration readiness status.
     */
    @Query("SELECT tc.category, COUNT(tc), " +
           "SUM(CASE WHEN tc.configValue IS NOT NULL AND TRIM(tc.configValue) != '' THEN 1 ELSE 0 END) " +
           "FROM TeamConfiguration tc WHERE tc.isActive = true " +
           "AND tc.category IN ('DISCORD', 'GOOGLE_WORKSPACE', 'GITHUB') " +
           "GROUP BY tc.category " +
           "ORDER BY tc.category ASC")
    List<Object[]> getIntegrationReadinessStatus();
    
    // Active Status Management
    
    /**
     * Finds all inactive configurations.
     */
    List<TeamConfiguration> findByIsActiveFalseOrderByCategoryAscConfigKeyAsc();
    
    /**
     * Finds configurations by category including inactive ones.
     */
    List<TeamConfiguration> findByCategoryOrderByCategoryAscConfigKeyAsc(
            TeamConfiguration.ConfigurationCategory category);
    
    // Bulk Operations Support
    
    /**
     * Finds configurations by IDs.
     */
    List<TeamConfiguration> findByIdInAndIsActiveTrueOrderByCategoryAscConfigKeyAsc(List<Long> ids);
    
    /**
     * Finds configurations by keys within a category.
     */
    List<TeamConfiguration> findByCategoryAndConfigKeyInAndIsActiveTrueOrderByConfigKeyAsc(
            TeamConfiguration.ConfigurationCategory category, List<String> configKeys);
}