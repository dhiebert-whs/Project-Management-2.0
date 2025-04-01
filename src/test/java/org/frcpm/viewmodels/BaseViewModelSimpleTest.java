// src/test/java/org/frcpm/viewmodels/BaseViewModelSimpleTest.java
package org.frcpm.viewmodels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BaseViewModelSimpleTest {

    private TestViewModel viewModel;

    @BeforeEach
    public void setUp() {
        viewModel = new TestViewModel();
    }

    @Test
    public void testDirtyFlag() {
        // Initially dirty flag should be false
        assertFalse(viewModel.isDirty());

        // When: setting dirty flag to true
        viewModel.testSetDirty(true);

        // Then: dirty flag should be true
        assertTrue(viewModel.isDirty());
    }

    @Test
    public void testErrorMessage() {
        // Initially error message should be null
        assertNull(viewModel.getErrorMessage());

        // When: setting an error message
        viewModel.testSetErrorMessage("Test error");

        // Then: error message should be set
        assertEquals("Test error", viewModel.getErrorMessage());

        // When: clearing error message
        viewModel.testClearErrorMessage();

        // Then: error message should be null
        assertNull(viewModel.getErrorMessage());
    }

    /**
     * Test implementation of BaseViewModel to access protected methods.
     */
    private class TestViewModel extends BaseViewModel {
        public void testSetDirty(boolean isDirty) {
            setDirty(isDirty);
        }

        public void testSetErrorMessage(String message) {
            setErrorMessage(message);
        }

        public void testClearErrorMessage() {
            clearErrorMessage();
        }
    }
}