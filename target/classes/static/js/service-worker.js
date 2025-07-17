/**
 * Service Worker for FRC Project Management System
 * Provides offline functionality, caching, and background sync capabilities
 */

const CACHE_NAME = 'frc-pm-v1.0.0';
const OFFLINE_URL = '/offline.html';

// Define cache strategies for different resource types
const CACHE_STRATEGIES = {
    NETWORK_FIRST: 'network-first',
    CACHE_FIRST: 'cache-first',
    STALE_WHILE_REVALIDATE: 'stale-while-revalidate',
    NETWORK_ONLY: 'network-only',
    CACHE_ONLY: 'cache-only'
};

// Resources to cache on install
const STATIC_CACHE_RESOURCES = [
    '/',
    '/offline.html',
    '/static/css/bootstrap.min.css',
    '/static/js/bootstrap.bundle.min.js',
    '/static/js/offline-manager.js',
    '/static/js/cache-manager.js',
    '/static/js/sync-manager.js',
    '/webjars/font-awesome/6.4.0/css/all.min.css',
    '/favicon.ico'
];

// Dynamic cache configuration
const CACHE_CONFIG = {
    // Static assets (CSS, JS, images)
    static: {
        strategy: CACHE_STRATEGIES.CACHE_FIRST,
        maxEntries: 100,
        maxAgeSeconds: 7 * 24 * 60 * 60 // 7 days
    },
    
    // API responses
    api: {
        strategy: CACHE_STRATEGIES.NETWORK_FIRST,
        maxEntries: 50,
        maxAgeSeconds: 5 * 60 // 5 minutes
    },
    
    // HTML pages
    pages: {
        strategy: CACHE_STRATEGIES.STALE_WHILE_REVALIDATE,
        maxEntries: 30,
        maxAgeSeconds: 24 * 60 * 60 // 24 hours
    },
    
    // Images
    images: {
        strategy: CACHE_STRATEGIES.CACHE_FIRST,
        maxEntries: 60,
        maxAgeSeconds: 30 * 24 * 60 * 60 // 30 days
    }
};

// Background sync queue for offline actions
const SYNC_QUEUES = {
    TASK_UPDATES: 'task-updates',
    MILESTONE_UPDATES: 'milestone-updates',
    ATTENDANCE_UPDATES: 'attendance-updates',
    ROBOT_WEIGHT_UPDATES: 'robot-weight-updates'
};

/**
 * Service Worker Installation
 */
self.addEventListener('install', event => {
    console.log('Service Worker: Installing...');
    
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('Service Worker: Caching essential resources');
                return cache.addAll(STATIC_CACHE_RESOURCES);
            })
            .then(() => {
                console.log('Service Worker: Installation complete');
                return self.skipWaiting();
            })
            .catch(error => {
                console.error('Service Worker: Installation failed', error);
            })
    );
});

/**
 * Service Worker Activation
 */
self.addEventListener('activate', event => {
    console.log('Service Worker: Activating...');
    
    event.waitUntil(
        caches.keys()
            .then(cacheNames => {
                return Promise.all(
                    cacheNames.map(cacheName => {
                        if (cacheName !== CACHE_NAME) {
                            console.log('Service Worker: Deleting old cache', cacheName);
                            return caches.delete(cacheName);
                        }
                    })
                );
            })
            .then(() => {
                console.log('Service Worker: Activation complete');
                return self.clients.claim();
            })
            .catch(error => {
                console.error('Service Worker: Activation failed', error);
            })
    );
});

/**
 * Fetch Event Handler - Main request interceptor
 */
self.addEventListener('fetch', event => {
    const { request } = event;
    const url = new URL(request.url);
    
    // Skip non-HTTP requests
    if (!request.url.startsWith('http')) {
        return;
    }
    
    // Skip requests to external domains (except for CDN resources)
    if (url.origin !== self.location.origin && !isCDNResource(url)) {
        return;
    }
    
    // Determine cache strategy based on request type
    const strategy = determineCacheStrategy(request);
    
    event.respondWith(
        handleRequest(request, strategy)
            .catch(error => {
                console.error('Service Worker: Request failed', error);
                return handleOfflineResponse(request);
            })
    );
});

/**
 * Background Sync Event Handler
 */
