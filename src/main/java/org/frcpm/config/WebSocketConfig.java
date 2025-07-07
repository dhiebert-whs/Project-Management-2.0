// src/main/java/org/frcpm/config/WebSocketConfig.java - FIXED VERSION

package org.frcpm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time collaboration features.
 * 
 * 🔧 FIXED: This will compile correctly after adding spring-boot-starter-websocket dependency
 * 
 * ⚠️  PREREQUISITE: Add this to pom.xml:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-websocket</artifactId>
 * </dependency>
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
     * 🔧 FIXED: Removed setHeartbeatValue() - method doesn't exist in newer Spring versions
     * Use heartbeat configuration in SockJS instead.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topic-based broadcasting
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Set application destination prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
        
        // 🔧 REMOVED: setHeartbeatValue() - not available in current Spring versions
        // Heartbeat is configured in SockJS configuration below
    }
    
    /**
     * Register WebSocket endpoints with SockJS fallback support.
     * 
     * 🔧 FIXED: Simplified SockJS configuration for compatibility
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // For development - restrict in production
                .withSockJS()
                .setHeartbeatTime(25000)        // 25 seconds heartbeat
                .setDisconnectDelay(5000);      // 5 seconds disconnect delay
                
        // 🔧 REMOVED: Some SockJS methods might not be available in all versions
        // Keep only essential configuration for maximum compatibility
    }
    
    // 🎯 FUTURE ENHANCEMENTS: Add these methods when needed
    
    /*
    // Security integration (requires spring-security-messaging)
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticationChannelInterceptor());
    }
    
    // Message size and timeout configuration
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024);     // 64KB max message
        registration.setSendTimeLimit(15 * 1000);        // 15 seconds send timeout
        registration.setSendBufferSizeLimit(512 * 1024); // 512KB send buffer
    }
    */
}

/*
🚀 STEP-BY-STEP FIX PROCESS:

1. ✅ ADD WEBSOCKET DEPENDENCY TO pom.xml:
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-websocket</artifactId>
   </dependency>

2. ✅ RELOAD MAVEN PROJECT:
   - Right-click project in IDE
   - Select "Maven" > "Reload project"
   - Wait for dependencies to download

3. ✅ VERIFY IMPORTS RESOLVE:
   - Check that all red underlines disappear
   - Imports should be found automatically

4. ✅ TEST COMPILATION:
   - Save file
   - Check for compilation errors
   - Should compile cleanly

🔍 TROUBLESHOOTING:

If still having issues:

1. Check Spring Boot version in pom.xml parent:
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.2.0</version>
   </parent>

2. Force clean build:
   mvn clean compile

3. Check IDE project refresh:
   - File > Invalidate Caches and Restart (IntelliJ)
   - Project > Clean (Eclipse)

4. Verify Maven dependencies:
   mvn dependency:tree | grep websocket
*/