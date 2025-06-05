package org.frcpm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Web MVC configuration for the FRC Project Management System.
 * 
 * This configuration sets up the web layer infrastructure for the Spring Boot
 * application, replacing the JavaFX UI framework with modern web technologies.
 * 
 * Key responsibilities:
 * - Static resource handling (CSS, JS, images)
 * - View controller mapping for simple pages
 * - CORS configuration for potential API access
 * - Resource caching for performance
 * - Foundation for Progressive Web App features
 * 
 * The configuration supports:
 * - Thymeleaf template rendering
 * - Bootstrap CSS framework integration
 * - Mobile-responsive design resources
 * - Future PWA manifest and service worker files
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since 2.0.0 (Spring Boot Migration)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures static resource handling for web assets.
     * 
     * Sets up resource mappings for:
     * - CSS stylesheets (Bootstrap + custom FRC styles)
     * - JavaScript files (application logic + PWA features)
     * - Images and icons (including PWA icons)
     * - PWA manifest and service worker files
     * 
     * Includes caching headers for performance optimization
     * suitable for FRC team network environments.
     * 
     * @param registry ResourceHandlerRegistry for configuration
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Main static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 24 hours cache
                
        // CSS stylesheets
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(86400);
                
        // JavaScript files
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(86400);
                
        // Images and icons
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(604800); // 7 days cache for images
                
        // PWA resources (manifest, service worker, icons)
        registry.addResourceHandler("/manifest.json")
                .addResourceLocations("classpath:/static/manifest.json")
                .setCachePeriod(3600); // 1 hour cache for manifest
                
        registry.addResourceHandler("/sw.js")
                .addResourceLocations("classpath:/static/sw.js")
                .setCachePeriod(0); // No cache for service worker
                
        // Favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(604800); // 7 days cache
    }

    /**
     * Configures simple view controllers for pages that don't need complex logic.
     * 
     * Maps URLs directly to template names for:
     * - Login page
     * - Error pages
     * - Simple informational pages
     * 
     * This reduces boilerplate controller code for basic pages.
     * 
     * @param registry ViewControllerRegistry for configuration
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Login page
        registry.addViewController("/login").setViewName("auth/login");
        
        // Root redirect to dashboard
        registry.addViewController("/").setViewName("redirect:/dashboard");
        
        // Error pages
        registry.addViewController("/error").setViewName("error/general");
        registry.addViewController("/403").setViewName("error/access-denied");
        registry.addViewController("/404").setViewName("error/not-found");
        registry.addViewController("/500").setViewName("error/server-error");
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for API access.
     * 
     * Phase 1: Basic CORS setup for potential future API consumption
     * Phase 2: Will be enhanced for mobile app integration
     * Phase 3: May include FRC-specific tool integrations
     * 
     * Current configuration allows:
     * - Local development access
     * - Same-origin requests
     * - Standard HTTP methods
     * 
     * @param registry CorsRegistry for configuration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*") // Development
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1 hour preflight cache
    }

    /**
     * TODO: Phase 2 Web Enhancements
     * 
     * The following features will be added in Phase 2:
     * 
     * 1. Progressive Web App Configuration:
     *    - Service worker registration
     *    - Offline page caching strategy
     *    - Background sync capabilities
     *    - Push notification setup
     * 
     * 2. Mobile Optimization:
     *    - Touch-friendly interface resources
     *    - Viewport configuration
     *    - Mobile-specific CSS breakpoints
     *    - Gesture handling for workshop environments
     * 
     * 3. FRC-Specific Features:
     *    - QR code scanning resources
     *    - Workshop environment optimizations
     * 
     * 4. Performance Enhancements:
     *    - Resource compression
     *    - CDN integration for Bootstrap/jQuery
     *    - Lazy loading strategies
     * 
     * Example future configurations:
     * 
     * @Override
     * public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
     *     // API content negotiation for mobile apps
     * }
     * 
     * @Override
     * public void addInterceptors(InterceptorRegistry registry) {
     *     // Performance monitoring
     *     // Security logging
     *     // COPPA compliance checking
     * }
     */
}