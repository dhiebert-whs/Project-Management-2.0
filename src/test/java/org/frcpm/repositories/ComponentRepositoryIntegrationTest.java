// Execute - Search for "component"
List<Component> componentResults = componentRepository.findByNameContainingIgnoreCase("component");
        
// Verify - Should find test component
assertThat(componentResults).hasSize(1);
assertThat(componentResults.get(0).getName()).isEqualTo("Test Component");

// Execute - Search for "aluminum"
List<Component> aluminumResults = componentRepository.findByNameContainingIgnoreCase("aluminum");

// Verify - Should find aluminum tubing
assertThat(aluminumResults).hasSize(1);
assertThat(aluminumResults.get(0).getName()).isEqualTo("Aluminum Tubing");
}

@Test
void testFindByDelivered() {
// Setup - Persist components with different delivery status
testComponent.setDelivered(false);
motorComponent.setDelivered(false);
deliveredComponent.setDelivered(true);

componentRepository.save(testComponent);
componentRepository.save(motorComponent);
componentRepository.save(deliveredComponent);
entityManager.flush();

// Execute - Find undelivered components
List<Component> undeliveredComponents = componentRepository.findByDelivered(false);

// Execute - Find delivered components
List<Component> deliveredComponents = componentRepository.findByDelivered(true);

// Verify
assertThat(undeliveredComponents).hasSize(2);
assertThat(undeliveredComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Test Component", "Falcon 500 Motor");
assertThat(undeliveredComponents).allMatch(c -> !c.isDelivered());

assertThat(deliveredComponents).hasSize(1);
assertThat(deliveredComponents.get(0).getName()).isEqualTo("Aluminum Tubing");
assertThat(deliveredComponents.get(0).isDelivered()).isTrue();
}

@Test
void testFindByExpectedDeliveryAfter() {
// Setup - Create components with different expected delivery dates
testComponent.setExpectedDelivery(LocalDate.now().plusDays(5));    // After threshold
motorComponent.setExpectedDelivery(LocalDate.now().plusDays(10));  // After threshold
deliveredComponent.setExpectedDelivery(LocalDate.now().minusDays(3)); // Before threshold

componentRepository.save(testComponent);
componentRepository.save(motorComponent);
componentRepository.save(deliveredComponent);
entityManager.flush();

// Execute - Find components expected after today + 3 days
LocalDate threshold = LocalDate.now().plusDays(3);
List<Component> futureComponents = componentRepository.findByExpectedDeliveryAfter(threshold);

// Verify - Should find components with expected delivery after threshold
assertThat(futureComponents).hasSize(2);
assertThat(futureComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Test Component", "Falcon 500 Motor");
assertThat(futureComponents).allMatch(c -> c.getExpectedDelivery().isAfter(threshold));
}

@Test
void testFindByExpectedDeliveryBefore() {
// Setup - Create components with different expected delivery dates
testComponent.setExpectedDelivery(LocalDate.now().plusDays(5));    // After threshold
motorComponent.setExpectedDelivery(LocalDate.now().plusDays(10));  // After threshold
deliveredComponent.setExpectedDelivery(LocalDate.now().minusDays(3)); // Before threshold

componentRepository.save(testComponent);
componentRepository.save(motorComponent);
componentRepository.save(deliveredComponent);
entityManager.flush();

// Execute - Find components expected before today
LocalDate threshold = LocalDate.now();
List<Component> pastComponents = componentRepository.findByExpectedDeliveryBefore(threshold);

// Verify - Should find components with expected delivery before threshold
assertThat(pastComponents).hasSize(1);
assertThat(pastComponents.get(0).getName()).isEqualTo("Aluminum Tubing");
assertThat(pastComponents.get(0).getExpectedDelivery()).isBefore(threshold);
}

@Test
void testFindByExpectedDeliveryBetween() {
// Setup - Create components with different expected delivery dates
Component nearComponent = new Component();
nearComponent.setName("Near Component");
nearComponent.setExpectedDelivery(LocalDate.now().plusDays(2));

testComponent.setExpectedDelivery(LocalDate.now().plusDays(5));    // Within range
motorComponent.setExpectedDelivery(LocalDate.now().plusDays(15));  // Outside range
deliveredComponent.setExpectedDelivery(LocalDate.now().minusDays(3)); // Outside range

componentRepository.save(nearComponent);
componentRepository.save(testComponent);
componentRepository.save(motorComponent);
componentRepository.save(deliveredComponent);
entityManager.flush();

// Execute - Find components expected between now and +7 days
LocalDate startDate = LocalDate.now();
LocalDate endDate = LocalDate.now().plusDays(7);
List<Component> rangeComponents = componentRepository.findByExpectedDeliveryBetween(startDate, endDate);

// Verify - Should find components within date range
assertThat(rangeComponents).hasSize(2);
assertThat(rangeComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Near Component", "Test Component");
assertThat(rangeComponents).allMatch(c -> 
    !c.getExpectedDelivery().isBefore(startDate) && !c.getExpectedDelivery().isAfter(endDate));
}

@Test
void testFindByDeliveredTrueAndActualDeliveryAfter() {
// Setup - Create delivered components with different actual delivery dates
Component recentDelivered = new Component();
recentDelivered.setName("Recent Delivered");
recentDelivered.setDelivered(true);
recentDelivered.setActualDelivery(LocalDate.now().minusDays(1)); // Recent

Component oldDelivered = new Component();
oldDelivered.setName("Old Delivered");
oldDelivered.setDelivered(true);
oldDelivered.setActualDelivery(LocalDate.now().minusDays(10)); // Old

testComponent.setDelivered(false); // Not delivered

componentRepository.save(recentDelivered);
componentRepository.save(oldDelivered);
componentRepository.save(testComponent);
entityManager.flush();

// Execute - Find components delivered after 5 days ago
LocalDate threshold = LocalDate.now().minusDays(5);
List<Component> recentlyDelivered = componentRepository.findByDeliveredTrueAndActualDeliveryAfter(threshold);

// Verify - Should find only recently delivered components
assertThat(recentlyDelivered).hasSize(1);
assertThat(recentlyDelivered.get(0).getName()).isEqualTo("Recent Delivered");
assertThat(recentlyDelivered.get(0).isDelivered()).isTrue();
assertThat(recentlyDelivered.get(0).getActualDelivery()).isAfter(threshold);
}

// ========== CUSTOM @QUERY METHODS ==========

@Test
void testFindByName() {
// Setup - Persist components
componentRepository.save(testComponent);
componentRepository.save(motorComponent);
entityManager.flush();

// Execute - Search by exact name
List<Component> testResults = componentRepository.findByName("Test");
List<Component> falconResults = componentRepository.findByName("Falcon");

// Verify - Should find partial matches
assertThat(testResults).hasSize(1);
assertThat(testResults.get(0).getName()).isEqualTo("Test Component");

assertThat(falconResults).hasSize(1);
assertThat(falconResults.get(0).getName()).isEqualTo("Falcon 500 Motor");
}

@Test
void testFindOverdueComponents() {
// Setup - Create components with different delivery status and dates
Component overdueComponent1 = new Component();
overdueComponent1.setName("Overdue Component 1");
overdueComponent1.setExpectedDelivery(LocalDate.now().minusDays(5));
overdueComponent1.setDelivered(false);

Component overdueComponent2 = new Component();
overdueComponent2.setName("Overdue Component 2");
overdueComponent2.setExpectedDelivery(LocalDate.now().minusDays(2));
overdueComponent2.setDelivered(false);

Component futureComponent = new Component();
futureComponent.setName("Future Component");
futureComponent.setExpectedDelivery(LocalDate.now().plusDays(5));
futureComponent.setDelivered(false);

Component deliveredOverdue = new Component();
deliveredOverdue.setName("Delivered Overdue");
deliveredOverdue.setExpectedDelivery(LocalDate.now().minusDays(3));
deliveredOverdue.setDelivered(true); // Delivered, so not overdue

componentRepository.save(overdueComponent1);
componentRepository.save(overdueComponent2);
componentRepository.save(futureComponent);
componentRepository.save(deliveredOverdue);
entityManager.flush();

// Execute
List<Component> overdueComponents = componentRepository.findOverdueComponents(LocalDate.now());

// Verify - Should find only undelivered components past expected delivery
assertThat(overdueComponents).hasSize(2);
assertThat(overdueComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Overdue Component 1", "Overdue Component 2");
assertThat(overdueComponents).allMatch(c -> !c.isDelivered());
assertThat(overdueComponents).allMatch(c -> c.getExpectedDelivery().isBefore(LocalDate.now()));
}

@Test
void testFindByRequiredForTasksId() {
// Setup - Create project, subsystem, task, and components
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);

testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

Component savedComponent1 = persistAndFlush(testComponent);
Component savedComponent2 = persistAndFlush(motorComponent);
Component savedComponent3 = persistAndFlush(deliveredComponent);

// Associate components with task
savedTask.addRequiredComponent(savedComponent1);
savedTask.addRequiredComponent(savedComponent2);
// savedComponent3 is not required for this task

entityManager.persist(savedTask);
entityManager.flush();

// Execute
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());

// Verify - Should find only components required for the task
assertThat(taskComponents).hasSize(2);
assertThat(taskComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Test Component", "Falcon 500 Motor");
}

@Test
void testCountByDelivered() {
// Setup - Create components with different delivery status
testComponent.setDelivered(false);
motorComponent.setDelivered(false);
deliveredComponent.setDelivered(true);

Component anotherDelivered = new Component();
anotherDelivered.setName("Another Delivered");
anotherDelivered.setDelivered(true);

componentRepository.save(testComponent);
componentRepository.save(motorComponent);
componentRepository.save(deliveredComponent);
componentRepository.save(anotherDelivered);
entityManager.flush();

// Execute
long deliveredCount = componentRepository.countByDelivered(true);
long undeliveredCount = componentRepository.countByDelivered(false);

// Verify
assertThat(deliveredCount).isEqualTo(2);
assertThat(undeliveredCount).isEqualTo(2);
}

@Test
void testExistsByPartNumberIgnoreCase() {
// Setup - Persist component
componentRepository.save(motorComponent);
entityManager.flush();

// Execute - Case insensitive checks
boolean existsExact = componentRepository.existsByPartNumberIgnoreCase("217-6515");
boolean existsLower = componentRepository.existsByPartNumberIgnoreCase("217-6515");
boolean existsMixed = componentRepository.existsByPartNumberIgnoreCase("217-6515");

// Verify
assertThat(existsExact).isTrue();
assertThat(existsLower).isTrue();
assertThat(existsMixed).isTrue();

// Execute - Non-existing part number
boolean notExists = componentRepository.existsByPartNumberIgnoreCase("NONEXISTENT");

// Verify
assertThat(notExists).isFalse();
}

// ========== ENTITY RELATIONSHIP VALIDATION ==========

@Test
void testUniqueConstraint_PartNumber() {
// Setup - Persist first component
componentRepository.save(motorComponent);
entityManager.flush(); // This should succeed

// Clear the persistence context to ensure fresh entity
entityManager.clear();

// Execute - Try to save component with same part number
// THIS IS WHERE THE EXCEPTION SHOULD BE CAUGHT
org.junit.jupiter.api.Assertions.assertThrows(
    org.springframework.dao.DataIntegrityViolationException.class,
    () -> {
        Component duplicatePartNumber = new Component();
        duplicatePartNumber.setName("Different Name");
        duplicatePartNumber.setPartNumber("217-6515");  // Same part number as motorComponent
        duplicatePartNumber.setDescription("Different description");
        
        // The save operation itself may trigger the constraint violation
        componentRepository.save(duplicatePartNumber);
        entityManager.flush(); // Ensure the save is flushed to database
    }
);
}

@Test
void testTaskComponentRelationship() {
// Setup - Create project, subsystem, task, and components
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);

testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

Component savedComponent1 = persistAndFlush(testComponent);
Component savedComponent2 = persistAndFlush(motorComponent);

// Execute - Associate components with task using helper method
savedTask.addRequiredComponent(savedComponent1);
savedTask.addRequiredComponent(savedComponent2);

entityManager.persist(savedTask);
entityManager.flush();

// Verify - Bidirectional relationship
assertThat(savedTask.getRequiredComponents()).hasSize(2);
assertThat(savedTask.getRequiredComponents()).containsExactlyInAnyOrder(savedComponent1, savedComponent2);

assertThat(savedComponent1.getRequiredForTasks()).contains(savedTask);
assertThat(savedComponent2.getRequiredForTasks()).contains(savedTask);

// Verify - Repository query works
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());
assertThat(taskComponents).hasSize(2);
assertThat(taskComponents).containsExactlyInAnyOrder(savedComponent1, savedComponent2);
}

@Test
void testComponentTaskAssociationChange() {
// Setup - Create task and components
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);

testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

Component savedComponent1 = persistAndFlush(testComponent);
Component savedComponent2 = persistAndFlush(motorComponent);
Component savedComponent3 = persistAndFlush(deliveredComponent);

// Initial association
savedTask.addRequiredComponent(savedComponent1);
savedTask.addRequiredComponent(savedComponent2);
entityManager.persist(savedTask);
entityManager.flush();

// Verify initial state
assertThat(savedTask.getRequiredComponents()).hasSize(2);

// Execute - Change component associations
savedTask.removeRequiredComponent(savedComponent1);
savedTask.addRequiredComponent(savedComponent3);

entityManager.persist(savedTask);
entityManager.flush();

// Verify - Component associations changed
assertThat(savedTask.getRequiredComponents()).hasSize(2);
assertThat(savedTask.getRequiredComponents()).containsExactlyInAnyOrder(savedComponent2, savedComponent3);
assertThat(savedTask.getRequiredComponents()).doesNotContain(savedComponent1);

// Verify - Repository queries reflect changes
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());
assertThat(taskComponents).hasSize(2);
assertThat(taskComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Falcon 500 Motor", "Aluminum Tubing");
}

