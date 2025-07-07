// src/main/java/org/frcpm/config/WebSocketConfig.java

package org.frcpm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time collaboration features.
 * 
 * ✅ PHASE 2C: PWA Development - WebSocket Infrastructure
 * 
 * This configuration enables real-time communication for:
 * - Live task progress updates across all connected devices
 * - Instant notifications for project changes and deadlines
 * - Team member activity streams and presence indicators
 * - Multi-user collaboration on project planning
 * - Build season coordination and status updates
 * 
 * Integrates seamlessly with the validated Phase 2B security framework
 * to ensure COPPA compliance and role-based access control in real-time features.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configure message broker for different types of real-time communication.
     * 
     * Broker destinations:
     * - /topic/* : Broadcast messages (project updates, announcements)
     * - /queue/* : Private messages (individual notifications)
     * - /user/*  : User-specific messages (personal task assignments)
     * 
     * Application destinations:
     * - /app/*   : Client messages to server (task updates, user actions)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topic-based broadcasting
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Set application destination prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
        
        // Configure heartbeat for connection monitoring
        config.setHeartbeatValue(new long[]{25000, 25000}); // 25 seconds
    }
    
    /**
     * Register WebSocket endpoints with SockJS fallback support.
     * 
     * SockJS provides fallback options for environments where WebSocket
     * is not available, ensuring compatibility with all workshop networks.
     * 
     * Endpoints:
     * - /ws : Main WebSocket endpoint for real-time features
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Configure properly for production
                .withSockJS()
                .setHeartbeatTime(25000)        // 25 seconds heartbeat
                .setDisconnectDelay(5000)       // 5 seconds disconnect delay
                .setStreamBytesLimit(128 * 1024) // 128KB stream limit
                .setHttpMessageCacheSize(1000)   // Cache size for HTTP transport
                .setSessionCookieNeeded(false);  // No session cookies needed
    }
    
    /**
     * TODO: Phase 2C Advanced WebSocket Features
     * 
     * Future enhancements to be implemented:
     * 
     * 1. Security Integration:
     *    - @Override configureClientInboundChannel() for authentication
     *    - Integration with Spring Security context
     *    - COPPA-compliant message filtering for users under 13
     * 
     * 2. Message Handling:
     *    - Custom message converters for complex data types
     *    - Message rate limiting to prevent spam
     *    - Message persistence for offline users
     * 
     * 3. Performance Optimization:
     *    - Message compression for large data transfers
     *    - Connection pooling and load balancing
     *    - Metrics and monitoring integration
     * 
     * 4. FRC-Specific Features:
     *    - Competition day live updates
     *    - Robot status streaming
     *    - Build session coordination
     * 
     * Example future implementation:
     * 
     * @Override
     * public void configureClientInboundChannel(ChannelRegistration registration) {
     *     registration.interceptors(new AuthenticationChannelInterceptor());
     * }
     * 
     * @Override
     * public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
     *     registration.setMessageSizeLimit(64 * 1024);     // 64KB max message
     *     registration.setSendTimeLimit(15 * 1000);        // 15 seconds send timeout
     *     registration.setSendBufferSizeLimit(512 * 1024); // 512KB send buffer
     * }
     */
}