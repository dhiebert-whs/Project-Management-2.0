// src/main/java/org/frcpm/services/PartService.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.services;

import org.frcpm.models.Part;
import org.frcpm.models.PartTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing parts inventory.
 * Provides business logic for part CRUD operations, inventory management,
 * and stock level monitoring.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
public interface PartService {
    
    // Basic CRUD Operations
    
    /**
     * Creates a new part in the inventory system.
     */
    Part createPart(Part part);
    
    /**
     * Updates an existing part.
     */
    Part updatePart(Long partId, Part part);
    
    /**
     * Finds a part by ID.
     */
    Optional<Part> findPartById(Long partId);
    
    /**
     * Finds a part by part number.
     */
    Optional<Part> findPartByPartNumber(String partNumber);
    
    /**
     * Deletes a part (soft delete - marks as inactive).
     */
    void deletePart(Long partId);
    
    /**
     * Permanently deletes a part from the system.
     */
    void permanentlyDeletePart(Long partId);
    
    // Inventory Management
    
    /**
     * Updates part quantity and creates transaction record.
     */
    Part updateQuantity(Long partId, int quantityChange, PartTransaction.TransactionType transactionType, String reason);
    
    /**
     * Restocks a part to optimal level.
     */
    Part restockPart(Long partId, int quantity, BigDecimal unitCost, String vendor, String referenceNumber);
    
    /**
     * Uses parts for a project/task and creates usage transaction.
     */
    Part useParts(Long partId, int quantity, Long projectId, Long taskId, String reason);
    
    /**
     * Adjusts inventory for physical count corrections.
     */
    Part adjustInventory(Long partId, int newQuantity, String reason);
    
    // Stock Level Monitoring
    
    /**
     * Gets all parts that are low on stock.
     */
    List<Part> getLowStockParts();
    
    /**
     * Gets all parts that are critically low.
     */
    List<Part> getCriticallyLowParts();
    
    /**
     * Gets all parts that are out of stock.
     */
    List<Part> getOutOfStockParts();
    
    /**
     * Gets all parts that need reordering.
     */
    List<Part> getPartsNeedingReorder();
    
    /**
     * Calculates total inventory value.
     */
    BigDecimal getTotalInventoryValue();
    
    /**
     * Calculates inventory value by category.
     */
    BigDecimal getInventoryValueByCategory(Part.PartCategory category);
    
    // Search and Filter Operations
    
    /**
     * Gets all active parts.
     */
    List<Part> getAllActiveParts();
    
    /**
     * Gets parts by category.
     */
    List<Part> getPartsByCategory(Part.PartCategory category);
    
    /**
     * Searches parts by name, description, or part number.
     */
    List<Part> searchParts(String searchTerm);
    
    /**
     * Gets parts by vendor.
     */
    List<Part> getPartsByVendor(String vendor);
    
    /**
     * Gets parts by storage location.
     */
    List<Part> getPartsByStorageLocation(String location);
    
    /**
     * Gets parts with pagination support.
     */
    Page<Part> getPartsPaginated(Pageable pageable);
    
    /**
     * Gets parts by category with pagination.
     */
    Page<Part> getPartsByCategoryPaginated(Part.PartCategory category, Pageable pageable);
    
    // Analytics and Reporting
    
    /**
     * Gets part usage statistics for a date range.
     */
    List<Object[]> getPartUsageStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * Gets parts not used since a specific date.
     */
    List<Part> getUnusedPartsSince(LocalDate date);
    
    /**
     * Gets most expensive parts.
     */
    List<Part> getMostExpensiveParts(int limit);
    
    /**
     * Gets parts with highest inventory value.
     */
    List<Part> getHighestValueInventoryParts(int limit);
    
    /**
     * Gets inventory turnover statistics.
     */
    List<Object[]> getInventoryTurnoverStats();
    
    // Bulk Operations
    
    /**
     * Imports parts from CSV or other data source.
     */
    List<Part> importParts(List<Part> parts);
    
    /**
     * Bulk updates stock levels.
     */
    List<Part> bulkUpdateStockLevels(List<Long> partIds, List<Integer> newQuantities, String reason);
    
    /**
     * Bulk updates minimum stock levels.
     */
    List<Part> bulkUpdateMinimumStock(List<Long> partIds, List<Integer> minimumStocks);
    
    // Validation and Business Rules
    
    /**
     * Validates that a part number is unique.
     */
    boolean isPartNumberUnique(String partNumber, Long excludePartId);
    
    /**
     * Validates inventory transaction.
     */
    boolean validateInventoryTransaction(Long partId, int quantityChange);
    
    /**
     * Checks if part can be deleted (no pending transactions).
     */
    boolean canDeletePart(Long partId);
    
    // Notification and Alerts
    
    /**
     * Gets parts requiring immediate attention (critically low, expired, etc.).
     */
    List<Part> getPartsRequiringAttention();
    
    /**
     * Calculates reorder recommendations.
     */
    List<Object[]> getReorderRecommendations();
    
    /**
     * Gets parts with long lead times that should be ordered early.
     */
    List<Part> getPartsWithLongLeadTimes(int minDays);
}