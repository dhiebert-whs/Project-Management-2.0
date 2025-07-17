/**
 * Service Worker Registration for FRC Project Management System
 * Simplified PWA registration for basic offline functionality
 */

// Check if service workers are supported
if ('serviceWorker' in navigator) {
    // Register service worker when page loads
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then(registration => {
                console.log('Service Worker registered successfully:', registration);
                
                // Check for updates
                registration.addEventListener('updatefound', () => {
                    const newWorker = registration.installing;
                    if (newWorker) {
                        newWorker.addEventListener('statechange', () => {
                            if (newWorker.state === 'installed') {
                                if (navigator.serviceWorker.controller) {
                                    // New update available
                                    showUpdateNotification();
                                }
                            }
                        });
                    }
                });
            })
            .catch(error => {
                console.log('Service Worker registration failed:', error);
            });
    });
}

/**
 * Show update notification to user
 */
function showUpdateNotification() {
    if (confirm('A new version is available. Would you like to update?')) {
        window.location.reload();
    }
}

/**
 * Check for updates manually
 */
function checkForUpdates() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.ready
            .then(registration => {
                registration.update();
            })
            .catch(error => {
                console.log('Update check failed:', error);
            });
    }
}

// Export functions for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        checkForUpdates,
        showUpdateNotification
    };
}