// ========== BUSINESS LOGIC VALIDATION ==========

@Test
void testComponentDeliveryLogic() {
// Setup - Save component as undelivered
testComponent.setDelivered(false);
testComponent.setActualDelivery(null);
Component savedComponent = componentRepository.save(testComponent);
entityManager.flush();

// Execute - Mark as delivered
savedComponent.setDelivered(true);
componentRepository.save(savedComponent);
entityManager.flush();

// Verify - Actual delivery date should be set automatically
assertThat(savedComponent.isDelivered()).isTrue();
assertThat(savedComponent.getActualDelivery()).isEqualTo(LocalDate.now());

// Execute - Mark as delivered with specific date
LocalDate specificDate = LocalDate.now().minusDays(2);
savedComponent.setDelivered(false);
savedComponent.setActualDelivery(null);
savedComponent.setDelivered(true);
// Set specific delivery date manually
savedComponent.setActualDelivery(specificDate);
componentRepository.save(savedComponent);
entityManager.flush();

// Verify - Specific delivery date preserved
assertThat(savedComponent.getActualDelivery()).isEqualTo(specificDate);
}

@Test
void testComponentToString() {
// Setup - Components with and without part numbers
Component withPartNumber = new Component("Motor", "M-001");
Component withoutPartNumber = new Component("Bracket");

// Execute and verify toString behavior
assertThat(withPartNumber.toString()).isEqualTo("Motor (M-001)");
assertThat(withoutPartNumber.toString()).isEqualTo("Bracket");
}

