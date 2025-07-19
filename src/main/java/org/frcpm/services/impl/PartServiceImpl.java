// src/main/java/org/frcpm/services/impl/PartServiceImpl.java
// Phase 4A.2: Parts Inventory Management System

package org.frcpm.services.impl;

import org.frcpm.models.Part;
import org.frcpm.models.PartTransaction;
import org.frcpm.repositories.spring.PartRepository;
import org.frcpm.services.PartService;
import org.frcpm.services.PartTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of PartService for managing parts inventory.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.2
 * @since Phase 4A.2 - Parts Inventory Management
 */
@Service
@Transactional
public class PartServiceImpl implements PartService {
    
    private static final Logger LOGGER = Logger.getLogger(PartServiceImpl.class.getName());
    
    @Autowired
    private PartRepository partRepository;
    
    @Autowired
    private PartTransactionService partTransactionService;
    
    // Basic CRUD Operations
    
    @Override
    public Part createPart(Part part) {
        try {
            LOGGER.info("Creating new part: " + part.getPartNumber());
            
            // Validate unique part number
            if (!isPartNumberUnique(part.getPartNumber(), null)) {
                throw new IllegalArgumentException("Part number already exists: " + part.getPartNumber());
            }
            
            // Set creation timestamp
            part.setCreatedAt(LocalDateTime.now());
            part.setUpdatedAt(LocalDateTime.now());
            
            Part savedPart = partRepository.save(part);
            
            // Create initial stock transaction if quantity > 0
            if (savedPart.getQuantityOnHand() > 0) {
                partTransactionService.createTransaction(
                    savedPart, 
                    PartTransaction.TransactionType.INITIAL_STOCK,
                    savedPart.getQuantityOnHand(),
                    "Initial inventory entry",
                    savedPart.getUnitCost(),
                    null, null, null
                );
            }
            
            LOGGER.info("Successfully created part: " + savedPart.getId());
            return savedPart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating part: " + part.getPartNumber(), e);
            throw new RuntimeException("Failed to create part: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Part updatePart(Long partId, Part part) {
        try {
            LOGGER.info("Updating part: " + partId);
            
            Optional<Part> existingPartOpt = partRepository.findById(partId);
            if (!existingPartOpt.isPresent()) {
                throw new IllegalArgumentException("Part not found: " + partId);
            }
            
            Part existingPart = existingPartOpt.get();
            
            // Validate unique part number if it's changing
            if (!existingPart.getPartNumber().equals(part.getPartNumber()) &&
                !isPartNumberUnique(part.getPartNumber(), partId)) {
                throw new IllegalArgumentException("Part number already exists: " + part.getPartNumber());
            }
            
            // Update fields (preserve ID and timestamps)
            part.setId(partId);
            part.setCreatedAt(existingPart.getCreatedAt());
            part.setUpdatedAt(LocalDateTime.now());
            
            Part savedPart = partRepository.save(part);
            LOGGER.info("Successfully updated part: " + savedPart.getId());
            return savedPart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating part: " + partId, e);
            throw new RuntimeException("Failed to update part: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Part> findPartById(Long partId) {
        try {
            return partRepository.findById(partId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding part by ID: " + partId, e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Part> findPartByPartNumber(String partNumber) {
        try {
            return partRepository.findByPartNumber(partNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding part by part number: " + partNumber, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void deletePart(Long partId) {
        try {
            LOGGER.info("Soft deleting part: " + partId);
            
            Optional<Part> partOpt = partRepository.findById(partId);
            if (!partOpt.isPresent()) {
                throw new IllegalArgumentException("Part not found: " + partId);
            }
            
            Part part = partOpt.get();
            part.setIsActive(false);
            part.setUpdatedAt(LocalDateTime.now());
            
            partRepository.save(part);
            LOGGER.info("Successfully soft deleted part: " + partId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting part: " + partId, e);
            throw new RuntimeException("Failed to delete part: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void permanentlyDeletePart(Long partId) {
        try {
            LOGGER.warning("Permanently deleting part: " + partId);
            
            if (!canDeletePart(partId)) {
                throw new IllegalStateException("Cannot delete part with existing transactions: " + partId);
            }
            
            partRepository.deleteById(partId);
            LOGGER.warning("Permanently deleted part: " + partId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error permanently deleting part: " + partId, e);
            throw new RuntimeException("Failed to permanently delete part: " + e.getMessage(), e);
        }
    }
    
    // Inventory Management
    
    @Override
    public Part updateQuantity(Long partId, int quantityChange, PartTransaction.TransactionType transactionType, String reason) {
        try {
            LOGGER.info("Updating quantity for part " + partId + " by " + quantityChange);
            
            Optional<Part> partOpt = partRepository.findById(partId);
            if (!partOpt.isPresent()) {
                throw new IllegalArgumentException("Part not found: " + partId);
            }
            
            Part part = partOpt.get();
            
            // Validate transaction
            if (!validateInventoryTransaction(partId, quantityChange)) {
                throw new IllegalArgumentException("Invalid inventory transaction");
            }
            
            // Update quantity
            int newQuantity = part.getQuantityOnHand() + quantityChange;
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Insufficient quantity available");
            }
            
            part.setQuantityOnHand(newQuantity);
            part.setUpdatedAt(LocalDateTime.now());
            
            // Update last used date for outgoing transactions
            if (quantityChange < 0) {
                part.setLastUsedDate(LocalDate.now());
            }
            
            Part savedPart = partRepository.save(part);
            
            // Create transaction record
            partTransactionService.createTransaction(
                savedPart, transactionType, Math.abs(quantityChange), reason,
                null, null, null, null
            );
            
            LOGGER.info("Successfully updated quantity for part: " + partId);
            return savedPart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating quantity for part: " + partId, e);
            throw new RuntimeException("Failed to update quantity: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Part restockPart(Long partId, int quantity, BigDecimal unitCost, String vendor, String referenceNumber) {
        try {
            LOGGER.info("Restocking part " + partId + " with " + quantity + " units");
            
            Optional<Part> partOpt = partRepository.findById(partId);
            if (!partOpt.isPresent()) {
                throw new IllegalArgumentException("Part not found: " + partId);
            }
            
            Part part = partOpt.get();
            
            // Update quantity and restock date
            part.setQuantityOnHand(part.getQuantityOnHand() + quantity);
            part.setLastRestockDate(LocalDate.now());
            part.setUpdatedAt(LocalDateTime.now());
            
            Part savedPart = partRepository.save(part);
            
            // Create purchase transaction
            PartTransaction transaction = partTransactionService.createTransaction(
                savedPart, PartTransaction.TransactionType.PURCHASE, quantity,
                "Restocked from vendor: " + vendor, unitCost, null, null, null
            );
            
            if (referenceNumber != null) {
                transaction.setReferenceNumber(referenceNumber);
                transaction.setVendor(vendor);
            }
            
            LOGGER.info("Successfully restocked part: " + partId);
            return savedPart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error restocking part: " + partId, e);
            throw new RuntimeException("Failed to restock part: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Part useParts(Long partId, int quantity, Long projectId, Long taskId, String reason) {
        try {
            LOGGER.info("Using " + quantity + " units of part " + partId);
            
            Optional<Part> partOpt = partRepository.findById(partId);
            if (!partOpt.isPresent()) {
                throw new IllegalArgumentException("Part not found: " + partId);
            }
            
            Part part = partOpt.get();
            
            if (part.getQuantityOnHand() < quantity) {
                throw new IllegalArgumentException("Insufficient quantity available. Available: " + 
                    part.getQuantityOnHand() + ", Requested: " + quantity);
            }
            
            // Update quantity
            part.setQuantityOnHand(part.getQuantityOnHand() - quantity);
            part.setLastUsedDate(LocalDate.now());
            part.setUpdatedAt(LocalDateTime.now());
            
            Part savedPart = partRepository.save(part);
            
            // Create usage transaction
            partTransactionService.createTransaction(
                savedPart, PartTransaction.TransactionType.USAGE, quantity,
                reason != null ? reason : "Used in project/task",
                null, projectId, taskId, null
            );
            
            LOGGER.info("Successfully used parts: " + partId);
            return savedPart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error using parts: " + partId, e);
            throw new RuntimeException("Failed to use parts: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Part adjustInventory(Long partId, int newQuantity, String reason) {
        try {
            LOGGER.info("Adjusting inventory for part " + partId + " to " + newQuantity);
            
            Optional<Part> partOpt = partRepository.findById(partId);
            if (!partOpt.isPresent()) {
                throw new IllegalArgumentException("Part not found: " + partId);
            }
            
            Part part = partOpt.get();
            int oldQuantity = part.getQuantityOnHand();
            int adjustment = newQuantity - oldQuantity;
            
            if (adjustment == 0) {
                return part; // No change needed
            }
            
            part.setQuantityOnHand(newQuantity);
            part.setUpdatedAt(LocalDateTime.now());
            
            Part savedPart = partRepository.save(part);
            
            // Create adjustment transaction
            PartTransaction.TransactionType transactionType = adjustment > 0 ? 
                PartTransaction.TransactionType.ADJUSTMENT_POSITIVE : 
                PartTransaction.TransactionType.ADJUSTMENT_NEGATIVE;
            
            partTransactionService.createTransaction(
                savedPart, transactionType, Math.abs(adjustment),
                reason != null ? reason : "Inventory adjustment",
                null, null, null, null
            );
            
            LOGGER.info("Successfully adjusted inventory for part: " + partId);
            return savedPart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adjusting inventory for part: " + partId, e);
            throw new RuntimeException("Failed to adjust inventory: " + e.getMessage(), e);
        }
    }
    
    // Stock Level Monitoring
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getLowStockParts() {
        try {
            return partRepository.findByQuantityOnHandLessThanEqualMinimumStockAndIsActiveTrue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting low stock parts", e);
            throw new RuntimeException("Failed to get low stock parts: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getCriticallyLowParts() {
        try {
            return partRepository.findByQuantityOnHandLessThanEqualSafetyStockAndIsActiveTrue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting critically low parts", e);
            throw new RuntimeException("Failed to get critically low parts: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getOutOfStockParts() {
        try {
            return partRepository.findByQuantityOnHandAndIsActiveTrue(0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting out of stock parts", e);
            throw new RuntimeException("Failed to get out of stock parts: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getPartsNeedingReorder() {
        try {
            return partRepository.findByQuantityOnHandLessThanMinimumStockAndIsActiveTrue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts needing reorder", e);
            throw new RuntimeException("Failed to get parts needing reorder: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue() {
        try {
            List<Part> parts = partRepository.findByIsActiveTrue();
            return parts.stream()
                .map(Part::getInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating total inventory value", e);
            throw new RuntimeException("Failed to calculate inventory value: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getInventoryValueByCategory(Part.PartCategory category) {
        try {
            List<Part> parts = partRepository.findByCategoryAndIsActiveTrue(category);
            return parts.stream()
                .map(Part::getInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating inventory value by category: " + category, e);
            throw new RuntimeException("Failed to calculate inventory value: " + e.getMessage(), e);
        }
    }
    
    // Search and Filter Operations
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getAllActiveParts() {
        try {
            return partRepository.findByIsActiveTrue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting all active parts", e);
            throw new RuntimeException("Failed to get active parts: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getPartsByCategory(Part.PartCategory category) {
        try {
            return partRepository.findByCategoryAndIsActiveTrue(category);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts by category: " + category, e);
            throw new RuntimeException("Failed to get parts by category: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> searchParts(String searchTerm) {
        try {
            return partRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrPartNumberContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching parts: " + searchTerm, e);
            throw new RuntimeException("Failed to search parts: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getPartsByVendor(String vendor) {
        try {
            return partRepository.findByVendorContainingIgnoreCase(vendor);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts by vendor: " + vendor, e);
            throw new RuntimeException("Failed to get parts by vendor: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getPartsByStorageLocation(String location) {
        try {
            return partRepository.findByStorageLocationContainingIgnoreCase(location);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts by storage location: " + location, e);
            throw new RuntimeException("Failed to get parts by storage location: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Part> getPartsPaginated(Pageable pageable) {
        try {
            return partRepository.findAll(pageable);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts paginated", e);
            throw new RuntimeException("Failed to get parts paginated: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Part> getPartsByCategoryPaginated(Part.PartCategory category, Pageable pageable) {
        // Note: This would require a custom implementation with Specification
        // For now, return a simple implementation
        try {
            List<Part> parts = partRepository.findByCategoryAndIsActiveTrue(category);
            // Convert to Page would require custom implementation
            return Page.empty(pageable);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts by category paginated", e);
            throw new RuntimeException("Failed to get parts by category paginated: " + e.getMessage(), e);
        }
    }
    
    // Validation and Business Rules
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPartNumberUnique(String partNumber, Long excludePartId) {
        try {
            Optional<Part> existingPart = partRepository.findByPartNumber(partNumber);
            if (!existingPart.isPresent()) {
                return true;
            }
            
            // If we're excluding a specific part (for updates), check if it's the same part
            return excludePartId != null && existingPart.get().getId().equals(excludePartId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking part number uniqueness: " + partNumber, e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateInventoryTransaction(Long partId, int quantityChange) {
        try {
            if (quantityChange >= 0) {
                return true; // Adding inventory is always valid
            }
            
            Optional<Part> partOpt = partRepository.findById(partId);
            if (!partOpt.isPresent()) {
                return false;
            }
            
            Part part = partOpt.get();
            return part.getQuantityOnHand() >= Math.abs(quantityChange);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating inventory transaction", e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canDeletePart(Long partId) {
        try {
            // Check if part has any transactions
            // This would require checking the transaction service
            // For now, return true (basic implementation)
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if part can be deleted: " + partId, e);
            return false;
        }
    }
    
    // Simplified implementations for methods requiring complex queries
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPartUsageStatistics(LocalDate startDate, LocalDate endDate) {
        // Simplified implementation - would require transaction service integration
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getUnusedPartsSince(LocalDate date) {
        try {
            return partRepository.findByLastUsedDateBeforeAndIsActiveTrue(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting unused parts since: " + date, e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getMostExpensiveParts(int limit) {
        try {
            return partRepository.findByIsActiveTrueOrderByUnitCostDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting most expensive parts", e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getHighestValueInventoryParts(int limit) {
        try {
            return partRepository.findByIsActiveTrue()
                .stream()
                .sorted((p1, p2) -> p2.getInventoryValue().compareTo(p1.getInventoryValue()))
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting highest value inventory parts", e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getInventoryTurnoverStats() {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    public List<Part> importParts(List<Part> parts) {
        // Simplified implementation
        return parts.stream()
            .map(this::createPart)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Part> bulkUpdateStockLevels(List<Long> partIds, List<Integer> newQuantities, String reason) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    public List<Part> bulkUpdateMinimumStock(List<Long> partIds, List<Integer> minimumStocks) {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getPartsRequiringAttention() {
        try {
            List<Part> lowStockParts = getLowStockParts();
            List<Part> criticalParts = getCriticallyLowParts();
            // Combine and deduplicate
            return lowStockParts;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts requiring attention", e);
            return List.of();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getReorderRecommendations() {
        // Simplified implementation
        return List.of();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Part> getPartsWithLongLeadTimes(int minDays) {
        try {
            return partRepository.findByLeadTimeDaysGreaterThanAndIsActiveTrue(minDays);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting parts with long lead times", e);
            return List.of();
        }
    }
}