self.addEventListener('sync', event => {
    console.log('Service Worker: Background sync triggered', event.tag);
    
    switch (event.tag) {
        case SYNC_QUEUES.TASK_UPDATES:
            event.waitUntil(syncTaskUpdates());
            break;
        case SYNC_QUEUES.MILESTONE_UPDATES:
            event.waitUntil(syncMilestoneUpdates());
            break;
        case SYNC_QUEUES.ATTENDANCE_UPDATES:
            event.waitUntil(syncAttendanceUpdates());
            break;
        case SYNC_QUEUES.ROBOT_WEIGHT_UPDATES:
            event.waitUntil(syncRobotWeightUpdates());
            break;
        default:
            console.log('Service Worker: Unknown sync event', event.tag);
    }
});

/**
 * Push Event Handler for notifications
 */
self.addEventListener('push', event => {
    console.log('Service Worker: Push message received', event);
    
    const options = {
        body: event.data ? event.data.text() : 'You have a new notification',
        icon: '/static/images/icon-192x192.png',
        badge: '/static/images/badge-72x72.png',
        vibrate: [100, 50, 100],
        data: {
            dateOfArrival: Date.now(),
            primaryKey: 1
        },
        actions: [
            {
                action: 'explore',
                title: 'View Details',
                icon: '/static/images/checkmark.png'
            },
            {
                action: 'close',
                title: 'Close',
                icon: '/static/images/xmark.png'
            }
        ]
    };
    
    event.waitUntil(
        self.registration.showNotification('FRC Project Management', options)
    );
});

/**
 * Notification Click Handler
 */
self.addEventListener('notificationclick', event => {
    console.log('Service Worker: Notification clicked', event);
    
    event.notification.close();
    
    if (event.action === 'explore') {
        event.waitUntil(
            clients.openWindow('/')
        );
    }
});

/**
 * Handle different request types with appropriate caching strategies
 */
async function handleRequest(request, strategy) {
    switch (strategy) {
        case CACHE_STRATEGIES.NETWORK_FIRST:
            return networkFirst(request);
        case CACHE_STRATEGIES.CACHE_FIRST:
            return cacheFirst(request);
        case CACHE_STRATEGIES.STALE_WHILE_REVALIDATE:
            return staleWhileRevalidate(request);
        case CACHE_STRATEGIES.NETWORK_ONLY:
            return fetch(request);
        case CACHE_STRATEGIES.CACHE_ONLY:
            return caches.match(request);
        default:
            return networkFirst(request);
    }
}

/**
 * Network First Strategy - Try network, fall back to cache
 */
async function networkFirst(request) {
    try {
        const networkResponse = await fetch(request);
        
        if (networkResponse.ok) {
            const cache = await caches.open(CACHE_NAME);
            cache.put(request, networkResponse.clone());
        }
        
        return networkResponse;
    } catch (error) {
        console.log('Service Worker: Network failed, trying cache', error);
        const cachedResponse = await caches.match(request);
        
        if (cachedResponse) {
            return cachedResponse;
        }
        
        throw error;
    }
}

/**
 * Cache First Strategy - Try cache, fall back to network
 */
async function cacheFirst(request) {
    const cachedResponse = await caches.match(request);
    
    if (cachedResponse) {
        return cachedResponse;
    }
    
    try {
        const networkResponse = await fetch(request);
        
        if (networkResponse.ok) {
            const cache = await caches.open(CACHE_NAME);
            cache.put(request, networkResponse.clone());
        }
        
        return networkResponse;
    } catch (error) {
        console.error('Service Worker: Cache first failed', error);
        throw error;
    }
}

/**
 * Stale While Revalidate Strategy - Return cache immediately, update in background
 */
async function staleWhileRevalidate(request) {
    const cachedResponse = await caches.match(request);
    
    const networkPromise = fetch(request).then(networkResponse => {
        if (networkResponse.ok) {
            const cache = caches.open(CACHE_NAME);
            cache.then(c => c.put(request, networkResponse.clone()));
        }
        return networkResponse;
    }).catch(error => {
        console.log('Service Worker: Background update failed', error);
        return null;
    });
    
    return cachedResponse || networkPromise;
}

/**
 * Determine appropriate cache strategy based on request
 */
