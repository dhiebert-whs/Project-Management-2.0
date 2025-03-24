package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a physical component or part in the FRC Project Management System.
 * This corresponds to the Component model in the Django application.
 */
@Entity
@Table(name = "components")
public class Component {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    
    @Column(name = "part_number", length = 100)
    private String partNumber;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "expected_delivery")
    private LocalDate expectedDelivery;
    
    @Column(name = "actual_delivery")
    private LocalDate actualDelivery;
    
    @Column(name = "is_delivered")
    private boolean delivered = false;
    
    @ManyToMany(mappedBy = "requiredComponents")
    private Set<Task> requiredForTasks = new HashSet<>();
    
    // Constructors
    
    public Component() {
        // Default constructor required by JPA
    }
    
    public Component(String name) {
        this.name = name;
    }
    
    public Component(String name, String partNumber) {
        this.name = name;
        this.partNumber = partNumber;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(LocalDate expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    public LocalDate getActualDelivery() {
        return actualDelivery;
    }

    public void setActualDelivery(LocalDate actualDelivery) {
        this.actualDelivery = actualDelivery;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
        
        // If marking as delivered, set actual delivery date to today if not already set
        if (delivered && actualDelivery == null) {
            actualDelivery = LocalDate.now();
        }
    }

    public Set<Task> getRequiredForTasks() {
        return requiredForTasks;
    }

    public void setRequiredForTasks(Set<Task> requiredForTasks) {
        this.requiredForTasks = requiredForTasks;
    }
    
    // Helper methods
    
    @Override
    public String toString() {
        if (partNumber != null && !partNumber.isEmpty()) {
            return name + " (" + partNumber + ")";
        }
        return name;
    }
}