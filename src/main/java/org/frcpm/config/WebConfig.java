// src/main/java/org/frcpm/config/WebConfig.java (Enhanced for Phase 2C)

package org.frcpm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Enhanced Web MVC configuration for Phase 2C PWA features.
 * 
 * ✅ PHASE 2C: PWA Development - Web Configuration Enhancement
 * 
 * This configuration builds upon the Phase 2A foundation to add:
 * - Progressive Web App manifest and service worker support
 * - WebSocket communication resources
 * - Offline-capable static resource caching strategies
 * - Mobile-optimized resource delivery
 * - Real-time collaboration infrastructure
 * 
 * Maintains compatibility with Phase 2B security and COPPA compliance.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Environment environment;

    /**
     * Enhanced static resource handling for PWA capabilities.
     * 
     * ✅ PHASE 2C: Added PWA manifest, service worker, and offline resources
     * 
     * Configures caching strategies optimized for:
     * - Offline-first PWA operation
     * - Mobile workshop environments with limited connectivity
     * - Real-time WebSocket communication assets
     * - Progressive enhancement for older browsers
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // PWA Core Files (critical for offline functionality)
        registry.addResourceHandler("/manifest.json")
                .addResourceLocations("classpath:/static/manifest.json")
                .setCachePeriod(3600) // 1 hour cache - allows for PWA updates
                .resourceChain(true);
                
        registry.addResourceHandler("/sw.js", "/service-worker.js")
                .addResourceLocations("classpath:/static/sw.js", "classpath:/static/service-worker.js")
                .setCachePeriod(0) // No cache for service worker - immediate updates
                .resourceChain(false); // Disable resource chain for SW
                
        // PWA Icons and Splash Screens
        registry.addResourceHandler("/icons/**")
                .addResourceLocations("classpath:/static/icons/")
                .setCachePeriod(604800) // 7 days cache - icons rarely change
                .resourceChain(true);
                
        // Enhanced JavaScript for PWA and WebSocket
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(isDevelopment() ? 0 : 86400) // No cache in dev, 24h in prod
                .resourceChain(true);
                
        // Enhanced CSS with mobile optimizations
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(isDevelopment() ? 0 : 86400)
                .resourceChain(true);
                
        // Main static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(isDevelopment() ? 0 : 86400)
                .resourceChain(true);
                
        // Images and media (longer cache for performance)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(604800) // 7 days cache
                .resourceChain(true);
                
        // Fonts for consistent offline experience
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/")
                .setCachePeriod(2592000) // 30 days cache - fonts very stable
                .resourceChain(true);
                
        // WebSocket fallback resources (SockJS)
        registry.addResourceHandler("/sockjs/**")
                .addResourceLocations("classpath:/static/sockjs/")
                .setCachePeriod(86400)
                .resourceChain(true);
                
        // Favicon and app icons
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(604800);
                
        // Apple touch icons for iOS PWA
        registry.addResourceHandler("/apple-touch-icon*.png")
                .addResourceLocations("classpath:/static/icons/")
                .setCachePeriod(604800);
    }

    /**
     * Enhanced view controllers for PWA and offline support.
     * 
     * ✅ PHASE 2C: Added offline fallback and PWA installation pages
     */
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Authentication pages
        registry.addViewController("/login").setViewName("auth/login");
        
        // Root redirect to dashboard
        registry.addViewController("/").setViewName("redirect:/dashboard");
        
        // Error pages
        registry.addViewController("/error").setViewName("error/general");
        registry.addViewController("/403").setViewName("error/access-denied");
        registry.addViewController("/404").setViewName("error/not-found");
        registry.addViewController("/500").setViewName("error/server-error");
        
        // PWA-specific pages
        registry.addViewController("/offline").setViewName("pwa/offline");
        registry.addViewController("/install").setViewName("pwa/install");
        
        // Mobile-optimized views
        registry.addViewController("/mobile").setViewName("mobile/dashboard");
        registry.addViewController("/mobile/tasks").setViewName("mobile/tasks");
        
        // API documentation (for development)
        if (isDevelopment()) {
            registry.addViewController("/api/docs").setViewName("api/documentation");
        }
    }

    /**
     * Enhanced CORS configuration for WebSocket and API access.
     * 
     * ✅ PHASE 2C: Added WebSocket origins and mobile app support
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // API endpoints for REST and WebSocket
        registry.addMapping("/api/**")
                .allowedOriginPatterns(getAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        // WebSocket endpoints
        registry.addMapping("/ws/**")
                .allowedOriginPatterns(getAllowedOrigins())
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        // Real-time API endpoints
        registry.addMapping("/realtime/**")
                .allowedOriginPatterns(getAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(1800); // Shorter cache for real-time endpoints
    }

    /**
     * Add interceptors for PWA and mobile optimization.
     * 
     * ✅ PHASE 2C: Mobile detection and PWA capabilities
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Mobile detection interceptor
        registry.addInterceptor(new MobileDetectionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/ws/**", "/static/**");
                
        // PWA installation prompt interceptor
        registry.addInterceptor(new PWAInterceptor())
                .addPathPatterns("/dashboard", "/tasks/**", "/projects/**")
                .excludePathPatterns("/api/**", "/ws/**");
        
        // Performance monitoring for real-time features
        if (isDevelopment()) {
            registry.addInterceptor(new PerformanceMonitoringInterceptor())
                    .addPathPatterns("/**");
        }
    }

    /**
     * Get allowed origins based on environment.
     */
    private String[] getAllowedOrigins() {
        if (isDevelopment()) {
            return new String[]{
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*", // Local network for mobile testing
                "https://localhost:*"  // HTTPS development
            };
        } else {
            // Production origins - should be configured via environment variables
            String productionOrigins = environment.getProperty("app.cors.allowed-origins", "");
            if (!productionOrigins.isEmpty()) {
                return productionOrigins.split(",");
            }
            return new String[]{"https://frcpm.yourteam.org"}; // Default production
        }
    }

    /**
     * Check if running in development mode.
     */
    private boolean isDevelopment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("development".equals(profile) || "dev".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mobile detection interceptor for responsive behavior.
     */
    private static class MobileDetectionInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        
        @Override
        public boolean preHandle(@NonNull jakarta.servlet.http.HttpServletRequest request, 
                               @NonNull jakarta.servlet.http.HttpServletResponse response, 
                               @NonNull Object handler) {
            
            String userAgent = request.getHeader("User-Agent");
            boolean isMobile = userAgent != null && (
                userAgent.contains("Mobile") || 
                userAgent.contains("Android") || 
                userAgent.contains("iPhone") ||
                userAgent.contains("iPad")
            );
            
            request.setAttribute("isMobile", isMobile);
            request.setAttribute("isTablet", userAgent != null && userAgent.contains("iPad"));
            
            // Add mobile-specific headers
            if (isMobile) {
                response.setHeader("X-Mobile-Detected", "true");
            }
            
            return true;
        }
    }

    /**
     * PWA installation prompt interceptor.
     */
    private static class PWAInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        
        @Override
        public boolean preHandle(@NonNull jakarta.servlet.http.HttpServletRequest request, 
                               @NonNull jakarta.servlet.http.HttpServletResponse response, 
                               @NonNull Object handler) {
            
            // Check if request is from a PWA-capable browser
            String userAgent = request.getHeader("User-Agent");
            boolean isPWACapable = userAgent != null && (
                userAgent.contains("Chrome") || 
                userAgent.contains("Firefox") || 
                userAgent.contains("Safari") ||
                userAgent.contains("Edge")
            );
            
            request.setAttribute("isPWACapable", isPWACapable);
            
            // Check if already installed as PWA
            String displayMode = request.getHeader("X-Display-Mode");
            boolean isInstalled = "standalone".equals(displayMode) || "fullscreen".equals(displayMode);
            request.setAttribute("isPWAInstalled", isInstalled);
            
            return true;
        }
    }

    /**
     * Performance monitoring interceptor for development.
     */
    private static class PerformanceMonitoringInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        
        @Override
        public boolean preHandle(@NonNull jakarta.servlet.http.HttpServletRequest request, 
                               @NonNull jakarta.servlet.http.HttpServletResponse response, 
                               @NonNull Object handler) {
            
            request.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }
        
        @Override
        public void afterCompletion(@NonNull jakarta.servlet.http.HttpServletRequest request, 
                                  @NonNull jakarta.servlet.http.HttpServletResponse response, 
                                  @NonNull Object handler, Exception ex) {
            
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                response.setHeader("X-Response-Time", duration + "ms");
                
                // Log slow requests
                if (duration > 1000) {
                    System.out.println(String.format("SLOW REQUEST: %s %s took %dms", 
                                                    request.getMethod(), request.getRequestURI(), duration));
                }
            }
        }
    }

    /**
     * TODO: Phase 2C Advanced Web Features
     * 
     * Additional features to be implemented:
     * 
     * 1. Content Security Policy:
     *    - CSP headers for PWA security
     *    - WebSocket connection security
     *    - Inline script restrictions
     * 
     * 2. Progressive Enhancement:
     *    - Automatic mobile redirect logic
     *    - Bandwidth-aware resource loading
     *    - Graceful WebSocket fallbacks
     * 
     * 3. Performance Optimization:
     *    - Resource bundling and minification
     *    - HTTP/2 push for critical resources
     *    - Lazy loading for non-critical features
     * 
     * 4. Workshop Environment Features:
     *    - Offline-first resource strategies
     *    - QR code scanning page mappings
     *    - Voice command interface routing
     * 
     * Example future implementation:
     * 
     * @Override
     * public void addResourceHandlers(ResourceHandlerRegistry registry) {
     *     // HTTP/2 push for critical PWA resources
     *     registry.addResourceHandler("/critical/**")
     *             .addResourceLocations("classpath:/static/critical/")
     *             .resourceChain(true)
     *             .addResolver(new PushResourceResolver());
     * }
     * 
     * @Bean
     * public SecurityHeadersFilter securityHeadersFilter() {
     *     return new SecurityHeadersFilter();
     * }
     */
}