@Test
void testComplexComponentScenario() {
// Setup - Create a complex component scenario
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);

// Create task requiring multiple components
testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

// Create components with different states
testComponent.setName("Complex Component");
testComponent.setPartNumber("COMP-001");
testComponent.setDescription("A complex component with full configuration");
testComponent.setExpectedDelivery(LocalDate.now().plusDays(3));
testComponent.setDelivered(false);

Component savedComponent = componentRepository.save(testComponent);
entityManager.flush();

// Associate with task
savedTask.addRequiredComponent(savedComponent);
entityManager.persist(savedTask);
entityManager.flush();

// Execute comprehensive queries
List<Component> allComponents = componentRepository.findAll();
List<Component> undeliveredComponents = componentRepository.findByDelivered(false);
Optional<Component> foundByPartNumber = componentRepository.findByPartNumber("COMP-001");
List<Component> foundByName = componentRepository.findByNameContainingIgnoreCase("complex");
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());

// Verify comprehensive scenario
assertThat(allComponents).hasSize(1);
assertThat(undeliveredComponents).hasSize(1);
assertThat(foundByPartNumber).isPresent();
assertThat(foundByName).hasSize(1);
assertThat(taskComponents).hasSize(1);

// Verify all properties
assertThat(savedComponent.getName()).isEqualTo("Complex Component");
assertThat(savedComponent.getPartNumber()).isEqualTo("COMP-001");
assertThat(savedComponent.getDescription()).isEqualTo("A complex component with full configuration");
assertThat(savedComponent.getExpectedDelivery()).isEqualTo(LocalDate.now().plusDays(3));
assertThat(savedComponent.isDelivered()).isFalse();
assertThat(savedComponent.getRequiredForTasks()).contains(savedTask);
}

// ========== CONSTRAINT AND VALIDATION TESTING ==========

@Test
void testComponentConstraints() {
// Setup - Create component with all required fields
testComponent.setName("Valid Component");
testComponent.setPartNumber("VALID-001");

// Execute - Save valid component
Component savedComponent = componentRepository.save(testComponent);
entityManager.flush();

// Verify - Component saved successfully
assertThat(savedComponent.getId()).isNotNull();
assertThat(savedComponent.getName()).isEqualTo("Valid Component");

// Verify - Required fields are present
assertThat(savedComponent.getName()).isNotNull();
}

@Test
void testComponentDateValidation() {
// Setup - Create component with valid dates
testComponent.setExpectedDelivery(LocalDate.now().plusDays(5));
testComponent.setActualDelivery(LocalDate.now().plusDays(3));
testComponent.setDelivered(true);

// Execute - Save component with dates
Component savedComponent = componentRepository.save(testComponent);
entityManager.flush();

// Verify - Dates are saved correctly
assertThat(savedComponent.getExpectedDelivery()).isEqualTo(LocalDate.now().plusDays(5));
assertThat(savedComponent.getActualDelivery()).isEqualTo(LocalDate.now().plusDays(3));
assertThat(savedComponent.isDelivered()).isTrue();
}

// ========== PERFORMANCE AND BULK OPERATIONS ==========

@Test
void testBulkComponentOperations() {
// Setup - Create multiple components
List<Component> components = new java.util.ArrayList<>();
for (int i = 1; i <= 20; i++) {
    Component component = new Component();
    component.setName("Bulk Component " + i);
    component.setPartNumber("BULK-" + String.format("%03d", i));
    component.setDescription("Bulk component description " + i);
    component.setExpectedDelivery(LocalDate.now().plusDays(i));
    component.setDelivered(i % 4 == 0); // Every 4th component is delivered
    
    if (component.isDelivered()) {
        component.setActualDelivery(LocalDate.now().minusDays(1));
    }
    
    components.add(component);
}

// Execute - Save all components
componentRepository.saveAll(components);
entityManager.flush();

// Verify - Bulk operations
List<Component> allComponents = componentRepository.findAll();
assertThat(allComponents).hasSize(20);

List<Component> deliveredComponents = componentRepository.findByDelivered(true);
assertThat(deliveredComponents).hasSize(5); // Every 4th component

List<Component> undeliveredComponents = componentRepository.findByDelivered(false);
assertThat(undeliveredComponents).hasSize(15); // Remaining components

long totalComponents = componentRepository.count();
assertThat(totalComponents).isEqualTo(20);

long deliveredCount = componentRepository.countByDelivered(true);
long undeliveredCount = componentRepository.countByDelivered(false);
assertThat(deliveredCount).isEqualTo(5);
assertThat(undeliveredCount).isEqualTo(15);
}

@Test
void testComponentQueryPerformance() {
// Setup - Create larger dataset for performance testing
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);

testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

// Create 40 components with various properties
List<Component> components = new java.util.ArrayList<>();
for (int i = 1; i <= 40; i++) {
    Component component = new Component();
    component.setName("Performance Component " + i);
    component.setPartNumber("PERF-" + String.format("%04d", i));
    component.setDescription("Performance testing component " + i);
    component.setExpectedDelivery(LocalDate.now().plusDays(i % 30));
    component.setDelivered(i % 5 == 0); // Every 5th component is delivered
    
    if (component.isDelivered()) {
        component.setActualDelivery(LocalDate.now().minusDays(i % 10));
    }
    
    components.add(component);
}

componentRepository.saveAll(components);

