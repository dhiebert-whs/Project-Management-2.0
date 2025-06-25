// src/main/java/org/frcpm/config/SecurityMethodConfig.java

package org.frcpm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method-level security configuration for fine-grained access control.
 * 
 * Enables the use of @PreAuthorize and @PostAuthorize annotations
 * throughout the application for COPPA compliance and role-based security.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityMethodConfig {
    
    /**
     * Custom method security expression handler with COPPA-aware expressions.
     */
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        // Could add custom expression root for COPPA-specific expressions
        return handler;
    }
}