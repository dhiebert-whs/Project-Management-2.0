/**
 * Cache Manager for FRC Project Management System
 * Handles intelligent caching strategies for optimal offline performance
 */

class CacheManager {
    constructor() {
        this.cacheStrategies = new Map();
        this.cacheStats = new Map();
        this.cacheMaxSizes = new Map();
        this.cacheExpiry = new Map();
        this.preloadQueue = [];
        this.isPreloading = false;
        
        this.initializeCacheStrategies();
        this.initializeStats();
        this.startMaintenanceScheduler();
    }
    
    /**
     * Initialize cache strategies for different data types
     */
    initializeCacheStrategies() {
        // Task data - cache with medium priority
        this.cacheStrategies.set('tasks', {
            strategy: 'stale-while-revalidate',
            maxAge: 30 * 60 * 1000, // 30 minutes
            maxSize: 500,
            priority: 'medium',
            preload: true
        });
        
        // Project data - cache with high priority
        this.cacheStrategies.set('projects', {
            strategy: 'cache-first',
            maxAge: 60 * 60 * 1000, // 1 hour
            maxSize: 100,
            priority: 'high',
            preload: true
        });
        
        // Team members - cache with high priority
        this.cacheStrategies.set('teamMembers', {
            strategy: 'cache-first',
            maxAge: 2 * 60 * 60 * 1000, // 2 hours
            maxSize: 200,
            priority: 'high',
            preload: true
        });
        
        // Milestones - cache with medium priority
        this.cacheStrategies.set('milestones', {
            strategy: 'stale-while-revalidate',
            maxAge: 45 * 60 * 1000, // 45 minutes
            maxSize: 200,
            priority: 'medium',
            preload: true
        });
        
        // Workshop sessions - cache with low priority
        this.cacheStrategies.set('workshopSessions', {
            strategy: 'network-first',
            maxAge: 15 * 60 * 1000, // 15 minutes
            maxSize: 100,
            priority: 'low',
            preload: false
        });
        
        // Attendance data - cache with medium priority
        this.cacheStrategies.set('attendance', {
            strategy: 'network-first',
            maxAge: 10 * 60 * 1000, // 10 minutes
            maxSize: 300,
            priority: 'medium',
            preload: false
        });
        
        // Build season data - cache with high priority
        this.cacheStrategies.set('buildSeason', {
            strategy: 'stale-while-revalidate',
            maxAge: 60 * 60 * 1000, // 1 hour
            maxSize: 50,
            priority: 'high',
            preload: true
        });
        
        // Robot data - cache with medium priority
        this.cacheStrategies.set('robots', {
            strategy: 'cache-first',
            maxAge: 90 * 60 * 1000, // 90 minutes
            maxSize: 50,
            priority: 'medium',
            preload: true
        });
        
        // Competition data - cache with medium priority
        this.cacheStrategies.set('competitions', {
            strategy: 'cache-first',
            maxAge: 4 * 60 * 60 * 1000, // 4 hours
            maxSize: 100,
            priority: 'medium',
            preload: true
        });
        
        // Static resources - cache with high priority
        this.cacheStrategies.set('static', {
            strategy: 'cache-first',
            maxAge: 24 * 60 * 60 * 1000, // 24 hours
            maxSize: 200,
            priority: 'high',
            preload: false
        });
    }
    
    /**
     * Initialize cache statistics
     */
    initializeStats() {
        for (const [type] of this.cacheStrategies) {
            this.cacheStats.set(type, {
                hits: 0,
                misses: 0,
                size: 0,
                lastAccess: Date.now(),
                hitRate: 0
            });
        }
    }
    