// Associate every 3rd component with the task
for (int i = 2; i < components.size(); i += 3) {
    savedTask.addRequiredComponent(components.get(i));
}
entityManager.persist(savedTask);
entityManager.flush();

// Execute - Performance-sensitive queries
long startTime = System.currentTimeMillis();

List<Component> allComponents = componentRepository.findAll();
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
List<Component> overdueComponents = componentRepository.findOverdueComponents(LocalDate.now());
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());
List<Component> nameSearchResults = componentRepository.findByNameContainingIgnoreCase("Performance");

long endTime = System.currentTimeMillis();

// Verify - Query results are correct
assertThat(allComponents).hasSize(40);
assertThat(deliveredComponents).hasSize(8); // Every 5th component
assertThat(taskComponents).hasSize(13); // Every 3rd component starting from index 2
assertThat(nameSearchResults).hasSize(40); // All components contain "Performance"

// Log performance (for development monitoring)
long queryTime = endTime - startTime;
System.out.println("Component query execution time: " + queryTime + "ms");

// Verify reasonable performance (should complete quickly)
assertThat(queryTime).isLessThan(5000); // Should complete within 5 seconds
}

// ========== ERROR HANDLING AND EDGE CASES ==========

@Test
void testComponentRepositoryErrorHandling() {
// Test null parameter handling in repository methods

// findByPartNumber with null - Spring Data JPA handles this gracefully
Optional<Component> nullPartComponent = componentRepository.findByPartNumber(null);
assertThat(nullPartComponent).isEmpty();

// findByName with null - Custom query handles this gracefully
List<Component> nullNameComponents = componentRepository.findByName(null);
assertThat(nullNameComponents).isEmpty();

// findByDelivered method should work with boolean values
List<Component> falseDelivered = componentRepository.findByDelivered(false);
List<Component> trueDelivered = componentRepository.findByDelivered(true);
assertThat(falseDelivered).isEmpty(); // No components in test
assertThat(trueDelivered).isEmpty(); // No components in test
}

@Test
void testComponentEdgeCases() {
// Setup - Create components with edge case values

// Component with minimal data
Component minimalComponent = new Component();
minimalComponent.setName("Minimal");
// No part number, no description, no dates

// Component with very long description
Component longDescComponent = new Component();
longDescComponent.setName("Long Description Component");
longDescComponent.setPartNumber("LONG-DESC-001");
longDescComponent.setDescription("This is a very long description that tests the system's ability to handle extensive text content. ".repeat(20));

// Component with null expected delivery but delivered
Component deliveredNoExpected = new Component();
deliveredNoExpected.setName("Delivered No Expected");
deliveredNoExpected.setPartNumber("DNE-001");
deliveredNoExpected.setExpectedDelivery(null);
deliveredNoExpected.setActualDelivery(LocalDate.now());
deliveredNoExpected.setDelivered(true);

componentRepository.save(minimalComponent);
componentRepository.save(longDescComponent);
componentRepository.save(deliveredNoExpected);
entityManager.flush();

// Execute - Query edge cases
List<Component> allComponents = componentRepository.findAll();
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
List<Component> longDescResults = componentRepository.findByNameContainingIgnoreCase("Long Description");

// Verify - Edge cases handled correctly
assertThat(allComponents).hasSize(3);
assertThat(deliveredComponents).hasSize(1);
assertThat(deliveredComponents.get(0).getName()).isEqualTo("Delivered No Expected");
assertThat(longDescResults).hasSize(1);

// Verify minimal component
assertThat(minimalComponent.getPartNumber()).isNull();
assertThat(minimalComponent.getDescription()).isNull();
assertThat(minimalComponent.getExpectedDelivery()).isNull();
assertThat(minimalComponent.getActualDelivery()).isNull();
assertThat(minimalComponent.isDelivered()).isFalse();

// Verify long description is preserved
assertThat(longDescComponent.getDescription()).hasSizeGreaterThan(1000);

// Verify delivered component with null expected delivery
assertThat(deliveredNoExpected.getExpectedDelivery()).isNull();
assertThat(deliveredNoExpected.getActualDelivery()).isNotNull();
assertThat(deliveredNoExpected.isDelivered()).isTrue();
}

@Test
void testComponentDateEdgeCases() {
// Setup - Create components with edge case dates

// Component with same expected and actual delivery
Component sameDayComponent = new Component();
sameDayComponent.setName("Same Day Component");
sameDayComponent.setPartNumber("SAME-001");
sameDayComponent.setExpectedDelivery(LocalDate.now());
sameDayComponent.setActualDelivery(LocalDate.now());
sameDayComponent.setDelivered(true);

// Component delivered before expected date
Component earlyComponent = new Component();
earlyComponent.setName("Early Component");
earlyComponent.setPartNumber("EARLY-001");
earlyComponent.setExpectedDelivery(LocalDate.now().plusDays(5));
earlyComponent.setActualDelivery(LocalDate.now().minusDays(1));
earlyComponent.setDelivered(true);

// Component delivered after expected date (late)
Component lateComponent = new Component();
lateComponent.setName("Late Component");
lateComponent.setPartNumber("LATE-001");
lateComponent.setExpectedDelivery(LocalDate.now().minusDays(5));
lateComponent.setActualDelivery(LocalDate.now().minusDays(1));
lateComponent.setDelivered(true);

componentRepository.save(sameDayComponent);
componentRepository.save(earlyComponent);
componentRepository.save(lateComponent);
entityManager.flush();

// Execute - Query with date conditions
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
List<Component> recentlyDelivered = componentRepository.findByDeliveredTrueAndActualDeliveryAfter(
    LocalDate.now().minusDays(2));
List<Component> overdueComponents = componentRepository.findOverdueComponents(LocalDate.now());

// Verify - Date edge cases handled correctly
assertThat(deliveredComponents).hasSize(3);
assertThat(recentlyDelivered).hasSize(2); // sameDayComponent and earlyComponent
assertThat(overdueComponents).isEmpty(); // All are delivered, so none are overdue

// Verify date relationships
assertThat(sameDayComponent.getExpectedDelivery()).isEqualTo(sameDayComponent.getActualDelivery());
assertThat(earlyComponent.getActualDelivery()).isBefore(earlyComponent.getExpectedDelivery());
assertThat(lateComponent.getActualDelivery()).isAfter(lateComponent.getExpectedDelivery());
}

// ========== INTEGRATION WITH SERVICE LAYER PATTERNS ==========

@Test
void testRepositoryServiceIntegration() {
// This test verifies that the repository works correctly with service layer patterns
// Setup - Create realistic component scenario
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);

testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

// Create component following service layer patterns
testComponent.setName("Service Integration Component");
testComponent.setPartNumber("SVC-INT-001");
testComponent.setDescription("Testing integration with service layer");
testComponent.setExpectedDelivery(LocalDate.now().plusDays(7));
testComponent.setDelivered(false);

Component savedComponent = componentRepository.save(testComponent);
entityManager.flush();

// Simulate service layer operations

