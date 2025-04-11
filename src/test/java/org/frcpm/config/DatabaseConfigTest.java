// src/test/java/org/frcpm/config/DatabaseConfigTest.java
package org.frcpm.config;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseConfigTest {
    
    @BeforeEach
    public void setUp() {
        // Initialize DB before each test
        DatabaseConfig.initialize();
    }
    
    @AfterEach
    public void tearDown() {
        // Shutdown DB after each test
        DatabaseConfig.shutdown();
    }
    
    @Test
    public void testGetEntityManagerFactory() {
        EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
        assertNotNull(emf, "EntityManagerFactory should not be null");
        assertTrue(emf.isOpen(), "EntityManagerFactory should be open");
    }
    
    @Test
    public void testGetEntityManager() {
        EntityManager em = DatabaseConfig.getEntityManager();
        assertNotNull(em, "EntityManager should not be null");
        assertTrue(em.isOpen(), "EntityManager should be open");
        em.close();
    }
}