    /**
     * Cache data with appropriate strategy
     */
    async cacheData(type, key, data, options = {}) {
        const strategy = this.cacheStrategies.get(type);
        if (!strategy) {
            console.warn(`CacheManager: Unknown cache type ${type}`);
            return false;
        }
        
        const cacheEntry = {
            key,\n            data,\n            timestamp: Date.now(),\n            type,\n            accessed: Date.now(),\n            accessCount: 1,\n            size: this.calculateDataSize(data),\n            priority: strategy.priority,\n            expires: Date.now() + (options.maxAge || strategy.maxAge)\n        };\n        \n        try {\n            // Check cache size limits\n            await this.enforceMaxSize(type, strategy.maxSize);\n            \n            // Store in appropriate cache\n            await this.storeInCache(type, key, cacheEntry);\n            \n            // Update stats\n            this.updateCacheStats(type, 'store');\n            \n            console.log(`CacheManager: Cached ${type}/${key}`);\n            return true;\n        } catch (error) {\n            console.error(`CacheManager: Error caching ${type}/${key}`, error);\n            return false;\n        }\n    }\n    \n    /**\n     * Retrieve data from cache\n     */\n    async getCachedData(type, key) {\n        try {\n            const cacheEntry = await this.getFromCache(type, key);\n            \n            if (!cacheEntry) {\n                this.updateCacheStats(type, 'miss');\n                return null;\n            }\n            \n            // Check if expired\n            if (Date.now() > cacheEntry.expires) {\n                await this.removeFromCache(type, key);\n                this.updateCacheStats(type, 'miss');\n                return null;\n            }\n            \n            // Update access info\n            cacheEntry.accessed = Date.now();\n            cacheEntry.accessCount++;\n            await this.storeInCache(type, key, cacheEntry);\n            \n            this.updateCacheStats(type, 'hit');\n            \n            console.log(`CacheManager: Cache hit for ${type}/${key}`);\n            return cacheEntry.data;\n        } catch (error) {\n            console.error(`CacheManager: Error retrieving ${type}/${key}`, error);\n            this.updateCacheStats(type, 'miss');\n            return null;\n        }\n    }\n    \n    /**\n     * Store data in appropriate cache store\n     */\n    async storeInCache(type, key, entry) {\n        if (window.offlineManager && window.offlineManager.db) {\n            await window.offlineManager.storeInIndexedDB(`cache_${type}`, {\n                id: key,\n                ...entry\n            });\n        } else {\n            // Fallback to localStorage (with size limits)\n            const storageKey = `cache_${type}_${key}`;\n            const data = JSON.stringify(entry);\n            \n            if (data.length < 1024 * 1024) { // 1MB limit for localStorage\n                localStorage.setItem(storageKey, data);\n            }\n        }\n    }\n    \n    /**\n     * Retrieve data from cache store\n     */\n    async getFromCache(type, key) {\n        if (window.offlineManager && window.offlineManager.db) {\n            return await window.offlineManager.getFromIndexedDB(`cache_${type}`, key);\n        } else {\n            // Fallback to localStorage\n            const storageKey = `cache_${type}_${key}`;\n            const data = localStorage.getItem(storageKey);\n            return data ? JSON.parse(data) : null;\n        }\n    }\n    \n    /**\n     * Remove data from cache\n     */\n    async removeFromCache(type, key) {\n        if (window.offlineManager && window.offlineManager.db) {\n            await window.offlineManager.removeFromIndexedDB(`cache_${type}`, key);\n        } else {\n            localStorage.removeItem(`cache_${type}_${key}`);\n        }\n    }\n    \n    /**\n     * Enforce maximum cache size\n     */\n    async enforceMaxSize(type, maxSize) {\n        const entries = await this.getAllCacheEntries(type);\n        \n        if (entries.length >= maxSize) {\n            // Remove oldest and least accessed entries\n            const toRemove = entries\n                .sort((a, b) => {\n                    // Sort by access frequency and recency\n                    const aScore = a.accessCount / (Date.now() - a.accessed);\n                    const bScore = b.accessCount / (Date.now() - b.accessed);\n                    return aScore - bScore;\n                })\n                .slice(0, Math.ceil(maxSize * 0.2)); // Remove 20% of entries\n            \n            for (const entry of toRemove) {\n                await this.removeFromCache(type, entry.key);\n            }\n            \n            console.log(`CacheManager: Removed ${toRemove.length} entries from ${type} cache`);\n        }\n    }\n    \n    /**\n     * Get all cache entries for a type\n     */\n    async getAllCacheEntries(type) {\n        if (window.offlineManager && window.offlineManager.db) {\n            const entries = await window.offlineManager.getFromIndexedDB(`cache_${type}`);\n            return entries || [];\n        } else {\n            const entries = [];\n            const prefix = `cache_${type}_`;\n            \n            for (let i = 0; i < localStorage.length; i++) {\n                const key = localStorage.key(i);\n                if (key && key.startsWith(prefix)) {\n                    const data = localStorage.getItem(key);\n                    if (data) {\n                        entries.push(JSON.parse(data));\n                    }\n                }\n            }\n            \n            return entries;\n        }\n    }\n    \n    /**\n     * Calculate data size for cache management\n     */\n    calculateDataSize(data) {\n        return new TextEncoder().encode(JSON.stringify(data)).length;\n    }\n    \n    /**\n     * Update cache statistics\n     */\n    updateCacheStats(type, action) {\n        const stats = this.cacheStats.get(type);\n        if (!stats) return;\n        \n        switch (action) {\n            case 'hit':\n                stats.hits++;\n                break;\n            case 'miss':\n                stats.misses++;\n                break;\n            case 'store':\n                stats.size++;\n                break;\n        }\n        \n        stats.lastAccess = Date.now();\n        stats.hitRate = stats.hits / (stats.hits + stats.misses);\n        \n        this.cacheStats.set(type, stats);\n    }\n    \n    /**\n     * Preload critical data for offline use\n     */\n    async preloadCriticalData() {\n        if (this.isPreloading) {\n            console.log('CacheManager: Preload already in progress');\n            return;\n        }\n        \n        this.isPreloading = true;\n        \n        try {\n            console.log('CacheManager: Starting critical data preload');\n            \n            // Preload data based on strategy configuration\n            for (const [type, strategy] of this.cacheStrategies) {\n                if (strategy.preload) {\n                    await this.preloadDataType(type);\n                }\n            }\n            \n            console.log('CacheManager: Critical data preload completed');\n        } catch (error) {\n            console.error('CacheManager: Error during preload', error);\n        } finally {\n            this.isPreloading = false;\n        }\n    }\n    \n    /**\n     * Preload specific data type\n     */\n    async preloadDataType(type) {\n        const endpoints = this.getPreloadEndpoints(type);\n        \n        for (const endpoint of endpoints) {\n            try {\n                const response = await fetch(endpoint.url);\n                if (response.ok) {\n                    const data = await response.json();\n                    await this.cacheData(type, endpoint.key, data);\n                }\n            } catch (error) {\n                console.error(`CacheManager: Error preloading ${type}`, error);\n            }\n        }\n    }\n    \n    /**\n     * Get preload endpoints for data type\n     */\n    getPreloadEndpoints(type) {\n        const endpoints = {\n            tasks: [\n                { url: '/api/tasks', key: 'all' },\n                { url: '/api/tasks/active', key: 'active' }\n            ],\n            projects: [\n                { url: '/api/projects', key: 'all' },\n                { url: '/api/projects/active', key: 'active' }\n            ],\n            teamMembers: [\n                { url: '/api/team-members', key: 'all' }\n            ],\n            milestones: [\n                { url: '/api/milestones', key: 'all' },\n                { url: '/api/milestones/upcoming', key: 'upcoming' }\n            ],\n            buildSeason: [\n                { url: '/api/build-season/dashboard', key: 'dashboard' },\n                { url: '/api/build-season/progress', key: 'progress' }\n            ],\n            robots: [\n                { url: '/api/robots/current', key: 'current' }\n            ],\n            competitions: [\n                { url: '/api/competitions/upcoming', key: 'upcoming' }\n            ]\n        };\n        \n        return endpoints[type] || [];\n    }\n    \n    /**\n     * Smart cache invalidation\n     */\n    async invalidateCache(type, key = null) {\n        if (key) {\n            await this.removeFromCache(type, key);\n            console.log(`CacheManager: Invalidated ${type}/${key}`);\n        } else {\n            // Invalidate all entries for type\n            const entries = await this.getAllCacheEntries(type);\n            for (const entry of entries) {\n                await this.removeFromCache(type, entry.key);\n            }\n            console.log(`CacheManager: Invalidated all ${type} cache`);\n        }\n        \n        // Reset stats\n        const stats = this.cacheStats.get(type);\n        if (stats) {\n            if (key) {\n                stats.size = Math.max(0, stats.size - 1);\n            } else {\n                stats.size = 0;\n            }\n        }\n    }\n    \n    /**\n     * Cache maintenance - remove expired entries\n     */\n    async performMaintenance() {\n        console.log('CacheManager: Performing maintenance');\n        \n        for (const [type] of this.cacheStrategies) {\n            const entries = await this.getAllCacheEntries(type);\n            const now = Date.now();\n            let removedCount = 0;\n            \n            for (const entry of entries) {\n                if (now > entry.expires) {\n                    await this.removeFromCache(type, entry.key);\n                    removedCount++;\n                }\n            }\n            \n            if (removedCount > 0) {\n                console.log(`CacheManager: Removed ${removedCount} expired entries from ${type}`);\n                const stats = this.cacheStats.get(type);\n                if (stats) {\n                    stats.size = Math.max(0, stats.size - removedCount);\n                }\n            }\n        }\n    }\n    \n    /**\n     * Get cache statistics\n     */\n    getCacheStats() {\n        const stats = {};\n        \n        for (const [type, typeStats] of this.cacheStats) {\n            stats[type] = {\n                ...typeStats,\n                hitRate: Math.round(typeStats.hitRate * 100) / 100\n            };\n        }\n        \n        return stats;\n    }\n    \n    /**\n     * Clear all caches\n     */\n    async clearAllCaches() {\n        console.log('CacheManager: Clearing all caches');\n        \n        for (const [type] of this.cacheStrategies) {\n            await this.invalidateCache(type);\n        }\n        \n        // Reset all stats\n        this.initializeStats();\n    }\n    \n    /**\n     * Get cache size information\n     */\n    async getCacheSizeInfo() {\n        const sizeInfo = {};\n        \n        for (const [type] of this.cacheStrategies) {\n            const entries = await this.getAllCacheEntries(type);\n            const totalSize = entries.reduce((sum, entry) => sum + (entry.size || 0), 0);\n            \n            sizeInfo[type] = {\n                entries: entries.length,\n                totalSize,\n                averageSize: entries.length > 0 ? totalSize / entries.length : 0\n            };\n        }\n        \n        return sizeInfo;\n    }\n    \n    /**\n     * Start maintenance scheduler\n     */\n    startMaintenanceScheduler() {\n        // Run maintenance every 30 minutes\n        setInterval(() => {\n            this.performMaintenance();\n        }, 30 * 60 * 1000);\n        \n        // Run preload when coming online\n        window.addEventListener('online', () => {\n            setTimeout(() => this.preloadCriticalData(), 5000);\n        });\n    }\n    \n    /**\n     * Register cache invalidation listener\n     */\n    onDataUpdate(type, callback) {\n        // Listen for data updates and invalidate cache\n        document.addEventListener(`${type}Updated`, () => {\n            this.invalidateCache(type);\n            if (callback) callback();\n        });\n    }\n    \n    /**\n     * Warm up cache for specific user workflow\n     */\n    async warmUpCache(workflow) {\n        const workflowCaches = {\n            taskManagement: ['tasks', 'projects', 'teamMembers'],\n            buildSeason: ['buildSeason', 'milestones', 'robots'],\n            workshop: ['workshopSessions', 'attendance', 'teamMembers'],\n            competition: ['competitions', 'buildSeason', 'robots']\n        };\n        \n        const cachesToWarm = workflowCaches[workflow];\n        if (!cachesToWarm) return;\n        \n        console.log(`CacheManager: Warming up cache for ${workflow}`);\n        \n        for (const type of cachesToWarm) {\n            await this.preloadDataType(type);\n        }\n    }\n    \n    /**\n     * Create cache key from parameters\n     */\n    createCacheKey(base, params = {}) {\n        const sortedParams = Object.keys(params)\n            .sort()\n            .map(key => `${key}=${params[key]}`)\n            .join('&');\n        \n        return sortedParams ? `${base}?${sortedParams}` : base;\n    }\n}\n\n// Initialize cache manager\nlet cacheManager;\n\ndocument.addEventListener('DOMContentLoaded', () => {\n    cacheManager = new CacheManager();\n    \n    // Preload critical data when online\n    if (navigator.onLine) {\n        setTimeout(() => cacheManager.preloadCriticalData(), 2000);\n    }\n    \n    // Make globally available\n    window.cacheManager = cacheManager;\n    \n    console.log('CacheManager: Initialized and ready');\n});\n\n// Export for use in other modules\nif (typeof module !== 'undefined' && module.exports) {\n    module.exports = CacheManager;\n}