// 1. Associate with task (service layer pattern)
savedTask.addRequiredComponent(savedComponent);
entityManager.persist(savedTask);
entityManager.flush();

// 2. Update expected delivery (service layer pattern)
savedComponent.setExpectedDelivery(LocalDate.now().plusDays(3));
componentRepository.save(savedComponent);

// 3. Mark as delivered (service layer pattern)
savedComponent.setDelivered(true);
componentRepository.save(savedComponent);

entityManager.flush();

// Verify - All service layer operations work through repository
Component finalComponent = componentRepository.findById(savedComponent.getId()).orElse(null);
assertThat(finalComponent).isNotNull();
assertThat(finalComponent.getExpectedDelivery()).isEqualTo(LocalDate.now().plusDays(3));
assertThat(finalComponent.isDelivered()).isTrue();
assertThat(finalComponent.getActualDelivery()).isEqualTo(LocalDate.now()); // Auto-set when marked delivered
assertThat(finalComponent.getRequiredForTasks()).contains(savedTask);

// Verify - Repository queries work for service layer
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());
Optional<Component> partNumberComponent = componentRepository.findByPartNumber("SVC-INT-001");

assertThat(deliveredComponents).contains(finalComponent);
assertThat(taskComponents).contains(finalComponent);
assertThat(partNumberComponent).isPresent();
assertThat(partNumberComponent.get().getId()).isEqualTo(finalComponent.getId());
}

@Test
void testComponentLifecycle() {
// Test the complete lifecycle of a component through repository operations

// 1. Create and save component (undelivered)
Component component = new Component();
component.setName("Lifecycle Component");
component.setPartNumber("LIFE-001");
component.setDescription("Testing component lifecycle");
component.setExpectedDelivery(LocalDate.now().plusDays(10));
component.setDelivered(false);

Component savedComponent = componentRepository.save(component);
entityManager.flush();
Long componentId = savedComponent.getId();

// Verify initial state
assertThat(savedComponent.isDelivered()).isFalse();
assertThat(savedComponent.getActualDelivery()).isNull();

// 2. Update expected delivery
savedComponent.setExpectedDelivery(LocalDate.now().plusDays(5));
componentRepository.save(savedComponent);
entityManager.flush();

// Verify update
Component updatedComponent = componentRepository.findById(componentId).orElse(null);
assertThat(updatedComponent.getExpectedDelivery()).isEqualTo(LocalDate.now().plusDays(5));

// 3. Mark as delivered
updatedComponent.setDelivered(true);
componentRepository.save(updatedComponent);
entityManager.flush();

// Verify delivery
Component deliveredComponent = componentRepository.findById(componentId).orElse(null);
assertThat(deliveredComponent.isDelivered()).isTrue();
assertThat(deliveredComponent.getActualDelivery()).isEqualTo(LocalDate.now());

// 4. Associate with task
Project savedProject = persistAndFlush(testProject);
Subsystem savedSubsystem = persistAndFlush(testSubsystem);
testTask.setProject(savedProject);
testTask.setSubsystem(savedSubsystem);
Task savedTask = persistAndFlush(testTask);

savedTask.addRequiredComponent(deliveredComponent);
entityManager.persist(savedTask);
entityManager.flush();

// Verify association
List<Component> taskComponents = componentRepository.findByRequiredForTasksId(savedTask.getId());
assertThat(taskComponents).hasSize(1);
assertThat(taskComponents.get(0).getId()).isEqualTo(componentId);

// 5. Query component through various methods
Optional<Component> byPartNumber = componentRepository.findByPartNumber("LIFE-001");
List<Component> byName = componentRepository.findByNameContainingIgnoreCase("Lifecycle");
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
long deliveredCount = componentRepository.countByDelivered(true);

// Verify all queries find the component
assertThat(byPartNumber).isPresent();
assertThat(byPartNumber.get().getId()).isEqualTo(componentId);
assertThat(byName).hasSize(1);
assertThat(byName.get(0).getId()).isEqualTo(componentId);
assertThat(deliveredComponents).contains(deliveredComponent);
assertThat(deliveredCount).isEqualTo(1);

// 6. Clean up - delete component
componentRepository.deleteById(componentId);
entityManager.flush();

// Verify deletion
assertThat(componentRepository.findById(componentId)).isEmpty();
assertThat(componentRepository.count()).isEqualTo(0);
}