function determineCacheStrategy(request) {
    const url = new URL(request.url);
    const pathname = url.pathname;
    
    // API requests
    if (pathname.startsWith('/api/')) {
        return CACHE_STRATEGIES.NETWORK_FIRST;
    }
    
    // Static assets
    if (pathname.match(/\.(css|js|woff2?|ttf|eot)$/)) {
        return CACHE_STRATEGIES.CACHE_FIRST;
    }
    
    // Images
    if (pathname.match(/\.(jpg|jpeg|png|gif|svg|ico)$/)) {
        return CACHE_STRATEGIES.CACHE_FIRST;
    }
    
    // HTML pages
    if (request.headers.get('accept')?.includes('text/html')) {
        return CACHE_STRATEGIES.STALE_WHILE_REVALIDATE;
    }
    
    // Default strategy
    return CACHE_STRATEGIES.NETWORK_FIRST;
}

/**
 * Check if URL is a CDN resource we want to cache
 */
function isCDNResource(url) {
    const cdnDomains = [
        'cdn.jsdelivr.net',
        'cdnjs.cloudflare.com',
        'unpkg.com',
        'fonts.googleapis.com',
        'fonts.gstatic.com'
    ];
    
    return cdnDomains.some(domain => url.hostname.includes(domain));
}

/**
 * Handle offline responses
 */
async function handleOfflineResponse(request) {
    const url = new URL(request.url);
    
    // For HTML pages, return the offline page
    if (request.headers.get('accept')?.includes('text/html')) {
        const offlineResponse = await caches.match(OFFLINE_URL);
        return offlineResponse || new Response('Offline', { status: 503 });
    }
    
    // For API requests, return cached data or offline indicator
    if (url.pathname.startsWith('/api/')) {
        const cachedResponse = await caches.match(request);
        if (cachedResponse) {
            return cachedResponse;
        }
        
        return new Response(JSON.stringify({
            error: 'Offline',
            message: 'This request is not available offline',
            cached: false
        }), {
            status: 503,
            headers: { 'Content-Type': 'application/json' }
        });
    }
    
    // For other resources, try to return cached version
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
        return cachedResponse;
    }
    
    // Return generic offline response
    return new Response('Resource not available offline', { status: 503 });
}

/**
 * Background Sync Functions
 */

async function syncTaskUpdates() {
    console.log('Service Worker: Syncing task updates');
    
    try {
        const pendingUpdates = await getPendingTaskUpdates();
        
        for (const update of pendingUpdates) {
            try {
                const response = await fetch(update.url, {
                    method: update.method,
                    headers: update.headers,
                    body: update.body
                });
                
                if (response.ok) {
                    await removePendingTaskUpdate(update.id);
                    console.log('Service Worker: Task update synced', update.id);
                } else {
                    console.error('Service Worker: Failed to sync task update', update.id, response.status);
                }
            } catch (error) {
                console.error('Service Worker: Error syncing task update', update.id, error);
            }
        }
    } catch (error) {
        console.error('Service Worker: Error in task sync', error);
    }
}

async function syncMilestoneUpdates() {
    console.log('Service Worker: Syncing milestone updates');
    
    try {
        const pendingUpdates = await getPendingMilestoneUpdates();
        
        for (const update of pendingUpdates) {
            try {
                const response = await fetch(update.url, {
                    method: update.method,
                    headers: update.headers,
                    body: update.body
                });
                
                if (response.ok) {
                    await removePendingMilestoneUpdate(update.id);
                    console.log('Service Worker: Milestone update synced', update.id);
                } else {
                    console.error('Service Worker: Failed to sync milestone update', update.id, response.status);
                }
            } catch (error) {
                console.error('Service Worker: Error syncing milestone update', update.id, error);
            }
        }
    } catch (error) {
        console.error('Service Worker: Error in milestone sync', error);
    }
}

async function syncAttendanceUpdates() {
    console.log('Service Worker: Syncing attendance updates');
    
    try {
        const pendingUpdates = await getPendingAttendanceUpdates();
        
        for (const update of pendingUpdates) {
            try {
                const response = await fetch(update.url, {
                    method: update.method,
                    headers: update.headers,
                    body: update.body
                });
                
                if (response.ok) {
                    await removePendingAttendanceUpdate(update.id);
                    console.log('Service Worker: Attendance update synced', update.id);
                } else {
                    console.error('Service Worker: Failed to sync attendance update', update.id, response.status);
                }
            } catch (error) {
                console.error('Service Worker: Error syncing attendance update', update.id, error);
            }
        }
    } catch (error) {
        console.error('Service Worker: Error in attendance sync', error);
    }
}

