// src/main/java/org/frcpm/repositories/spring/PartRepository.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.repositories.spring;

import org.frcpm.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Part entities.
 * Provides inventory management and part lookup functionality.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    
    // Basic Lookups
    
    /**
     * Finds a part by its unique part number.
     */
    Optional<Part> findByPartNumber(String partNumber);
    
    /**
     * Finds a part by part number (case-insensitive).
     */
    Optional<Part> findByPartNumberIgnoreCase(String partNumber);
    
    /**
     * Checks if a part number already exists.
     */
    boolean existsByPartNumber(String partNumber);
    
    /**
     * Checks if a part number exists (case-insensitive).
     */
    boolean existsByPartNumberIgnoreCase(String partNumber);
    
    /**
     * Finds parts by name containing search term.
     */
    List<Part> findByNameContainingIgnoreCase(String name);
    
    /**
     * Finds parts by category.
     */
    List<Part> findByCategory(Part.PartCategory category);
    
    /**
     * Finds parts by category and active status.
     */
    List<Part> findByCategoryAndIsActiveTrue(Part.PartCategory category);
    
    /**
     * Finds all active parts.
     */
    List<Part> findByIsActiveTrue();
    
    /**
     * Finds parts by vendor.
     */
    List<Part> findByVendorContainingIgnoreCase(String vendor);
    
    /**
     * Finds parts by storage location.
     */
    List<Part> findByStorageLocationContainingIgnoreCase(String location);
    
    // Inventory Status Queries
    
    /**
     * Finds parts that are low on stock (quantity <= minimum stock).
     */
    @Query("SELECT p FROM Part p WHERE p.quantityOnHand <= p.minimumStock AND p.isActive = true")
    List<Part> findByQuantityOnHandLessThanEqualMinimumStockAndIsActiveTrue();
    
    /**
     * Finds parts that are critically low (quantity <= safety stock).
     */
    @Query("SELECT p FROM Part p WHERE p.quantityOnHand <= p.safetyStock AND p.isActive = true")
    List<Part> findByQuantityOnHandLessThanEqualSafetyStockAndIsActiveTrue();
    
    /**
     * Finds parts that are out of stock.
     */
    List<Part> findByQuantityOnHandAndIsActiveTrue(int quantity);
    
    /**
     * Finds parts that need reordering based on stock levels.
     */
    @Query("SELECT p FROM Part p WHERE p.quantityOnHand < p.minimumStock AND p.isActive = true")\n    List<Part> findByQuantityOnHandLessThanMinimumStockAndIsActiveTrue();
    
    /**
     * Finds parts with quantities between specified values.
     */
    List<Part> findByQuantityOnHandBetweenAndIsActiveTrue(int minQuantity, int maxQuantity);
    
    // Cost and Financial Queries
    
    /**
     * Finds parts within a cost range.
     */
    List<Part> findByUnitCostBetweenAndIsActiveTrue(BigDecimal minCost, BigDecimal maxCost);
    
    /**
     * Finds parts above a certain cost threshold.
     */
    List<Part> findByUnitCostGreaterThanAndIsActiveTrue(BigDecimal threshold);
    
    /**
     * Finds parts with no cost information.
     */
    List<Part> findByUnitCostIsNullAndIsActiveTrue();
    
    // Date-based Queries
    
    /**
     * Finds parts not used since a specific date.
     */
    List<Part> findByLastUsedDateBeforeAndIsActiveTrue(LocalDate date);
    
    /**
     * Finds parts never used.
     */
    List<Part> findByLastUsedDateIsNullAndIsActiveTrue();
    
    /**
     * Finds parts restocked after a specific date.
     */
    List<Part> findByLastRestockDateAfterAndIsActiveTrue(LocalDate date);
    
    /**
     * Finds parts not restocked since a specific date.
     */
    List<Part> findByLastRestockDateBeforeAndIsActiveTrue(LocalDate date);
    
    // Consumable vs Reusable
    
    /**
     * Finds consumable parts only.
     */
    List<Part> findByIsConsumableTrueAndIsActiveTrue();
    
    /**
     * Finds reusable parts only.
     */
    List<Part> findByIsConsumableFalseAndIsActiveTrue();
    
    // Advanced Search
    
    /**
     * Searches parts by multiple criteria (name, description, part number).
     */
    List<Part> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrPartNumberContainingIgnoreCase(
            String nameSearch, String descriptionSearch, String partNumberSearch);
    
    /**
     * Finds parts by category and vendor.
     */
    List<Part> findByCategoryAndVendorContainingIgnoreCaseAndIsActiveTrue(
            Part.PartCategory category, String vendor);
    
    // Statistical Queries
    
    /**
     * Counts parts by category.
     */
    long countByCategoryAndIsActiveTrue(Part.PartCategory category);
    
    /**
     * Counts low stock parts.
     */
    @Query("SELECT COUNT(p) FROM Part p WHERE p.quantityOnHand <= p.minimumStock AND p.isActive = true")\n    long countByQuantityOnHandLessThanEqualMinimumStockAndIsActiveTrue();
    
    /**
     * Counts out of stock parts.
     */
    long countByQuantityOnHandAndIsActiveTrue(int quantity);
    
    /**
     * Counts parts by vendor.
     */
    long countByVendorAndIsActiveTrue(String vendor);
    
    // Inventory Management
    
    /**
     * Finds parts that haven't been counted recently (for inventory audits).
     */
    List<Part> findByUpdatedAtBeforeAndIsActiveTrue(LocalDate cutoffDate);
    
    /**
     * Finds parts with long lead times.
     */
    List<Part> findByLeadTimeDaysGreaterThanAndIsActiveTrue(int days);
    
    /**
     * Finds parts from specific vendors.
     */
    List<Part> findByVendorInAndIsActiveTrue(List<String> vendors);
    
    /**
     * Finds parts in specific storage locations.
     */
    List<Part> findByStorageLocationInAndIsActiveTrue(List<String> locations);
    
    // Ordering Operations
    
    /**
     * Finds all parts ordered by name.
     */
    List<Part> findByIsActiveTrueOrderByName();
    
    /**
     * Finds all parts ordered by category then name.
     */
    List<Part> findByIsActiveTrueOrderByCategoryAscNameAsc();
    
    /**
     * Finds parts ordered by quantity (lowest first).
     */
    List<Part> findByIsActiveTrueOrderByQuantityOnHandAsc();
    
    /**
     * Finds parts ordered by last used date (oldest first).
     */
    List<Part> findByIsActiveTrueOrderByLastUsedDateAsc();
    
    /**
     * Finds parts ordered by unit cost (highest first).
     */
    List<Part> findByIsActiveTrueOrderByUnitCostDesc();
}