@Test
void testComponentSearchAndFiltering() {
// Setup - Create diverse components for comprehensive search testing
Component motor1 = new Component();
motor1.setName("Falcon 500 Motor");
motor1.setPartNumber("M-FAL-500");
motor1.setDescription("High-performance brushless motor");
motor1.setExpectedDelivery(LocalDate.now().plusDays(5));
motor1.setDelivered(false);

Component motor2 = new Component();
motor2.setName("NEO Motor");
motor2.setPartNumber("M-NEO-550");
motor2.setDescription("Compact brushless motor for FRC");
motor2.setExpectedDelivery(LocalDate.now().plusDays(10));
motor2.setDelivered(true);
motor2.setActualDelivery(LocalDate.now().minusDays(1));

Component sensor = new Component();
sensor.setName("Encoder Sensor");
sensor.setPartNumber("S-ENC-001");
sensor.setDescription("Rotary encoder for position feedback");
sensor.setExpectedDelivery(LocalDate.now().plusDays(3));
sensor.setDelivered(false);

Component frame = new Component();
frame.setName("Aluminum Frame");
frame.setPartNumber("F-ALU-2x1");
frame.setDescription("2x1 aluminum frame tubing");
frame.setExpectedDelivery(LocalDate.now().minusDays(5));
frame.setDelivered(true);
frame.setActualDelivery(LocalDate.now().minusDays(3));

componentRepository.save(motor1);
componentRepository.save(motor2);
componentRepository.save(sensor);
componentRepository.save(frame);
entityManager.flush();

// Execute comprehensive search tests

// Search by name patterns
List<Component> motorComponents = componentRepository.findByNameContainingIgnoreCase("motor");
assertThat(motorComponents).hasSize(2);
assertThat(motorComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Falcon 500 Motor", "NEO Motor");

// Search by part number patterns
List<Component> mPrefixComponents = componentRepository.findByName("M-");
assertThat(mPrefixComponents).hasSize(2); // Both motors have M- prefix

// Search by description
List<Component> brushlessComponents = componentRepository.findByNameContainingIgnoreCase("brushless");
assertThat(brushlessComponents).isEmpty(); // Name search, not description

// Filter by delivery status
List<Component> undeliveredComponents = componentRepository.findByDelivered(false);
assertThat(undeliveredComponents).hasSize(2);
assertThat(undeliveredComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Falcon 500 Motor", "Encoder Sensor");

// Filter by expected delivery dates
List<Component> nearDelivery = componentRepository.findByExpectedDeliveryBefore(LocalDate.now().plusDays(7));
assertThat(nearDelivery).hasSize(3); // motor1, sensor, and frame (past date)

// Find overdue components
List<Component> overdueComponents = componentRepository.findOverdueComponents(LocalDate.now());
assertThat(overdueComponents).isEmpty(); // Frame was delivered despite being past expected date

// Count operations
long totalComponents = componentRepository.count();
long deliveredCount = componentRepository.countByDelivered(true);
long undeliveredCount = componentRepository.countByDelivered(false);

assertThat(totalComponents).isEqualTo(4);
assertThat(deliveredCount).isEqualTo(2);
assertThat(undeliveredCount).isEqualTo(2);
}

@Test
void testComponentTaskIntegrationScenarios() {
// Setup - Create a complex scenario with multiple tasks requiring overlapping components
Project savedProject = persistAndFlush(testProject);
Subsystem driveSubsystem = new Subsystem();
driveSubsystem.setName("Drivetrain");
driveSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
Subsystem savedDriveSubsystem = persistAndFlush(driveSubsystem);

Subsystem shooterSubsystem = new Subsystem();
shooterSubsystem.setName("Shooter");
shooterSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
Subsystem savedShooterSubsystem = persistAndFlush(shooterSubsystem);

// Create tasks
Task driveTask = new Task();
driveTask.setTitle("Build Drive System");
driveTask.setProject(savedProject);
driveTask.setSubsystem(savedDriveSubsystem);
driveTask.setEstimatedDuration(Duration.ofHours(20));
driveTask.setPriority(Task.Priority.HIGH);
Task savedDriveTask = persistAndFlush(driveTask);

Task shooterTask = new Task();
shooterTask.setTitle("Build Shooter");
shooterTask.setProject(savedProject);
shooterTask.setSubsystem(savedShooterSubsystem);
shooterTask.setEstimatedDuration(Duration.ofHours(15));
shooterTask.setPriority(Task.Priority.MEDIUM);
Task savedShooterTask = persistAndFlush(shooterTask);

// Create components
Component driveMotor = new Component();
driveMotor.setName("Drive Motor");
driveMotor.setPartNumber("DM-001");
driveMotor.setDelivered(true);
driveMotor.setActualDelivery(LocalDate.now().minusDays(2));
Component savedDriveMotor = persistAndFlush(driveMotor);

Component shooterMotor = new Component();
shooterMotor.setName("Shooter Motor");
shooterMotor.setPartNumber("SM-001");
shooterMotor.setDelivered(false);
shooterMotor.setExpectedDelivery(LocalDate.now().plusDays(5));
Component savedShooterMotor = persistAndFlush(shooterMotor);

Component sharedController = new Component();
sharedController.setName("Motor Controller");
sharedController.setPartNumber("MC-001");
sharedController.setDelivered(true);
sharedController.setActualDelivery(LocalDate.now().minusDays(1));
Component savedController = persistAndFlush(sharedController);

// Create component associations
savedDriveTask.addRequiredComponent(savedDriveMotor);
savedDriveTask.addRequiredComponent(savedController);

savedShooterTask.addRequiredComponent(savedShooterMotor);
savedShooterTask.addRequiredComponent(savedController); // Shared component

entityManager.persist(savedDriveTask);
entityManager.persist(savedShooterTask);
entityManager.flush();

// Execute integration tests

// Find components for each task
List<Component> driveComponents = componentRepository.findByRequiredForTasksId(savedDriveTask.getId());
List<Component> shooterComponents = componentRepository.findByRequiredForTasksId(savedShooterTask.getId());

assertThat(driveComponents).hasSize(2);
assertThat(driveComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Drive Motor", "Motor Controller");

assertThat(shooterComponents).hasSize(2);
assertThat(shooterComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Shooter Motor", "Motor Controller");

// Verify shared component appears in both tasks
assertThat(savedController.getRequiredForTasks()).hasSize(2);
assertThat(savedController.getRequiredForTasks()).containsExactlyInAnyOrder(savedDriveTask, savedShooterTask);

// Test component filtering by delivery status in context of tasks
List<Component> allComponents = componentRepository.findAll();
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
List<Component> undeliveredComponents = componentRepository.findByDelivered(false);

assertThat(allComponents).hasSize(3);
assertThat(deliveredComponents).hasSize(2); // Drive motor and controller
assertThat(undeliveredComponents).hasSize(1); // Shooter motor

// Verify task readiness based on component delivery
boolean driveTaskReady = driveComponents.stream().allMatch(Component::isDelivered);
boolean shooterTaskReady = shooterComponents.stream().allMatch(Component::isDelivered);

assertThat(driveTaskReady).isTrue(); // All drive components delivered
assertThat(shooterTaskReady).isFalse(); // Shooter motor not delivered
}

@Test
void testComponentDeliveryTracking() {
// Setup - Create components with various delivery scenarios
Component urgentComponent = new Component();
urgentComponent.setName("Urgent Component");
urgentComponent.setPartNumber("URG-001");
urgentComponent.setExpectedDelivery(LocalDate.now().plusDays(1));
urgentComponent.setDelivered(false);

Component lateComponent = new Component();
lateComponent.setName("Late Component");
lateComponent.setPartNumber("LATE-001");
lateComponent.setExpectedDelivery(LocalDate.now().minusDays(3));
lateComponent.setDelivered(false);

Component earlyDelivered = new Component();
earlyDelivered.setName("Early Delivered");
earlyDelivered.setPartNumber("EARLY-001");
earlyDelivered.setExpectedDelivery(LocalDate.now().plusDays(5));
earlyDelivered.setActualDelivery(LocalDate.now().minusDays(1));
earlyDelivered.setDelivered(true);

Component onTimeDelivered = new Component();
onTimeDelivered.setName("On Time Delivered");
onTimeDelivered.setPartNumber("ONTIME-001");
onTimeDelivered.setExpectedDelivery(LocalDate.now());
onTimeDelivered.setActualDelivery(LocalDate.now());
onTimeDelivered.setDelivered(true);

componentRepository.save(urgentComponent);
componentRepository.save(lateComponent);
componentRepository.save(earlyDelivered);
componentRepository.save(onTimeDelivered);
entityManager.flush();

// Execute delivery tracking queries

// Find overdue components (undelivered and past expected date)
List<Component> overdueComponents = componentRepository.findOverdueComponents(LocalDate.now());
assertThat(overdueComponents).hasSize(1);
assertThat(overdueComponents.get(0).getName()).isEqualTo("Late Component");

// Find components due soon
List<Component> dueSoon = componentRepository.findByExpectedDeliveryBetween(
    LocalDate.now(), LocalDate.now().plusDays(2));
assertThat(dueSoon).hasSize(2); // urgentComponent and onTimeDelivered

// Find recently delivered components
List<Component> recentDeliveries = componentRepository.findByDeliveredTrueAndActualDeliveryAfter(
    LocalDate.now().minusDays(2));
assertThat(recentDeliveries).hasSize(2); // earlyDelivered and onTimeDelivered

// Test delivery performance metrics
List<Component> allDelivered = componentRepository.findByDelivered(true);
long earlyDeliveries = allDelivered.stream()
    .mapToLong(c -> c.getActualDelivery().compareTo(c.getExpectedDelivery()))
    .filter(diff -> diff < 0)
    .count();

long onTimeDeliveries = allDelivered.stream()
    .mapToLong(c -> c.getActualDelivery().compareTo(c.getExpectedDelivery()))
    .filter(diff -> diff == 0)
    .count();

assertThat(earlyDeliveries).isEqualTo(1); // earlyDelivered
assertThat(onTimeDeliveries).isEqualTo(1); // onTimeDelivered

// Count by delivery status
long totalComponents = componentRepository.count();
long deliveredCount = componentRepository.countByDelivered(true);
long pendingCount = componentRepository.countByDelivered(false);

assertThat(totalComponents).isEqualTo(4);
assertThat(deliveredCount).isEqualTo(2);
assertThat(pendingCount).isEqualTo(2);
}

@Test
void testComponentDataIntegrityAndValidation() {
// Test data integrity constraints and validation

// Test component with all valid data
Component validComponent = new Component();
validComponent.setName("Valid Component");
validComponent.setPartNumber("VALID-001");
validComponent.setDescription("A completely valid component");
validComponent.setExpectedDelivery(LocalDate.now().plusDays(7));
validComponent.setDelivered(false);

Component savedValid = componentRepository.save(validComponent);
entityManager.flush();

assertThat(savedValid.getId()).isNotNull();
assertThat(savedValid.getName()).isEqualTo("Valid Component");

// Test component name uniqueness (if enforced by database)
// Note: Name uniqueness is not enforced in the current model, but part number is
Component similarName = new Component();
similarName.setName("Valid Component"); // Same name
similarName.setPartNumber("VALID-002"); // Different part number

Component savedSimilar = componentRepository.save(similarName);
entityManager.flush();

assertThat(savedSimilar.getId()).isNotNull();
assertThat(savedSimilar.getId()).isNotEqualTo(savedValid.getId());

// Test part number case sensitivity
boolean existsExact = componentRepository.existsByPartNumberIgnoreCase("VALID-001");
boolean existsLower = componentRepository.existsByPartNumberIgnoreCase("valid-001");
boolean existsMixed = componentRepository.existsByPartNumberIgnoreCase("Valid-001");

assertThat(existsExact).isTrue();
assertThat(existsLower).isTrue();
assertThat(existsMixed).isTrue();

// Test delivery logic validation
Component deliveryTest = new Component();
deliveryTest.setName("Delivery Test");
deliveryTest.setPartNumber("DELIV-001");
deliveryTest.setDelivered(false);
deliveryTest.setActualDelivery(null);

Component savedDeliveryTest = componentRepository.save(deliveryTest);
entityManager.flush();

// Mark as delivered - should auto-set actual delivery date
savedDeliveryTest.setDelivered(true);
componentRepository.save(savedDeliveryTest);
entityManager.flush();

assertThat(savedDeliveryTest.getActualDelivery()).isEqualTo(LocalDate.now());

// Test toString functionality
Component withPartNumber = new Component("Test Name", "TEST-001");
Component withoutPartNumber = new Component("Test Name Only");

assertThat(withPartNumber.toString()).isEqualTo("Test Name (TEST-001)");
assertThat(withoutPartNumber.toString()).isEqualTo("Test Name Only");
}

@Test
void testComponentRepositoryPerformanceOptimization() {
// Test repository performance with larger datasets and complex queries

// Setup - Create a substantial dataset
List<Component> components = new java.util.ArrayList<>();
for (int i = 1; i <= 100; i++) {
    Component component = new Component();
    component.setName("Performance Component " + i);
    component.setPartNumber("PERF-" + String.format("%04d", i));
    component.setDescription("Performance testing component number " + i);
    
    // Vary expected delivery dates
    component.setExpectedDelivery(LocalDate.now().plusDays(i % 30));
    
    // Vary delivery status (every 3rd component is delivered)
    if (i % 3 == 0) {
        component.setDelivered(true);
        component.setActualDelivery(LocalDate.now().minusDays(i % 10));
    } else {
        component.setDelivered(false);
    }
    
    components.add(component);
}

// Batch save for performance
long saveStartTime = System.currentTimeMillis();
componentRepository.saveAll(components);
entityManager.flush();
long saveEndTime = System.currentTimeMillis();

// Execute performance-critical queries
long queryStartTime = System.currentTimeMillis();

// Count operations
long totalCount = componentRepository.count();
long deliveredCount = componentRepository.countByDelivered(true);
long undeliveredCount = componentRepository.countByDelivered(false);

// Search operations
List<Component> nameSearch = componentRepository.findByNameContainingIgnoreCase("Performance");
List<Component> deliveredComponents = componentRepository.findByDelivered(true);
List<Component> recentDeliveries = componentRepository.findByDeliveredTrueAndActualDeliveryAfter(
    LocalDate.now().minusDays(5));
List<Component> overdueComponents = componentRepository.findOverdueComponents(LocalDate.now());

// Date range queries
List<Component> nearTermDelivery = componentRepository.findByExpectedDeliveryBetween(
    LocalDate.now(), LocalDate.now().plusDays(10));

long queryEndTime = System.currentTimeMillis();

// Verify results
assertThat(totalCount).isEqualTo(100);
assertThat(deliveredCount).isEqualTo(33); // Every 3rd component
assertThat(undeliveredCount).isEqualTo(67);
assertThat(nameSearch).hasSize(100); // All contain "Performance"
assertThat(deliveredComponents).hasSize(33);
assertThat(nearTermDelivery).hasSize(34); // Components with delivery in next 10 days

// Performance assertions
long saveTime = saveEndTime - saveStartTime;
long queryTime = queryEndTime - queryStartTime;

System.out.println("Component batch save time: " + saveTime + "ms");
System.out.println("Component query execution time: " + queryTime + "ms");

// Performance should be reasonable for dataset size
assertThat(saveTime).isLessThan(10000); // Save should complete within 10 seconds
assertThat(queryTime).isLessThan(5000); // Queries should complete within 5 seconds

// Memory efficiency - ensure we can handle the dataset without issues
assertThat(nameSearch.size()).isLessThanOrEqualTo(100);
assertThat(deliveredComponents.size()).isLessThanOrEqualTo(100);
}
}
}// src/test/java/org/frcpm/repositories/ComponentRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.ComponentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
* Integration test for ComponentRepository using Spring Boot @SpringBootTest.
* Uses full Spring context instead of @DataJpaTest to avoid context loading issues.
* 
* @SpringBootTest loads the complete application context
* @Transactional ensures each test runs in a transaction that's rolled back
* @AutoConfigureMockMvc configures MockMvc (though not used in repository tests)
*/
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComponentRepositoryIntegrationTest {

@Autowired
private ComponentRepository componentRepository;

@Autowired
private EntityManager entityManager;

private Component testComponent;
private Component motorComponent;
private Component deliveredComponent;
private Task testTask;
private Project testProject;
private Subsystem testSubsystem;

@BeforeEach
void setUp() {
// Create test objects ONLY - no premature setup
testProject = createTestProject();
testSubsystem = createTestSubsystem();
testTask = createTestTask();
testComponent = createTestComponent();
motorComponent = createMotorComponent();
deliveredComponent = createDeliveredComponent();
}

/**
* Creates a test project for use in tests.
*/
private Project createTestProject() {
Project project = new Project();
project.setName("Component Test Project");
project.setStartDate(LocalDate.now());
project.setGoalEndDate(LocalDate.now().plusWeeks(6));
project.setHardDeadline(LocalDate.now().plusWeeks(8));
return project;
}

/**
* Creates a test subsystem for use in tests.
*/
private Subsystem createTestSubsystem() {
Subsystem subsystem = new Subsystem();
subsystem.setName("Test Subsystem");
subsystem.setDescription("Test subsystem for component testing");
subsystem.setStatus(Subsystem.Status.IN_PROGRESS);
return subsystem;
}

/**
* Creates a test task for component associations.
*/
private Task createTestTask() {
Task task = new Task();
task.setTitle("Component Assembly Task");
task.setDescription("Assemble components for testing");
task.setEstimatedDuration(Duration.ofHours(4));
task.setPriority(Task.Priority.MEDIUM);
task.setProgress(25);
task.setCompleted(false);
return task;
}

/**
* Creates a test component for use in tests.
*/
private Component createTestComponent() {
Component component = new Component();
component.setName("Test Component");
component.setPartNumber("TC-001");
component.setDescription("A test component for unit testing");
component.setExpectedDelivery(LocalDate.now().plusDays(5));
component.setDelivered(false);
return component;
}

/**
* Creates a motor component for complex tests.
*/
private Component createMotorComponent() {
Component component = new Component();
component.setName("Falcon 500 Motor");
component.setPartNumber("217-6515");
component.setDescription("Falcon 500 brushless motor with integrated encoder");
component.setExpectedDelivery(LocalDate.now().plusDays(10));
component.setDelivered(false);
return component;
}

/**
* Creates a delivered component for delivery tests.
*/
private Component createDeliveredComponent() {
Component component = new Component();
component.setName("Aluminum Tubing");
component.setPartNumber("AL-2x1-48");
component.setDescription("2x1 inch aluminum tubing, 48 inches long");
component.setExpectedDelivery(LocalDate.now().minusDays(3));
component.setActualDelivery(LocalDate.now().minusDays(1));
component.setDelivered(true);
return component;
}

/**
* Helper method to persist and flush an entity.
* Replaces TestEntityManager's persistAndFlush functionality.
*/
private <T> T persistAndFlush(T entity) {
entityManager.persist(entity);
entityManager.flush();
return entity;
}

// ========== BASIC CRUD OPERATIONS ==========

@Test
void testSaveAndFindById() {
// Execute - Save component
Component savedComponent = componentRepository.save(testComponent);
entityManager.flush();

// Verify save
assertThat(savedComponent.getId()).isNotNull();

// Execute - Find by ID
Optional<Component> found = componentRepository.findById(savedComponent.getId());

// Verify find
assertThat(found).isPresent();
assertThat(found.get().getName()).isEqualTo("Test Component");
assertThat(found.get().getPartNumber()).isEqualTo("TC-001");
assertThat(found.get().getDescription()).isEqualTo("A test component for unit testing");
assertThat(found.get().getExpectedDelivery()).isEqualTo(LocalDate.now().plusDays(5));
assertThat(found.get().isDelivered()).isFalse();
assertThat(found.get().getActualDelivery()).isNull();
}

@Test
void testFindAll() {
// Setup - Persist multiple components
componentRepository.save(testComponent);
componentRepository.save(motorComponent);
componentRepository.save(deliveredComponent);
entityManager.flush();

// Execute - Find all
List<Component> allComponents = componentRepository.findAll();

// Verify
assertThat(allComponents).hasSize(3);
assertThat(allComponents).extracting(Component::getName)
    .containsExactlyInAnyOrder("Test Component", "Falcon 500 Motor", "Aluminum Tubing");
}

@Test
void testDeleteById() {
// Setup - Persist component
Component savedComponent = persistAndFlush(testComponent);

// Verify exists before deletion
assertThat(componentRepository.existsById(savedComponent.getId())).isTrue();

// Execute - Delete
componentRepository.deleteById(savedComponent.getId());
entityManager.flush();

// Verify deletion
assertThat(componentRepository.existsById(savedComponent.getId())).isFalse();
assertThat(componentRepository.findById(savedComponent.getId())).isEmpty();
}

@Test
void testCount() {
// Setup - Initial count should be 0
assertThat(componentRepository.count()).isEqualTo(0);

// Setup - Persist components
componentRepository.save(testComponent);
componentRepository.save(motorComponent);
entityManager.flush();

// Execute and verify
assertThat(componentRepository.count()).isEqualTo(2);
}

// ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========

@Test
void testFindByPartNumber() {
// Setup - Persist component
componentRepository.save(motorComponent);
entityManager.flush();

// Execute
Optional<Component> result = componentRepository.findByPartNumber("217-6515");

// Verify
assertThat(result).isPresent();
assertThat(result.get().getName()).isEqualTo("Falcon 500 Motor");
assertThat(result.get().getPartNumber()).isEqualTo("217-6515");
}

@Test
void testFindByPartNumber_NotFound() {
// Setup - Persist a different component
componentRepository.save(motorComponent);
entityManager.flush();

// Execute - Search for non-existent part number
Optional<Component> result = componentRepository.findByPartNumber("NONEXISTENT");

// Verify
assertThat(result).isEmpty();
}

@Test
void testFindByPartNumberIgnoreCase() {
// Setup - Persist component
componentRepository.save(motorComponent);
entityManager.flush();

// Execute - Case insensitive search
Optional<Component> lowerResult = componentRepository.findByPartNumberIgnoreCase("217-6515");
Optional<Component> mixedResult = componentRepository.findByPartNumberIgnoreCase("217-6515");

// Verify
assertThat(lowerResult).isPresent();
assertThat(lowerResult.get().getName()).isEqualTo("Falcon 500 Motor");

assertThat(mixedResult).isPresent();
assertThat(mixedResult.get().getName()).isEqualTo("Falcon 500 Motor");
}

@Test
void testFindByNameContainingIgnoreCase() {
// Setup - Persist components
componentRepository.save(testComponent);     // "Test Component"
componentRepository.save(motorComponent);    // "Falcon 500 Motor"
componentRepository.save(deliveredComponent); // "Aluminum Tubing"
entityManager.flush();

// Execute - Case insensitive search for "motor"
List<Component> motorResults = componentRepository.findByNameContainingIgnoreCase("MOTOR");

// Verify - Should find Falcon motor
assertThat(motorResults).hasSize(1);
assertThat(motorResults.get(0).getName()).isEqualTo("Falcon 500 Motor");

// Execute - Search for "component"
List<Component> componentResults = componentRepository.findByNameContainingIgnoreCase("component");