async function syncRobotWeightUpdates() {
    console.log('Service Worker: Syncing robot weight updates');
    
    try {
        const pendingUpdates = await getPendingRobotWeightUpdates();
        
        for (const update of pendingUpdates) {
            try {
                const response = await fetch(update.url, {
                    method: update.method,
                    headers: update.headers,
                    body: update.body
                });
                
                if (response.ok) {
                    await removePendingRobotWeightUpdate(update.id);
                    console.log('Service Worker: Robot weight update synced', update.id);
                } else {
                    console.error('Service Worker: Failed to sync robot weight update', update.id, response.status);
                }
            } catch (error) {
                console.error('Service Worker: Error syncing robot weight update', update.id, error);
            }
        }
    } catch (error) {
        console.error('Service Worker: Error in robot weight sync', error);
    }
}

/**
 * IndexedDB Operations for Sync Queue Management
 */

async function getPendingTaskUpdates() {
    return getFromIndexedDB('syncQueue', 'task-updates');
}

async function removePendingTaskUpdate(id) {
    return removeFromIndexedDB('syncQueue', 'task-updates', id);
}

async function getPendingMilestoneUpdates() {
    return getFromIndexedDB('syncQueue', 'milestone-updates');
}

async function removePendingMilestoneUpdate(id) {
    return removeFromIndexedDB('syncQueue', 'milestone-updates', id);
}

async function getPendingAttendanceUpdates() {
    return getFromIndexedDB('syncQueue', 'attendance-updates');
}

async function removePendingAttendanceUpdate(id) {
    return removeFromIndexedDB('syncQueue', 'attendance-updates', id);
}

async function getPendingRobotWeightUpdates() {
    return getFromIndexedDB('syncQueue', 'robot-weight-updates');
}

async function removePendingRobotWeightUpdate(id) {
    return removeFromIndexedDB('syncQueue', 'robot-weight-updates', id);
}

/**
 * Generic IndexedDB operations
 */
async function getFromIndexedDB(storeName, objectStoreName) {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(storeName, 1);
        
        request.onerror = () => reject(request.error);
        request.onsuccess = () => {
            const db = request.result;
            const transaction = db.transaction([objectStoreName], 'readonly');
            const store = transaction.objectStore(objectStoreName);
            const getRequest = store.getAll();
            
            getRequest.onsuccess = () => resolve(getRequest.result || []);
            getRequest.onerror = () => reject(getRequest.error);
        };
    });
}

async function removeFromIndexedDB(storeName, objectStoreName, id) {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(storeName, 1);
        
        request.onerror = () => reject(request.error);
        request.onsuccess = () => {
            const db = request.result;
            const transaction = db.transaction([objectStoreName], 'readwrite');
            const store = transaction.objectStore(objectStoreName);
            const deleteRequest = store.delete(id);
            
            deleteRequest.onsuccess = () => resolve();
            deleteRequest.onerror = () => reject(deleteRequest.error);
        };
    });
}

/**
 * Cache Management Functions
 */

async function cleanupExpiredCache() {
    console.log('Service Worker: Cleaning up expired cache entries');
    
    const cache = await caches.open(CACHE_NAME);
    const requests = await cache.keys();
    
    for (const request of requests) {
        const response = await cache.match(request);
        if (response) {
            const cachedDate = new Date(response.headers.get('date'));
            const now = new Date();
            const maxAge = getCacheMaxAge(request);
            
            if (now - cachedDate > maxAge * 1000) {
                console.log('Service Worker: Removing expired cache entry', request.url);
                await cache.delete(request);
            }
        }
    }
}

function getCacheMaxAge(request) {
    const url = new URL(request.url);
    const pathname = url.pathname;
    
    if (pathname.startsWith('/api/')) {
        return CACHE_CONFIG.api.maxAgeSeconds;
    }
    
    if (pathname.match(/\.(css|js|woff2?|ttf|eot)$/)) {
        return CACHE_CONFIG.static.maxAgeSeconds;
    }
    
    if (pathname.match(/\.(jpg|jpeg|png|gif|svg|ico)$/)) {
        return CACHE_CONFIG.images.maxAgeSeconds;
    }
    
    return CACHE_CONFIG.pages.maxAgeSeconds;
}

// Clean up expired cache entries every 24 hours
setInterval(cleanupExpiredCache, 24 * 60 * 60 * 1000);

console.log('Service Worker: Script loaded');