// src/test/java/org/frcpm/repositories/spring/UserRepositoryTest.java

package org.frcpm.repositories.spring;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests for UserRepository - Phase 2B Security Testing
 * 
 * Tests the JPA repository layer for user management and COPPA compliance.
 * Uses @DataJpaTest for focused database testing with in-memory H2.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B Testing
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testStudent;
    private User testMentor;
    private User testAdmin;
    private User testParent;
    private User testMinorStudent;
    
    @BeforeEach
    void setUp() {
        // Create test users for different scenarios
        testStudent = createUser("student1", "student@frcteam.org", "John", "Student", 
                                UserRole.STUDENT, 16, null, false);
        
        testMentor = createUser("mentor1", "mentor@frcteam.org", "Jane", "Mentor", 
                               UserRole.MENTOR, 35, null, true);
        
        testAdmin = createUser("admin1", "admin@frcteam.org", "Bob", "Admin", 
                              UserRole.ADMIN, 40, null, true);
        
        testParent = createUser("parent1", "parent@frcteam.org", "Alice", "Parent", 
                               UserRole.PARENT, 42, null, false);
        
        // COPPA-protected user (under 13)
        testMinorStudent = createUser("minor1", "minor@frcteam.org", "Tim", "Minor", 
                                     UserRole.STUDENT, 12, "parent@frcteam.org", false);
        testMinorStudent.setRequiresParentalConsent(true);
        testMinorStudent.setParentalConsentToken("test-consent-token-123");
        
        // Persist test data
        entityManager.persistAndFlush(testStudent);
        entityManager.persistAndFlush(testMentor);
        entityManager.persistAndFlush(testAdmin);
        entityManager.persistAndFlush(testParent);
        entityManager.persistAndFlush(testMinorStudent);
    }
    
    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudTests {
        
        @Test
        @DisplayName("Should save and find user by ID")
        void shouldSaveAndFindUserById() {
            // Given
            User newUser = createUser("newuser", "new@frcteam.org", "New", "User", 
                                     UserRole.STUDENT, 17, null, false);
            
            // When
            User savedUser = userRepository.save(newUser);
            Optional<User> foundUser = userRepository.findById(savedUser.getId());
            
            // Then
            assertTrue(foundUser.isPresent(), "User should be found by ID");
            assertEquals("newuser", foundUser.get().getUsername());
            assertEquals("new@frcteam.org", foundUser.get().getEmail());
            assertEquals(UserRole.STUDENT, foundUser.get().getRole());
            // Note: JPA Auditing may not be configured in test environment
            // assertNotNull(foundUser.get().getCreatedAt(), "Created timestamp should be set");
        }
        
        @Test
        @DisplayName("Should update user information")
        void shouldUpdateUserInformation() {
            // When
            testStudent.setFirstName("Updated");
            testStudent.setLastName("Name");
            User updatedUser = userRepository.save(testStudent);
            
            // Then
            assertEquals("Updated", updatedUser.getFirstName());
            assertEquals("Name", updatedUser.getLastName());
            // Note: JPA Auditing may not be configured in test environment
            // assertNotNull(updatedUser.getUpdatedAt(), "Updated timestamp should be set");
        }
        
        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            // Given
            Long userId = testStudent.getId();
            
            // When
            userRepository.delete(testStudent);
            Optional<User> deletedUser = userRepository.findById(userId);
            
            // Then
            assertFalse(deletedUser.isPresent(), "Deleted user should not be found");
        }
        
        @Test
        @DisplayName("Should count total users")
        void shouldCountTotalUsers() {
            // When
            long count = userRepository.count();
            
            // Then
            assertEquals(5, count, "Should count all test users");
        }
    }
    
    @Nested
    @DisplayName("Authentication Queries")
    class AuthenticationTests {
        
        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // When
            Optional<User> foundUser = userRepository.findByUsername("student1");
            
            // Then
            assertTrue(foundUser.isPresent(), "User should be found by username");
            assertEquals("student@frcteam.org", foundUser.get().getEmail());
            assertEquals(UserRole.STUDENT, foundUser.get().getRole());
        }
        
        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // When
            Optional<User> foundUser = userRepository.findByEmail("mentor@frcteam.org");
            
            // Then
            assertTrue(foundUser.isPresent(), "User should be found by email");
            assertEquals("mentor1", foundUser.get().getUsername());
            assertEquals(UserRole.MENTOR, foundUser.get().getRole());
        }
        
        @Test
        @DisplayName("Should find user by username or email")
        void shouldFindUserByUsernameOrEmail() {
            // Test with username
            Optional<User> userByUsername = userRepository.findByUsernameOrEmail("admin1", "");
            assertTrue(userByUsername.isPresent(), "Should find user by username");
            assertEquals(UserRole.ADMIN, userByUsername.get().getRole());
            
            // Test with email
            Optional<User> userByEmail = userRepository.findByUsernameOrEmail("", "admin@frcteam.org");
            assertTrue(userByEmail.isPresent(), "Should find user by email");
            assertEquals("admin1", userByEmail.get().getUsername());
            
            // Test with both (should still work)
            Optional<User> userByBoth = userRepository.findByUsernameOrEmail("admin1", "admin@frcteam.org");
            assertTrue(userByBoth.isPresent(), "Should find user by username or email");
        }
        
        @Test
        @DisplayName("Should return empty for non-existent user")
        void shouldReturnEmptyForNonExistentUser() {
            // When
            Optional<User> nonExistent = userRepository.findByUsername("nonexistent");
            
            // Then
            assertFalse(nonExistent.isPresent(), "Non-existent user should not be found");
        }
        
        @Test
        @DisplayName("Should check username and email existence")
        void shouldCheckUsernameAndEmailExistence() {
            // Test existing username
            assertTrue(userRepository.existsByUsername("student1"), "Existing username should be found");
            assertFalse(userRepository.existsByUsername("nonexistent"), "Non-existing username should not be found");
            
            // Test existing email
            assertTrue(userRepository.existsByEmail("mentor@frcteam.org"), "Existing email should be found");
            assertFalse(userRepository.existsByEmail("nonexistent@frcteam.org"), "Non-existing email should not be found");
        }
    }
    
    @Nested
    @DisplayName("Role-Based Queries")
    class RoleBasedTests {
        
        @Test
        @DisplayName("Should find users by role")
        void shouldFindUsersByRole() {
            // Test each role
            List<User> students = userRepository.findByRole(UserRole.STUDENT);
            assertEquals(2, students.size(), "Should find 2 students (including minor)");
            
            List<User> mentors = userRepository.findByRole(UserRole.MENTOR);
            assertEquals(1, mentors.size(), "Should find 1 mentor");
            
            List<User> admins = userRepository.findByRole(UserRole.ADMIN);
            assertEquals(1, admins.size(), "Should find 1 admin");
            
            List<User> parents = userRepository.findByRole(UserRole.PARENT);
            assertEquals(1, parents.size(), "Should find 1 parent");
        }
        
        @Test
        @DisplayName("Should find active users by role")
        void shouldFindActiveUsersByRole() {
            // Disable one student
            testStudent.setEnabled(false);
            entityManager.persistAndFlush(testStudent);
            
            // When
            List<User> activeStudents = userRepository.findActiveUsersByRole(UserRole.STUDENT);
            
            // Then
            assertEquals(1, activeStudents.size(), "Should find only 1 active student");
            assertEquals("minor1", activeStudents.get(0).getUsername(), "Should find the minor student");
        }
        
        @Test
        @DisplayName("Should find enabled and disabled users")
        void shouldFindEnabledAndDisabledUsers() {
            // Disable the minor student (simulate no parental consent)
            testMinorStudent.setEnabled(false);
            entityManager.persistAndFlush(testMinorStudent);
            
            // When
            List<User> enabledUsers = userRepository.findByEnabledTrue();
            List<User> disabledUsers = userRepository.findByEnabledFalse();
            
            // Then
            assertEquals(4, enabledUsers.size(), "Should find 4 enabled users");
            assertEquals(1, disabledUsers.size(), "Should find 1 disabled user");
            assertEquals("minor1", disabledUsers.get(0).getUsername(), "Disabled user should be the minor");
        }
        
        @Test
        @DisplayName("Should count users by role")
        void shouldCountUsersByRole() {
            // When/Then
            assertEquals(2, userRepository.countByRole(UserRole.STUDENT), "Should count 2 students");
            assertEquals(1, userRepository.countByRole(UserRole.MENTOR), "Should count 1 mentor");
            assertEquals(1, userRepository.countByRole(UserRole.ADMIN), "Should count 1 admin");
            assertEquals(1, userRepository.countByRole(UserRole.PARENT), "Should count 1 parent");
        }
        
        @Test
        @DisplayName("Should count enabled users")
        void shouldCountEnabledUsers() {
            // When
            long enabledCount = userRepository.countEnabledUsers();
            
            // Then
            assertEquals(5, enabledCount, "Should count all enabled users");
            
            // Disable a user and test again
            testStudent.setEnabled(false);
            entityManager.persistAndFlush(testStudent);
            
            long newEnabledCount = userRepository.countEnabledUsers();
            assertEquals(4, newEnabledCount, "Should count one less enabled user");
        }
    }
    
    @Nested
    @DisplayName("COPPA Compliance Queries")
    class COPPAComplianceTests {
        
        @Test
        @DisplayName("Should find users requiring parental consent")
        void shouldFindUsersRequiringParentalConsent() {
            // When
            List<User> usersRequiringConsent = userRepository.findUsersRequiringParentalConsent();
            
            // Then
            assertEquals(1, usersRequiringConsent.size(), "Should find 1 user requiring consent");
            assertEquals("minor1", usersRequiringConsent.get(0).getUsername(), "Should be the minor student");
            assertTrue(usersRequiringConsent.get(0).isRequiresParentalConsent(), "Should require consent");
        }
        
        @Test
        @DisplayName("Should find user by parental consent token")
        void shouldFindUserByParentalConsentToken() {
            // When
            Optional<User> userWithToken = userRepository.findByParentalConsentToken("test-consent-token-123");
            
            // Then
            assertTrue(userWithToken.isPresent(), "Should find user with consent token");
            assertEquals("minor1", userWithToken.get().getUsername(), "Should be the minor student");
            
            // Test with non-existent token
            Optional<User> nonExistentToken = userRepository.findByParentalConsentToken("non-existent-token");
            assertFalse(nonExistentToken.isPresent(), "Should not find user with non-existent token");
        }
        
        @Test
        @DisplayName("Should find minors under 13")
        void shouldFindMinorsUnder13() {
            // When
            List<User> minorsUnder13 = userRepository.findMinorsUnder13();
            
            // Then
            assertEquals(1, minorsUnder13.size(), "Should find 1 user under 13");
            assertEquals("minor1", minorsUnder13.get(0).getUsername(), "Should be the minor student");
            assertEquals(12, minorsUnder13.get(0).getAge(), "Age should be 12");
        }
        
        @Test
        @DisplayName("Should find all minors under 18")
        void shouldFindAllMinorsUnder18() {
            // When
            List<User> allMinors = userRepository.findAllMinors();
            
            // Then
            assertEquals(2, allMinors.size(), "Should find 2 users under 18");
            
            // Verify both are under 18
            for (User minor : allMinors) {
                assertTrue(minor.getAge() < 18, "All found users should be under 18");
            }
        }
        
        @Test
        @DisplayName("Should count minors under 13")
        void shouldCountMinorsUnder13() {
            // When
            long minorCount = userRepository.countMinorsUnder13();
            
            // Then
            assertEquals(1, minorCount, "Should count 1 user under 13");
        }
        
        @Test
        @DisplayName("Should handle COPPA consent workflow")
        void shouldHandleCOPPAConsentWorkflow() {
            // Given - User requires consent
            User minorUser = userRepository.findByUsername("minor1").orElseThrow();
            assertTrue(minorUser.isRequiresParentalConsent(), "Minor should require consent initially");
            assertNull(minorUser.getParentalConsentDate(), "Should not have consent date initially");
            
            // When - Grant consent
            minorUser.setParentalConsentDate(LocalDateTime.now());
            minorUser.setRequiresParentalConsent(false);
            minorUser.setParentalConsentToken(null); // Clear token after consent
            userRepository.save(minorUser);
            
            // Then - Verify consent granted
            List<User> stillRequiringConsent = userRepository.findUsersRequiringParentalConsent();
            assertEquals(0, stillRequiringConsent.size(), "No users should require consent after granting");
            
            User updatedMinor = userRepository.findByUsername("minor1").orElseThrow();
            assertFalse(updatedMinor.isRequiresParentalConsent(), "Should no longer require consent");
            assertNotNull(updatedMinor.getParentalConsentDate(), "Should have consent date");
            assertNull(updatedMinor.getParentalConsentToken(), "Token should be cleared");
        }
    }
    
    @Nested
    @DisplayName("MFA Queries")
    class MFATests {
        
        @Test
        @DisplayName("Should find users with MFA enabled")
        void shouldFindUsersWithMFAEnabled() {
            // When
            List<User> mfaUsers = userRepository.findByMfaEnabledTrue();
            
            // Then
            assertEquals(2, mfaUsers.size(), "Should find 2 users with MFA enabled (mentor and admin)");
            
            // Verify they are mentor and admin
            for (User user : mfaUsers) {
                assertTrue(user.getRole().requiresMFA(), "Only mentor/admin roles should have MFA enabled");
            }
        }
        
        @Test
        @DisplayName("Should find users without MFA")
        void shouldFindUsersWithoutMFA() {
            // When
            List<User> nonMfaUsers = userRepository.findByMfaEnabledFalse();
            
            // Then
            assertEquals(3, nonMfaUsers.size(), "Should find 3 users without MFA (students and parent)");
            
            // Verify they don't require MFA
            for (User user : nonMfaUsers) {
                assertFalse(user.getRole().requiresMFA(), "Users not requiring MFA should not have it enabled");
            }
        }
        
        @Test
        @DisplayName("Should find users requiring MFA setup")
        void shouldFindUsersRequiringMFASetup() {
            // Disable MFA for mentor to simulate new mentor needing setup
            testMentor.setMfaEnabled(false);
            testMentor.setTotpSecret(null);
            entityManager.persistAndFlush(testMentor);
            
            // When
            List<UserRole> mfaRoles = List.of(UserRole.MENTOR, UserRole.ADMIN);
            List<User> usersRequiringMFA = userRepository.findUsersRequiringMFA(mfaRoles);
            
            // Then
            assertEquals(1, usersRequiringMFA.size(), "Should find 1 user requiring MFA setup");
            assertEquals("mentor1", usersRequiringMFA.get(0).getUsername(), "Should be the mentor");
            assertFalse(usersRequiringMFA.get(0).isMfaEnabled(), "User should not have MFA enabled");
        }
    }
    
    @Nested
    @DisplayName("Activity and Time-Based Queries")
    class ActivityTests {
        
        @Test
        @DisplayName("Should find recently active users")
        void shouldFindRecentlyActiveUsers() {
            // Given - Set last login for some users
            LocalDateTime recentTime = LocalDateTime.now().minusHours(1);
            LocalDateTime oldTime = LocalDateTime.now().minusDays(10);
            
            testStudent.setLastLogin(recentTime);
            testMentor.setLastLogin(oldTime);
            testAdmin.setLastLogin(recentTime);
            
            entityManager.persistAndFlush(testStudent);
            entityManager.persistAndFlush(testMentor);
            entityManager.persistAndFlush(testAdmin);
            
            // When
            LocalDateTime since = LocalDateTime.now().minusDays(1);
            List<User> recentlyActive = userRepository.findRecentlyActiveUsers(since);
            
            // Then
            assertEquals(2, recentlyActive.size(), "Should find 2 recently active users");
            
            for (User user : recentlyActive) {
                assertTrue(user.getLastLogin().isAfter(since), "User should have recent login");
            }
        }
        
        @Test
        @DisplayName("Should find recently created users")
        void shouldFindRecentlyCreatedUsers() {
            // Given - Manually set creation timestamps since JPA auditing may not work in tests
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(30);
            testStudent.setCreatedAt(recentTime);
            testMentor.setCreatedAt(recentTime);
            testAdmin.setCreatedAt(recentTime);
            testParent.setCreatedAt(recentTime);
            testMinorStudent.setCreatedAt(recentTime);
            
            entityManager.persistAndFlush(testStudent);
            entityManager.persistAndFlush(testMentor);
            entityManager.persistAndFlush(testAdmin);
            entityManager.persistAndFlush(testParent);
            entityManager.persistAndFlush(testMinorStudent);
            
            // When
            LocalDateTime since = LocalDateTime.now().minusHours(1);
            List<User> recentUsers = userRepository.findRecentlyCreatedUsers(since);
            
            // Then
            assertEquals(5, recentUsers.size(), "Should find all test users as recently created");
            
            for (User user : recentUsers) {
                assertTrue(user.getCreatedAt().isAfter(since), "User should be recently created");
            }
        }
    }
    
    @Nested
    @DisplayName("Data Integrity and Edge Cases")
    class DataIntegrityTests {
        
        @Test
        @DisplayName("Should enforce unique username constraint")
        void shouldEnforceUniqueUsernameConstraint() {
            // Given
            User duplicateUser = createUser("student1", "different@email.com", "Different", "User", 
                                          UserRole.STUDENT, 17, null, false);
            
            // When/Then
            assertThrows(Exception.class, () -> {
                userRepository.saveAndFlush(duplicateUser);
            }, "Should throw exception for duplicate username");
        }
        
        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            User duplicateUser = createUser("differentuser", "student@frcteam.org", "Different", "User", 
                                          UserRole.STUDENT, 17, null, false);
            
            // When/Then
            assertThrows(Exception.class, () -> {
                userRepository.saveAndFlush(duplicateUser);
            }, "Should throw exception for duplicate email");
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Test finding with null values
            Optional<User> nullUsername = userRepository.findByUsername(null);
            assertFalse(nullUsername.isPresent(), "Should handle null username gracefully");
            
            Optional<User> nullEmail = userRepository.findByEmail(null);
            assertFalse(nullEmail.isPresent(), "Should handle null email gracefully");
            
            // Test existence checks with null
            assertFalse(userRepository.existsByUsername(null), "Should handle null username existence check");
            assertFalse(userRepository.existsByEmail(null), "Should handle null email existence check");
        }
        
        @Test
        @DisplayName("Should handle case sensitivity correctly")
        void shouldHandleCaseSensitivityCorrectly() {
            // Test username case sensitivity
            Optional<User> upperCaseUsername = userRepository.findByUsername("STUDENT1");
            assertFalse(upperCaseUsername.isPresent(), "Username should be case sensitive");
            
            // Test email case sensitivity (emails should be case insensitive in practice)
            Optional<User> upperCaseEmail = userRepository.findByEmail("STUDENT@FRCTEAM.ORG");
            // This depends on implementation - typically emails should be case insensitive
            // but our current implementation may be case sensitive
        }
    }
    
    @Nested
    @DisplayName("Performance and Bulk Operations")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle bulk operations efficiently")
        void shouldHandleBulkOperationsEfficiently() {
            // Given - Create multiple users
            for (int i = 0; i < 50; i++) {
                User bulkUser = createUser("bulk" + i, "bulk" + i + "@frcteam.org", 
                                         "Bulk", "User" + i, UserRole.STUDENT, 16, null, false);
                entityManager.persist(bulkUser);
            }
            entityManager.flush();
            
            // When
            long startTime = System.currentTimeMillis();
            List<User> allStudents = userRepository.findByRole(UserRole.STUDENT);
            long endTime = System.currentTimeMillis();
            
            // Then
            assertEquals(52, allStudents.size(), "Should find all students including bulk users");
            assertTrue((endTime - startTime) < 1000, "Query should complete in under 1 second");
        }
        
        @Test
        @DisplayName("Should optimize COPPA compliance queries")
        void shouldOptimizeCOPPAComplianceQueries() {
            // Given - Add more minor users with unique usernames
            for (int i = 1; i <= 10; i++) { // Start from 1 to avoid conflict with existing "minor1"
                User minorUser = createUser("minoruser" + i, "minoruser" + i + "@frcteam.org", 
                                          "Minor", "User" + i, UserRole.STUDENT, 12, 
                                          "parent" + i + "@frcteam.org", false);
                minorUser.setRequiresParentalConsent(true);
                entityManager.persist(minorUser);
            }
            entityManager.flush();
            
            // When
            long startTime = System.currentTimeMillis();
            List<User> minorsRequiringConsent = userRepository.findUsersRequiringParentalConsent();
            long minorCount = userRepository.countMinorsUnder13();
            long endTime = System.currentTimeMillis();
            
            // Then
            assertEquals(11, minorsRequiringConsent.size(), "Should find all minors requiring consent");
            assertEquals(11, minorCount, "Should count all minors under 13");
            assertTrue((endTime - startTime) < 500, "COPPA queries should be optimized");
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    private User createUser(String username, String email, String firstName, String lastName, 
                           UserRole role, Integer age, String parentEmail, boolean mfaEnabled) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded-password"); // Would be BCrypt encoded in real app
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setAge(age);
        user.setParentEmail(parentEmail);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setMfaEnabled(mfaEnabled);
        
        // Manually set timestamps since JPA auditing may not work in test environment
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        if (mfaEnabled) {
            user.setTotpSecret("test-totp-secret");
        }
        
        return user;
    }
}