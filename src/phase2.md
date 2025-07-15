# Phase 2: Core Web Features & COPPA Compliance - Detailed Implementation Plan

## Overview

Phase 2 builds upon the Spring Boot foundation established in Phase 1 to implement comprehensive security, COPPA compliance, real-time collaboration features, and Progressive Web App capabilities. This phase transforms the basic web application into a production-ready system with enterprise-grade security and modern web features.

## Prerequisites

- Completed Phase 1: Spring Boot web application running
- Basic authentication working (admin/admin)
- Core controllers and templates operational
- Service layer fully migrated to Spring components

---

## Step 1: Enhanced Security Infrastructure

### **User Management & Authentication System**

#### **Create User Domain Models**

**File**: `src/main/java/org/frcpm/models/User.java`
```java
package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;
    
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
    
    @Email
    @NotBlank
    @Column(unique = true)
    private String email;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    
    // COPPA Compliance Fields
    private Integer age;
    private boolean requiresParentalConsent = false;
    private LocalDateTime parentalConsentDate;
    private String parentalConsentToken;
    
    // MFA Fields
    private String totpSecret;
    private boolean mfaEnabled = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TeamMember teamMember;
    
    // Constructors, getters, setters
    public User() {}
    
    public User(String username, String password, String email, String firstName, String lastName, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isMinor() {
        return age != null && age < 18;
    }
    
    public boolean requiresCOPPACompliance() {
        return age != null && age < 13;
    }
    
    // Standard getters and setters...
}
```

**File**: `src/main/java/org/frcpm/models/UserRole.java`
```java
package org.frcpm.models;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    STUDENT("ROLE_STUDENT", "Student team member"),
    MENTOR("ROLE_MENTOR", "Adult mentor or coach"),
    PARENT("ROLE_PARENT", "Parent with limited access"),
    ADMIN("ROLE_ADMIN", "System administrator");
    
    private final String authority;
    private final String description;
    
    UserRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }
    
    @Override
    public String getAuthority() {
        return authority;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isMentor() {
        return this == MENTOR || this == ADMIN;
    }
    
    public boolean isStudent() {
        return this == STUDENT;
    }
    
    public boolean requiresMFA() {
        return this == MENTOR || this == ADMIN;
    }
}
```

#### **Create User Repository**

**File**: `src/main/java/org/frcpm/repositories/spring/UserRepository.java`
```java
package org.frcpm.repositories.spring;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByEnabledTrue();
    
    @Query("SELECT u FROM User u WHERE u.age < 13 AND u.parentalConsentDate IS NULL")
    List<User> findUsersRequiringParentalConsent();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.createdAt > :since")
    List<User> findRecentUsers(LocalDateTime since);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### **User Service Implementation**

**File**: `src/main/java/org/frcpm/services/impl/UserService.java`
```java
package org.frcpm.services.impl;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.frcpm.repositories.spring.UserRepository;
import org.frcpm.security.COPPAComplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final COPPAComplianceService coppaService;
    
    @Autowired
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      COPPAComplianceService coppaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.coppaService = coppaService;
    }
    
    public User createUser(String username, String password, String email, 
                          String firstName, String lastName, UserRole role, Integer age) {
        
        // Validation
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        // Create user
        User user = new User(username, passwordEncoder.encode(password), 
                           email, firstName, lastName, role);
        user.setAge(age);
        
        // COPPA compliance check
        if (user.requiresCOPPACompliance()) {
            user.setRequiresParentalConsent(true);
            coppaService.initiateParentalConsentProcess(user);
        }
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findUsersRequiringParentalConsent() {
        return userRepository.findUsersRequiringParentalConsent();
    }
    
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void enableMFA(Long userId, String totpSecret) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setTotpSecret(totpSecret);
        user.setMfaEnabled(true);
        userRepository.save(user);
    }
}
```

---

## Step 2: COPPA Compliance Framework

### **COPPA Compliance Service**

**File**: `src/main/java/org/frcpm/security/COPPAComplianceService.java`
```java
package org.frcpm.security;

import org.frcpm.models.User;
import org.frcpm.models.AuditLog;
import org.frcpm.repositories.spring.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class COPPAComplianceService {
    
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final EmailService emailService;
    
    @Autowired
    public COPPAComplianceService(UserRepository userRepository,
                                 AuditService auditService,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.emailService = emailService;
    }
    
    public boolean requiresParentalConsent(User user) {
        return user.getAge() != null && user.getAge() < 13;
    }
    
    public void initiateParentalConsentProcess(User user) {
        if (!requiresParentalConsent(user)) {
            return;
        }
        
        // Generate consent token
        String consentToken = UUID.randomUUID().toString();
        user.setParentalConsentToken(consentToken);
        user.setRequiresParentalConsent(true);
        
        userRepository.save(user);
        
        // Send email to parent/guardian
        emailService.sendParentalConsentRequest(user, consentToken);
        
        // Log the action
        auditService.logCOPPAAction("CONSENT_INITIATED", user, 
            "Parental consent process initiated for user under 13");
    }
    
    public boolean processParentalConsent(String consentToken, boolean granted) {
        User user = userRepository.findByParentalConsentToken(consentToken)
            .orElse(null);
            
        if (user == null) {
            return false;
        }
        
        if (granted) {
            user.setParentalConsentDate(LocalDateTime.now());
            user.setRequiresParentalConsent(false);
            user.setEnabled(true);
            
            auditService.logCOPPAAction("CONSENT_GRANTED", user, 
                "Parental consent granted");
        } else {
            user.setEnabled(false);
            auditService.logCOPPAAction("CONSENT_DENIED", user, 
                "Parental consent denied - account disabled");
        }
        
        user.setParentalConsentToken(null); // Clear token
        userRepository.save(user);
        
        return true;
    }
    
    public void enforceDataMinimization(User user) {
        if (!requiresParentalConsent(user)) {
            return;
        }
        
        // Implement data minimization rules for users under 13
        // - Limited profile information
        // - Restricted data collection
        // - Enhanced privacy controls
        
        auditService.logCOPPAAction("DATA_MINIMIZATION", user, 
            "Data minimization rules applied");
    }
    
    public List<User> getUsersRequiringConsentReview() {
        return userRepository.findUsersRequiringParentalConsent();
    }
    
    public void performScheduledCOPPAReview() {
        List<User> usersRequiringReview = getUsersRequiringConsentReview();
        
        for (User user : usersRequiringReview) {
            // Check if consent process has been pending too long
            if (user.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                // Disable account after 7 days without consent
                user.setEnabled(false);
                userRepository.save(user);
                
                auditService.logCOPPAAction("ACCOUNT_DISABLED_NO_CONSENT", user, 
                    "Account disabled due to missing parental consent after 7 days");
            }
        }
    }
}
```

### **Audit Service for COPPA Compliance**

**File**: `src/main/java/org/frcpm/models/AuditLog.java`
```java
package org.frcpm.models;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "subject_user_id")
    private User subjectUser; // User being accessed (for COPPA tracking)
    
    private String action;
    private String description;
    private String ipAddress;
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    private AuditLevel level;
    
    @CreatedDate
    private LocalDateTime timestamp;
    
    // Constructors, getters, setters
    public AuditLog() {}
    
    public AuditLog(User user, String action, String description) {
        this.user = user;
        this.action = action;
        this.description = description;
        this.level = AuditLevel.INFO;
    }
    
    // Standard getters and setters...
}
```

**File**: `src/main/java/org/frcpm/models/AuditLevel.java`
```java
package org.frcpm.models;

public enum AuditLevel {
    INFO,
    WARNING,
    COPPA_COMPLIANCE,
    SECURITY_ALERT
}
```

**File**: `src/main/java/org/frcpm/security/AuditService.java`
```java
package org.frcpm.security;

import org.frcpm.models.AuditLog;
import org.frcpm.models.AuditLevel;
import org.frcpm.models.User;
import org.frcpm.repositories.spring.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    public void logUserAction(User user, String action, String description) {
        logAction(user, null, action, description, AuditLevel.INFO);
    }
    
    public void logCOPPAAction(String action, User subjectUser, String description) {
        User currentUser = getCurrentUser(); // Get from security context
        logAction(currentUser, subjectUser, action, description, AuditLevel.COPPA_COMPLIANCE);
    }
    
    public void logSecurityAlert(User user, String action, String description) {
        logAction(user, null, action, description, AuditLevel.SECURITY_ALERT);
    }
    
    private void logAction(User user, User subjectUser, String action, 
                          String description, AuditLevel level) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setSubjectUser(subjectUser);
        log.setAction(action);
        log.setDescription(description);
        log.setLevel(level);
        
        // Capture request details
        ServletRequestAttributes attrs = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
        
        auditLogRepository.save(log);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private User getCurrentUser() {
        // Implementation to get current user from Spring Security context
        // Will be implemented in security configuration section
        return null; // Placeholder
    }
}
```

---

## Step 3: Enhanced Security Configuration

### **Custom User Details Service**

**File**: `src/main/java/org/frcpm/security/UserDetailsServiceImpl.java`
```java
package org.frcpm.security;

import org.frcpm.models.User;
import org.frcpm.repositories.spring.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new UserPrincipal(user);
    }
}
```

**File**: `src/main/java/org/frcpm/security/UserPrincipal.java`
```java
package org.frcpm.security;

import org.frcpm.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    
    private final User user;
    
    public UserPrincipal(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(user.getRole());
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }
    
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
    
    public User getUser() {
        return user;
    }
    
    public String getFullName() {
        return user.getFullName();
    }
    
    public boolean requiresMFA() {
        return user.getRole().requiresMFA();
    }
    
    public boolean isMFAEnabled() {
        return user.isMfaEnabled();
    }
}
```

### **TOTP MFA Service**

**File**: `src/main/java/org/frcpm/security/TOTPService.java`
```java
package org.frcpm.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TOTPService {
    
    private static final int SECRET_LENGTH = 20;
    private static final int WINDOW_SIZE = 3;
    private static final int TIME_STEP = 30;
    
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[SECRET_LENGTH];
        random.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
    
    public String generateQRCodeUrl(String secret, String username, String issuer) {
        String encodedSecret = secret.replace("=", "");
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s",
            issuer, username, encodedSecret, issuer
        );
    }
    
    public boolean validateToken(String secret, String token) {
        try {
            long timeWindow = System.currentTimeMillis() / 1000 / TIME_STEP;
            
            for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; i++) {
                String expectedToken = generateToken(secret, timeWindow + i);
                if (token.equals(expectedToken)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String generateToken(String secret, long timeWindow) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        byte[] secretBytes = Base64.getDecoder().decode(secret);
        byte[] timeBytes = longToBytes(timeWindow);
        
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA1");
        mac.init(keySpec);
        
        byte[] hash = mac.doFinal(timeBytes);
        int offset = hash[hash.length - 1] & 0xf;
        
        int code = ((hash[offset] & 0x7f) << 24) |
                   ((hash[offset + 1] & 0xff) << 16) |
                   ((hash[offset + 2] & 0xff) << 8) |
                   (hash[offset + 3] & 0xff);
        
        return String.format("%06d", code % 1000000);
    }
    
    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return result;
    }
}
```

### **Enhanced Security Configuration**

**File**: `src/main/java/org/frcpm/config/SecurityConfig.java` (Updated)
```java
package org.frcpm.config;

import org.frcpm.security.UserDetailsServiceImpl;
import org.frcpm.security.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final UserDetailsServiceImpl userDetailsService;
    private final AuditService auditService;
    
    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                         AuditService auditService) {
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/css/**", "/js/**", "/images/**", 
                               "/manifest.json", "/sw.js").permitAll()
                .requestMatchers("/login", "/register", "/coppa/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/mentor/**").hasAnyRole("MENTOR", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .rememberMe(remember -> remember
                .key("frc-project-management")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
            )
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/ws/**")
            );
            
        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            
            // Log successful login
            auditService.logUserAction(principal.getUser(), "LOGIN_SUCCESS", 
                "User logged in successfully");
            
            // Determine redirect based on role and MFA status
            if (principal.requiresMFA() && !principal.isMFAEnabled()) {
                response.sendRedirect("/mfa/setup");
            } else if (principal.requiresMFA() && principal.isMFAEnabled()) {
                response.sendRedirect("/mfa/verify");
            } else {
                // Set session timeout based on role
                int timeout = principal.getUser().getRole().isStudent() ? 900 : 1800; // 15 or 30 minutes
                request.getSession().setMaxInactiveInterval(timeout);
                
                response.sendRedirect("/dashboard");
            }
        };
    }
    
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                auditService.logUserAction(principal.getUser(), "LOGOUT", "User logged out");
            }
            response.sendRedirect("/login?logout=true");
        };
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

---

## Step 4: Real-time Features with WebSocket

### **WebSocket Configuration**

**File**: `src/main/java/org/frcpm/config/WebSocketConfig.java`
```java
package org.frcpm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

### **Real-time Messaging Models**

**File**: `src/main/java/org/frcpm/web/dto/TaskUpdateMessage.java`
```java
package org.frcpm.web.dto;

import java.time.LocalDateTime;

public class TaskUpdateMessage {
    
    private Long taskId;
    private Long projectId;
    private String taskTitle;
    private Integer progress;
    private String status;
    private String updatedBy;
    private LocalDateTime timestamp;
    private String changeType; // CREATED, UPDATED, DELETED, PROGRESS_CHANGED
    
    // Constructors
    public TaskUpdateMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public TaskUpdateMessage(Long taskId, Long projectId, String taskTitle, 
                           Integer progress, String status, String updatedBy, String changeType) {
        this();
        this.taskId = taskId;
        this.projectId = projectId;
        this.taskTitle = taskTitle;
        this.progress = progress;
        this.status = status;
        this.updatedBy = updatedBy;
        this.changeType = changeType;
    }
    
    // Getters and setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
}
```

**File**: `src/main/java/org/frcpm/web/dto/ProjectNotification.java`
```java
package org.frcpm.web.dto;

import java.time.LocalDateTime;

public class ProjectNotification {
    
    private Long projectId;
    private String title;
    private String message;
    private String type; // INFO, WARNING, ALERT, SUCCESS
    private String sender;
    private LocalDateTime timestamp;
    
    public ProjectNotification() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ProjectNotification(Long projectId, String title, String message, 
                             String type, String sender) {
        this();
        this.projectId = projectId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.sender = sender;
    }
    
    // Getters and setters...
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
```

### **WebSocket Controllers**

**File**: `src/main/java/org/frcpm/web/controllers/WebSocketController.java`
```java
package org.frcpm.web.controllers;

import org.frcpm.web.dto.TaskUpdateMessage;
import org.frcpm.web.dto.ProjectNotification;
import org.frcpm.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    @MessageMapping("/task/update")
    @SendTo("/topic/project/{projectId}")
    public TaskUpdateMessage updateTask(@Payload TaskUpdateMessage message,
                                       @AuthenticationPrincipal UserPrincipal user) {
        // Set the user who made the update
        message.setUpdatedBy(user.getFullName());
        
        // Send to all subscribers of this project
        return message;
    }
    
    @MessageMapping("/project/notification")
    public void sendProjectNotification(@Payload ProjectNotification notification,
                                       @AuthenticationPrincipal UserPrincipal user) {
        notification.setSender(user.getFullName());
        
        // Send to all project subscribers
        messagingTemplate.convertAndSend(
            "/topic/project/" + notification.getProjectId() + "/notifications", 
            notification
        );
    }
    
    // Method to send notifications from server-side code
    public void notifyTaskUpdate(TaskUpdateMessage message) {
        messagingTemplate.convertAndSend(
            "/topic/project/" + message.getProjectId(), 
            message
        );
    }
    
    public void notifyProjectUpdate(ProjectNotification notification) {
        messagingTemplate.convertAndSend(
            "/topic/project/" + notification.getProjectId() + "/notifications", 
            notification
        );
    }
}
```

---

## Step 5: Progressive Web App Implementation

### **Service Worker for Offline Capabilities**

**File**: `src/main/resources/static/sw.js`
```javascript
const CACHE_NAME = 'frc-pm-v2.0.0';
const OFFLINE_URL = '/offline.html';

// Cache essential resources
const CACHE_URLS = [
    '/',
    '/dashboard',
    '/css/app.css',
    '/js/app.js',
    '/js/offline-manager.js',
    '/images/icon-192.png',
    '/images/icon-512.png',
    OFFLINE_URL
];

// Install event - cache essential resources
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('Caching essential resources');
                return cache.addAll(CACHE_URLS);
            })
            .then(() => self.skipWaiting())
    );
});

// Activate event - clean up old caches
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheName !== CACHE_NAME) {
                        console.log('Deleting old cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// Fetch event - serve from cache when offline
self.addEventListener('fetch', event => {
    const { request } = event;
    
    // Handle navigation requests
    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request)
                .then(response => {
                    // If online, cache the response
                    if (response.status === 200) {
                        const responseClone = response.clone();
                        caches.open(CACHE_NAME)
                            .then(cache => cache.put(request, responseClone));
                    }
                    return response;
                })
                .catch(() => {
                    // If offline, try cache first, then offline page
                    return caches.match(request)
                        .then(response => response || caches.match(OFFLINE_URL));
                })
        );
        return;
    }
    
    // Handle API requests
    if (request.url.includes('/api/')) {
        event.respondWith(
            fetch(request)
                .then(response => {
                    // Cache successful API responses
                    if (response.status === 200) {
                        const responseClone = response.clone();
                        caches.open(CACHE_NAME)
                            .then(cache => cache.put(request, responseClone));
                    }
                    return response;
                })
                .catch(() => {
                    // Return cached response if available
                    return caches.match(request)
                        .then(response => {
                            if (response) {
                                return response;
                            }
                            // Return offline indicator for API calls
                            return new Response(
                                JSON.stringify({ offline: true, error: 'No network connection' }),
                                { 
                                    status: 503,
                                    headers: { 'Content-Type': 'application/json' }
                                }
                            );
                        });
                })
        );
        return;
    }
    
    // Handle other requests (CSS, JS, images)
    event.respondWith(
        caches.match(request)
            .then(response => {
                if (response) {
                    return response;
                }
                return fetch(request)
                    .then(response => {
                        // Cache successful responses
                        if (response.status === 200) {
                            const responseClone = response.clone();
                            caches.open(CACHE_NAME)
                                .then(cache => cache.put(request, responseClone));
                        }
                        return response;
                    });
            })
    );
});

// Background sync for offline data
self.addEventListener('sync', event => {
    if (event.tag === 'background-sync') {
        event.waitUntil(
            // Send queued data when connection is restored
            syncOfflineData()
        );
    }
});

// Handle push notifications
self.addEventListener('push', event => {
    if (event.data) {
        const data = event.data.json();
        event.waitUntil(
            self.registration.showNotification(data.title, {
                body: data.body,
                icon: '/images/icon-192.png',
                badge: '/images/badge-72.png',
                data: data.url
            })
        );
    }
});

// Handle notification clicks
self.addEventListener('notificationclick', event => {
    event.notification.close();
    
    if (event.notification.data) {
        event.waitUntil(
            self.clients.openWindow(event.notification.data)
        );
    }
});

async function syncOfflineData() {
    try {
        // Get offline data from IndexedDB
        const offlineData = await getOfflineData();
        
        for (const item of offlineData) {
            try {
                await fetch(item.url, {
                    method: item.method,
                    headers: item.headers,
                    body: item.body
                });
                
                // Remove from offline storage after successful sync
                await removeOfflineData(item.id);
            } catch (error) {
                console.log('Failed to sync item:', item.id);
            }
        }
    } catch (error) {
        console.log('Background sync failed:', error);
    }
}

// Placeholder functions for IndexedDB operations
async function getOfflineData() {
    // Implementation for retrieving offline data from IndexedDB
    return [];
}

async function removeOfflineData(id) {
    // Implementation for removing synced data from IndexedDB
}
```

### **Offline Data Manager**

**File**: `src/main/resources/static/js/offline-manager.js`
```javascript
class OfflineManager {
    constructor() {
        this.dbName = 'FRCProjectManagement';
        this.dbVersion = 1;
        this.db = null;
        this.isOnline = navigator.onLine;
        
        this.init();
        this.setupEventListeners();
    }
    
    async init() {
        try {
            this.db = await this.openDB();
            console.log('Offline manager initialized');
        } catch (error) {
            console.error('Failed to initialize offline manager:', error);
        }
    }
    
    openDB() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(this.dbName, this.dbVersion);
            
            request.onerror = () => reject(request.error);
            request.onsuccess = () => resolve(request.result);
            
            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                
                // Create stores for offline data
                if (!db.objectStoreNames.contains('tasks')) {
                    const taskStore = db.createObjectStore('tasks', { keyPath: 'id' });
                    taskStore.createIndex('projectId', 'projectId', { unique: false });
                    taskStore.createIndex('lastModified', 'lastModified', { unique: false });
                }
                
                if (!db.objectStoreNames.contains('projects')) {
                    const projectStore = db.createObjectStore('projects', { keyPath: 'id' });
                    projectStore.createIndex('lastModified', 'lastModified', { unique: false });
                }
                
                if (!db.objectStoreNames.contains('pendingChanges')) {
                    const changesStore = db.createObjectStore('pendingChanges', { 
                        keyPath: 'id', 
                        autoIncrement: true 
                    });
                    changesStore.createIndex('timestamp', 'timestamp', { unique: false });
                }
            };
        });
    }
    
    setupEventListeners() {
        // Listen for online/offline events
        window.addEventListener('online', () => {
            this.isOnline = true;
            this.showStatus('Connection restored', 'success');
            this.syncPendingChanges();
        });
        
        window.addEventListener('offline', () => {
            this.isOnline = false;
            this.showStatus('Working offline', 'warning');
        });
        
        // Listen for beforeunload to save any pending data
        window.addEventListener('beforeunload', () => {
            this.savePendingChanges();
        });
    }
    
    async cacheTaskData(projectId) {
        if (!this.db) return;
        
        try {
            const response = await fetch(`/api/tasks?projectId=${projectId}`);
            const tasks = await response.json();
            
            const transaction = this.db.transaction(['tasks'], 'readwrite');
            const store = transaction.objectStore('tasks');
            
            for (const task of tasks) {
                task.lastModified = Date.now();
                await this.putData(store, task);
            }
            
            console.log(`Cached ${tasks.length} tasks for project ${projectId}`);
        } catch (error) {
            console.error('Failed to cache task data:', error);
        }
    }
    
    async getCachedTasks(projectId) {
        if (!this.db) return [];
        
        try {
            const transaction = this.db.transaction(['tasks'], 'readonly');
            const store = transaction.objectStore('tasks');
            const index = store.index('projectId');
            
            return await this.getAllData(index, projectId);
        } catch (error) {
            console.error('Failed to get cached tasks:', error);
            return [];
        }
    }
    
    async updateTaskOffline(task) {
        if (!this.db) return false;
        
        try {
            // Update local cache
            const transaction = this.db.transaction(['tasks', 'pendingChanges'], 'readwrite');
            
            // Update task in cache
            task.lastModified = Date.now();
            task.modifiedOffline = true;
            await this.putData(transaction.objectStore('tasks'), task);
            
            // Queue change for sync
            const change = {
                type: 'UPDATE_TASK',
                data: task,
                timestamp: Date.now(),
                url: `/api/tasks/${task.id}`,
                method: 'PUT'
            };
            await this.putData(transaction.objectStore('pendingChanges'), change);
            
            console.log('Task updated offline:', task.id);
            return true;
        } catch (error) {
            console.error('Failed to update task offline:', error);
            return false;
        }
    }
    
    async syncPendingChanges() {
        if (!this.db || !this.isOnline) return;
        
        try {
            const transaction = this.db.transaction(['pendingChanges'], 'readonly');
            const store = transaction.objectStore('pendingChanges');
            const changes = await this.getAllData(store);
            
            let syncedCount = 0;
            for (const change of changes) {
                try {
                    const response = await fetch(change.url, {
                        method: change.method,
                        headers: {
                            'Content-Type': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest'
                        },
                        body: JSON.stringify(change.data)
                    });
                    
                    if (response.ok) {
                        // Remove successfully synced change
                        await this.deleteData('pendingChanges', change.id);
                        syncedCount++;
                    }
                } catch (error) {
                    console.error('Failed to sync change:', change.id, error);
                }
            }
            
            if (syncedCount > 0) {
                this.showStatus(`Synced ${syncedCount} changes`, 'success');
            }
        } catch (error) {
            console.error('Failed to sync pending changes:', error);
        }
    }
    
    async resolveConflicts(localData, serverData) {
        // Simple conflict resolution: server wins for now
        // In production, implement more sophisticated conflict resolution
        console.log('Resolving conflict between local and server data');
        return serverData;
    }
    
    showStatus(message, type = 'info') {
        // Create or update status indicator
        let statusElement = document.getElementById('offline-status');
        if (!statusElement) {
            statusElement = document.createElement('div');
            statusElement.id = 'offline-status';
            statusElement.className = 'alert alert-dismissible fade show position-fixed';
            statusElement.style.cssText = 'top: 20px; right: 20px; z-index: 1050; min-width: 300px;';
            document.body.appendChild(statusElement);
        }
        
        statusElement.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        statusElement.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (statusElement && statusElement.parentNode) {
                statusElement.remove();
            }
        }, 5000);
    }
    
    // Helper methods for IndexedDB operations
    putData(store, data) {
        return new Promise((resolve, reject) => {
            const request = store.put(data);
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }
    
    getAllData(store, query = null) {
        return new Promise((resolve, reject) => {
            const request = query ? store.getAll(query) : store.getAll();
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }
    
    deleteData(storeName, id) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([storeName], 'readwrite');
            const store = transaction.objectStore(storeName);
            const request = store.delete(id);
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }
}

// Initialize offline manager
const offlineManager = new OfflineManager();

// Export for use in other scripts
window.offlineManager = offlineManager;
```

---

## Step 6: Enhanced Authentication UI

### **Login Page with MFA Support**

**File**: `src/main/resources/templates/auth/login.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - FRC Project Management</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/app.css}" rel="stylesheet">
    
    <style>
        .login-container {
            min-height: 100vh;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        .login-card {
            max-width: 400px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        }
        .login-header {
            background: var(--frc-blue);
            color: white;
            text-align: center;
            padding: 2rem;
        }
        .age-verification {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 0.375rem;
            padding: 1rem;
            margin-bottom: 1rem;
        }
    </style>
</head>
<body>
    <div class="login-container d-flex align-items-center justify-content-center">
        <div class="login-card card">
            <div class="login-header">
                <h2 class="mb-0">FRC Project Management</h2>
                <p class="mb-0">Team Login</p>
            </div>
            
            <div class="card-body p-4">
                <!-- Alert Messages -->
                <div th:if="${param.error}" class="alert alert-danger">
                    Invalid username or password.
                </div>
                <div th:if="${param.logout}" class="alert alert-success">
                    You have been logged out successfully.
                </div>
                <div th:if="${param.expired}" class="alert alert-warning">
                    Your session has expired. Please log in again.
                </div>
                
                <!-- Login Form -->
                <form th:action="@{/login}" method="post" class="needs-validation" novalidate>
                    <div class="mb-3">
                        <label for="username" class="form-label">Username</label>
                        <input type="text" class="form-control" id="username" name="username" required>
                        <div class="invalid-feedback">
                            Please provide a valid username.
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                        <div class="invalid-feedback">
                            Please provide a password.
                        </div>
                    </div>
                    
                    <!-- Age Verification for COPPA Compliance -->
                    <div class="age-verification">
                        <h6 class="fw-bold">Age Verification</h6>
                        <p class="small mb-2">For compliance with children's privacy laws, please verify your age:</p>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="ageVerification" id="age13Plus" value="13plus" required>
                            <label class="form-check-label" for="age13Plus">
                                I am 13 years old or older
                            </label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="ageVerification" id="ageUnder13" value="under13" required>
                            <label class="form-check-label" for="ageUnder13">
                                I am under 13 years old
                            </label>
                        </div>
                        <div class="invalid-feedback">
                            Please verify your age to continue.
                        </div>
                    </div>
                    
                    <div class="mb-3 form-check">
                        <input type="checkbox" class="form-check-input" id="rememberMe" name="remember-me">
                        <label class="form-check-label" for="rememberMe">
                            Remember me
                        </label>
                    </div>
                    
                    <button type="submit" class="btn btn-primary w-100 mb-3">Login</button>
                </form>
                
                <!-- Additional Links -->
                <div class="text-center">
                    <a href="#" class="text-decoration-none small">Forgot Password?</a>
                    <span class="text-muted small mx-2">|</span>
                    <a th:href="@{/register}" class="text-decoration-none small">Register</a>
                </div>
                
                <!-- COPPA Notice -->
                <div class="mt-4 p-3 bg-light rounded">
                    <h6 class="fw-bold mb-2">Privacy Notice</h6>
                    <p class="small mb-0">
                        We comply with the Children's Online Privacy Protection Act (COPPA). 
                        Users under 13 require parental consent before accessing the system.
                        <a href="/privacy" target="_blank">Learn more</a>
                    </p>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // Form validation
        (function() {
            'use strict';
            window.addEventListener('load', function() {
                var forms = document.getElementsByClassName('needs-validation');
                var validation = Array.prototype.filter.call(forms, function(form) {
                    form.addEventListener('submit', function(event) {
                        if (form.checkValidity() === false) {
                            event.preventDefault();
                            event.stopPropagation();
                        }
                        form.classList.add('was-validated');
                    }, false);
                });
            }, false);
        })();
        
        // Handle age verification
        document.addEventListener('DOMContentLoaded', function() {
            const ageRadios = document.querySelectorAll('input[name="ageVerification"]');
            const under13Radio = document.getElementById('ageUnder13');
            
            ageRadios.forEach(radio => {
                radio.addEventListener('change', function() {
                    if (under13Radio.checked) {
                        // Show COPPA warning for under-13 users
                        alert('Users under 13 require parental consent. You will be redirected to the consent process after login.');
                    }
                });
            });
        });
    </script>
</body>
</html>
```

### **MFA Setup Page**

**File**: `src/main/resources/templates/auth/mfa-setup.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title>Setup Two-Factor Authentication</title>
</head>
<body>
    <div layout:fragment="content">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0">Setup Two-Factor Authentication</h4>
                        <p class="mb-0 small">Required for mentors and administrators</p>
                    </div>
                    
                    <div class="card-body">
                        <div class="alert alert-info">
                            <h6 class="alert-heading">Enhanced Security Required</h6>
                            <p class="mb-0">
                                As a mentor or administrator, you must enable two-factor authentication (2FA) 
                                to protect student data and comply with our security policies.
                            </p>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <h5>Step 1: Install Authenticator App</h5>
                                <p>Download and install one of these apps on your mobile device:</p>
                                <ul>
                                    <li>Google Authenticator</li>
                                    <li>Microsoft Authenticator</li>
                                    <li>Authy</li>
                                    <li>1Password</li>
                                </ul>
                                
                                <h5 class="mt-4">Step 2: Scan QR Code</h5>
                                <p>Use your authenticator app to scan this QR code:</p>
                                
                                <div class="text-center mb-3">
                                    <div id="qrcode" class="border p-3 d-inline-block">
                                        <!-- QR Code will be generated here -->
                                        <img th:src="${qrCodeUrl}" alt="QR Code for 2FA setup" class="img-fluid" style="max-width: 200px;">
                                    </div>
                                </div>
                                
                                <div class="alert alert-secondary">
                                    <strong>Manual Setup:</strong> If you can't scan the QR code, 
                                    manually enter this secret: <code th:text="${secret}">SECRET_KEY</code>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <h5>Step 3: Verify Setup</h5>
                                <p>Enter the 6-digit code from your authenticator app to complete setup:</p>
                                
                                <form th:action="@{/mfa/setup}" method="post" class="needs-validation" novalidate>
                                    <input type="hidden" name="secret" th:value="${secret}">
                                    
                                    <div class="mb-3">
                                        <label for="verificationCode" class="form-label">Verification Code</label>
                                        <input type="text" class="form-control text-center" 
                                               id="verificationCode" name="verificationCode" 
                                               maxlength="6" pattern="[0-9]{6}" 
                                               placeholder="000000" required>
                                        <div class="invalid-feedback">
                                            Please enter a 6-digit verification code.
                                        </div>
                                    </div>
                                    
                                    <button type="submit" class="btn btn-primary w-100">
                                        Complete Setup
                                    </button>
                                </form>
                                
                                <div class="mt-4">
                                    <h6>Recovery Codes</h6>
                                    <p class="small">
                                        After setup is complete, you'll receive recovery codes. 
                                        Store them safely - they can be used if you lose access to your authenticator app.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <div layout:fragment="scripts">
        <script>
            // Auto-format verification code input
            document.getElementById('verificationCode').addEventListener('input', function(e) {
                // Remove any non-digit characters
                this.value = this.value.replace(/\D/g, '');
                
                // Limit to 6 digits
                if (this.value.length > 6) {
                    this.value = this.value.substring(0, 6);
                }
            });
            
            // Auto-submit when 6 digits are entered
            document.getElementById('verificationCode').addEventListener('input', function(e) {
                if (this.value.length === 6) {
                    // Validate the form and submit if valid
                    const form = this.closest('form');
                    if (form.checkValidity()) {
                        form.submit();
                    }
                }
            });
        </script>
    </div>
</body>
</html>
```

---

## Step 7: REST API Controllers

### **Task API Controller for Real-time Updates**

**File**: `src/main/java/org/frcpm/web/api/TaskApiController.java`
```java
package org.frcpm.web.api;

import org.frcpm.models.Task;
import org.frcpm.services.impl.TaskService;
import org.frcpm.web.controllers.WebSocketController;
import org.frcpm.web.dto.TaskUpdateMessage;
import org.frcpm.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@PreAuthorize("isAuthenticated()")
public class TaskApiController {
    
    private final TaskService taskService;
    private final WebSocketController webSocketController;
    
    @Autowired
    public TaskApiController(TaskService taskService, WebSocketController webSocketController) {
        this.taskService = taskService;
        this.webSocketController = webSocketController;
    }
    
    @GetMapping
    public ResponseEntity<List<Task>> getTasks(@RequestParam Long projectId,
                                              @AuthenticationPrincipal UserPrincipal user) {
        try {
            var project = taskService.getProjectService().findById(projectId);
            if (project.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Task> tasks = taskService.findByProject(project.get());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal user) {
        var task = taskService.findById(id);
        return task.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task,
                                          @AuthenticationPrincipal UserPrincipal user) {
        try {
            Task savedTask = taskService.save(task);
            
            // Send real-time notification
            TaskUpdateMessage message = new TaskUpdateMessage(
                savedTask.getId(),
                savedTask.getProject().getId(),
                savedTask.getTitle(),
                savedTask.getProgress(),
                savedTask.getCompleted() ? "COMPLETED" : "IN_PROGRESS",
                user.getFullName(),
                "CREATED"
            );
            webSocketController.notifyTaskUpdate(message);
            
            return ResponseEntity.ok(savedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
                                          @Valid @RequestBody Task task,
                                          @AuthenticationPrincipal UserPrincipal user) {
        try {
            var existingTask = taskService.findById(id);
            if (existingTask.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check permissions - students can only update tasks assigned to them
            if (user.getUser().getRole().isStudent()) {
                boolean isAssigned = existingTask.get().getAssignedTo().stream()
                    .anyMatch(member -> member.getUser() != null && 
                             member.getUser().getId().equals(user.getUser().getId()));
                if (!isAssigned) {
                    return ResponseEntity.status(403).build();
                }
            }
            
            task.setId(id);
            Task savedTask = taskService.save(task);
            
            // Send real-time notification
            TaskUpdateMessage message = new TaskUpdateMessage(
                savedTask.getId(),
                savedTask.getProject().getId(),
                savedTask.getTitle(),
                savedTask.getProgress(),
                savedTask.getCompleted() ? "COMPLETED" : "IN_PROGRESS",
                user.getFullName(),
                "UPDATED"
            );
            webSocketController.notifyTaskUpdate(message);
            
            return ResponseEntity.ok(savedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/progress")
    public ResponseEntity<Void> updateProgress(@PathVariable Long id,
                                              @RequestParam Integer progress,
                                              @AuthenticationPrincipal UserPrincipal user) {
        try {
            var task = taskService.findById(id);
            if (task.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update progress
            boolean completed = progress >= 100;
            taskService.updateTaskProgress(id, progress, completed);
            
            // Send real-time notification
            TaskUpdateMessage message = new TaskUpdateMessage(
                id,
                task.get().getProject().getId(),
                task.get().getTitle(),
                progress,
                completed ? "COMPLETED" : "IN_PROGRESS",
                user.getFullName(),
                "PROGRESS_CHANGED"
            );
            webSocketController.notifyTaskUpdate(message);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                          @AuthenticationPrincipal UserPrincipal user) {
        try {
            var task = taskService.findById(id);
            if (task.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            taskService.deleteById(id);
            
            // Send real-time notification
            TaskUpdateMessage message = new TaskUpdateMessage(
                id,
                task.get().getProject().getId(),
                task.get().getTitle(),
                null,
                "DELETED",
                user.getFullName(),
                "DELETED"
            );
            webSocketController.notifyTaskUpdate(message);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

### **Offline Data Controller**

**File**: `src/main/java/org/frcpm/web/api/OfflineDataController.java`
```java
package org.frcpm.web.api;

import org.frcpm.models.Task;
import org.frcpm.models.Project;
import org.frcpm.services.impl.TaskService;
import org.frcpm.services.impl.ProjectService;
import org.frcpm.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/offline")
public class OfflineDataController {
    
    private final TaskService taskService;
    private final ProjectService projectService;
    
    @Autowired
    public OfflineDataController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }
    
    @GetMapping("/sync-data")
    public ResponseEntity<Map<String, Object>> getSyncData(
            @RequestParam Long projectId,
            @RequestParam(required = false) String lastSync,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            var project = projectService.findById(projectId);
            if (project.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> syncData = new HashMap<>();
            
            // Get tasks for the project
            List<Task> tasks = taskService.findByProject(project.get());
            syncData.put("tasks", tasks);
            
            // Get project details
            syncData.put("project", project.get());
            
            // Add sync metadata
            syncData.put("syncTimestamp", LocalDateTime.now());
            syncData.put("version", "2.0.0");
            
            return ResponseEntity.ok(syncData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/sync-changes")
    public ResponseEntity<Map<String, Object>> syncChanges(
            @RequestBody List<Map<String, Object>> changes,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        
        for (Map<String, Object> change : changes) {
            try {
                String changeType = (String) change.get("type");
                Map<String, Object> data = (Map<String, Object>) change.get("data");
                
                switch (changeType) {
                    case "UPDATE_TASK":
                        processTaskUpdate(data, user);
                        successCount++;
                        break;
                    case "CREATE_TASK":
                        processTaskCreation(data, user);
                        successCount++;
                        break;
                    default:
                        errorCount++;
                        break;
                }
            } catch (Exception e) {
                errorCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("syncTimestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(result);
    }
    
    private void processTaskUpdate(Map<String, Object> data, UserPrincipal user) {
        Long taskId = Long.valueOf(data.get("id").toString());
        Integer progress = (Integer) data.get("progress");
        Boolean completed = (Boolean) data.get("completed");
        
        if (progress != null) {
            taskService.updateTaskProgress(taskId, progress, completed != null ? completed : false);
        }
    }
    
    private void processTaskCreation(Map<String, Object> data, UserPrincipal user) {
        // Implementation for creating tasks from offline data
        // This would involve converting the Map data to a Task object and saving it
    }
    
    @GetMapping("/manifest")
    public ResponseEntity<Map<String, Object>> getOfflineManifest() {
        Map<String, Object> manifest = new HashMap<>();
        manifest.put("version", "2.0.0");
        manifest.put("offlineCapabilities", List.of(
            "task_viewing",
            "task_progress_updates", 
            "project_viewing",
            "basic_team_info"
        ));
        manifest.put("maxOfflineDays", 7);
        manifest.put("syncRequired", List.of(
            "task_creation",
            "task_deletion",
            "project_changes",
            "team_member_changes"
        ));
        
        return ResponseEntity.ok(manifest);
    }
}
```

---

## Step 8: Enhanced Templates with Real-time Features

### **Enhanced Dashboard with WebSocket Integration**

**File**: `src/main/resources/templates/dashboard/index.html` (Enhanced)
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title>Dashboard</title>
</head>
<body>
    <div layout:fragment="content">
        <!-- Offline Status Indicator -->
        <div id="connection-status" class="alert alert-info alert-dismissible fade show d-none" role="alert">
            <span id="connection-message">Connecting...</span>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        
        <div class="row">
            <div class="col-12">
                <h1>Project Dashboard</h1>
                <span id="last-updated" class="text-muted small"></span>
            </div>
        </div>
        
        <!-- Project Selection -->
        <div class="row mb-4" th:if="${projects != null and !projects.empty}">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">Select Project</h5>
                    </div>
                    <div class="card-body">
                        <select class="form-select" id="projectSelector" onchange="selectProject()">
                            <option value="">Choose a project...</option>
                            <option th:each="project : ${projects}" 
                                    th:value="${project.id}" 
                                    th:text="${project.name}"
                                    th:selected="${currentProject != null and currentProject.id == project.id}">
                            </option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">Quick Actions</h5>
                    </div>
                    <div class="card-body">
                        <a th:href="@{/projects/new}" class="btn btn-primary me-2">New Project</a>
                        <a th:href="@{/projects}" class="btn btn-outline-primary me-2">All Projects</a>
                        <button id="refreshData" class="btn btn-outline-secondary" onclick="refreshDashboard()">
                            <i class="fas fa-sync-alt"></i> Refresh
                        </button>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Real-time Activity Feed -->
        <div th:if="${currentProject != null}" class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Recent Activity</h5>
                        <span class="badge bg-success" id="online-users">0 online</span>
                    </div>
                    <div class="card-body">
                        <div id="activity-feed" class="activity-feed" style="max-height: 200px; overflow-y: auto;">
                            <!-- Real-time activities will appear here -->
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Project Overview with Real-time Updates -->
        <div th:if="${currentProject != null}">
            <div class="row mb-4">
                <div class="col-12">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0" th:text="${currentProject.name}">Project Name</h5>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-3">
                                    <strong>Start Date:</strong>
                                    <span th:text="${#temporals.format(currentProject.startDate, 'MMM dd, yyyy')}"></span>
                                </div>
                                <div class="col-md-3">
                                    <strong>Goal End Date:</strong>
                                    <span th:text="${#temporals.format(currentProject.goalEndDate, 'MMM dd, yyyy')}"></span>
                                </div>
                                <div class="col-md-3">
                                    <strong>Hard Deadline:</strong>
                                    <span th:text="${#temporals.format(currentProject.hardDeadline, 'MMM dd, yyyy')}"></span>
                                </div>
                                <div class="col-md-3">
                                    <strong>Progress:</strong>
                                    <div class="progress mt-1">
                                        <div id="project-progress-bar" class="progress-bar" role="progressbar" 
                                             style="width: 0%" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                            0%
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Task and Milestone Lists with Real-time Updates -->
            <div class="row">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h5 class="mb-0">Upcoming Tasks</h5>
                            <span class="badge bg-primary" id="task-count">0</span>
                        </div>
                        <div class="card-body">
                            <div id="upcoming-tasks">
                                <div th:if="${upcomingTasks != null and !upcomingTasks.empty}">
                                    <div class="list-group">
                                        <div th:each="task : ${upcomingTasks}" 
                                             class="list-group-item task-item" 
                                             th:data-task-id="${task.id}">
                                            <div class="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <h6 class="mb-1 task-title" th:text="${task.title}">Task Title</h6>
                                                    <small class="text-muted" 
                                                           th:text="${'Due: ' + #temporals.format(task.endDate, 'MMM dd')}">
                                                    </small>
                                                </div>
                                                <span class="badge bg-primary rounded-pill task-progress" 
                                                      th:text="${task.progress + '%'}">0%</span>
                                            </div>
                                            <div class="progress mt-2" style="height: 4px;">
                                                <div class="progress-bar task-progress-bar" 
                                                     role="progressbar" 
                                                     th:style="'width: ' + ${task.progress} + '%'"
                                                     th:aria-valuenow="${task.progress}">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div th:if="${upcomingTasks == null or upcomingTasks.empty}" 
                                     class="text-muted text-center py-3">
                                    No upcoming tasks
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Upcoming Milestones</h5>
                        </div>
                        <div class="card-body">
                            <div id="upcoming-milestones">
                                <div th:if="${upcomingMilestones != null and !upcomingMilestones.empty}">
                                    <div class="list-group">
                                        <div th:each="milestone : ${upcomingMilestones}" class="list-group-item">
                                            <h6 class="mb-1" th:text="${milestone.name}">Milestone Name</h6>
                                            <small class="text-muted" 
                                                   th:text="${#temporals.format(milestone.date, 'MMM dd, yyyy')}">
                                            </small>
                                        </div>
                                    </div>
                                </div>
                                <div th:if="${upcomingMilestones == null or upcomingMilestones.empty}" 
                                     class="text-muted text-center py-3">
                                    No upcoming milestones
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- No Project Selected -->
        <div th:if="${currentProject == null}" class="text-center py-5">
            <h3 class="text-muted">Select a project to view dashboard</h3>
            <p class="text-muted">Choose a project from the dropdown above or create a new one.</p>
        </div>
    </div>
    
    <div layout:fragment="scripts">
        <!-- SockJS and STOMP for WebSocket -->
        <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>
        
        <script th:inline="javascript">
            let stompClient = null;
            let currentProjectId = /*[[${currentProject?.id}]]*/ null;
            let isConnected = false;
            
            function selectProject() {
                const projectId = document.getElementById('projectSelector').value;
                if (projectId) {
                    window.location.href = '/dashboard?projectId=' + projectId;
                }
            }
            
            function refreshDashboard() {
                if (currentProjectId && offlineManager.isOnline) {
                    // Refresh data from server
                    fetch(`/api/offline/sync-data?projectId=${currentProjectId}`)
                        .then(response => response.json())
                        .then(data => {
                            updateTaskList(data.tasks);
                            updateLastUpdated();
                        })
                        .catch(error => {
                            console.error('Failed to refresh dashboard:', error);
                            // Fall back to cached data if offline
                            if (window.offlineManager) {
                                offlineManager.getCachedTasks(currentProjectId)
                                    .then(tasks => updateTaskList(tasks));
                            }
                        });
                }
            }
            
            function connectWebSocket() {
                if (!currentProjectId) return;
                
                const socket = new SockJS('/ws');
                stompClient = new StompJs.Client({
                    webSocketFactory: () => socket,
                    reconnectDelay: 5000,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000
                });
                
                stompClient.onConnect = function(frame) {
                    console.log('Connected to WebSocket');
                    isConnected = true;
                    updateConnectionStatus('Connected', 'success');
                    
                    // Subscribe to project updates
                    stompClient.subscribe(`/topic/project/${currentProjectId}`, function(message) {
                        const taskUpdate = JSON.parse(message.body);
                        handleTaskUpdate(taskUpdate);
                    });
                    
                    // Subscribe to project notifications
                    stompClient.subscribe(`/topic/project/${currentProjectId}/notifications`, function(message) {
                        const notification = JSON.parse(message.body);
                        showNotification(notification);
                    });
                };
                
                stompClient.onStompError = function(frame) {
                    console.error('STOMP error:', frame);
                    isConnected = false;
                    updateConnectionStatus('Connection error', 'danger');
                };
                
                stompClient.onWebSocketClose = function(event) {
                    console.log('WebSocket connection closed');
                    isConnected = false;
                    updateConnectionStatus('Disconnected', 'warning');
                };
                
                stompClient.activate();
            }
            
            function handleTaskUpdate(taskUpdate) {
                console.log('Received task update:', taskUpdate);
                
                // Update task in the UI
                const taskElement = document.querySelector(`[data-task-id="${taskUpdate.taskId}"]`);
                if (taskElement) {
                    const progressElement = taskElement.querySelector('.task-progress');
                    const progressBarElement = taskElement.querySelector('.task-progress-bar');
                    
                    if (progressElement && taskUpdate.progress !== null) {
                        progressElement.textContent = taskUpdate.progress + '%';
                        progressBarElement.style.width = taskUpdate.progress + '%';
                        progressBarElement.setAttribute('aria-valuenow', taskUpdate.progress);
                    }
                    
                    // Add visual feedback for updates
                    taskElement.classList.add('border-primary');
                    setTimeout(() => {
                        taskElement.classList.remove('border-primary');
                    }, 3000);
                }
                
                // Add to activity feed
                addToActivityFeed(taskUpdate);
                
                // Update project progress
                updateProjectProgress();
            }
            
            function addToActivityFeed(taskUpdate) {
                const activityFeed = document.getElementById('activity-feed');
                if (!activityFeed) return;
                
                const activityItem = document.createElement('div');
                activityItem.className = 'activity-item border-bottom pb-2 mb-2';
                
                const now = new Date();
                const timeString = now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                
                let message = '';
                switch (taskUpdate.changeType) {
                    case 'CREATED':
                        message = `created task "${taskUpdate.taskTitle}"`;
                        break;
                    case 'UPDATED':
                        message = `updated task "${taskUpdate.taskTitle}"`;
                        break;
                    case 'PROGRESS_CHANGED':
                        message = `updated progress on "${taskUpdate.taskTitle}" to ${taskUpdate.progress}%`;
                        break;
                    case 'DELETED':
                        message = `deleted task "${taskUpdate.taskTitle}"`;
                        break;
                    default:
                        message = `modified "${taskUpdate.taskTitle}"`;
                }
                
                activityItem.innerHTML = `
                    <div class="d-flex justify-content-between">
                        <span><strong>${taskUpdate.updatedBy}</strong> ${message}</span>
                        <small class="text-muted">${timeString}</small>
                    </div>
                `;
                
                // Add to top of feed
                activityFeed.insertBefore(activityItem, activityFeed.firstChild);
                
                // Keep only last 10 items
                while (activityFeed.children.length > 10) {
                    activityFeed.removeChild(activityFeed.lastChild);
                }
            }
            
            function updateConnectionStatus(message, type) {
                const statusElement = document.getElementById('connection-status');
                const messageElement = document.getElementById('connection-message');
                
                if (statusElement && messageElement) {
                    messageElement.textContent = message;
                    statusElement.className = `alert alert-${type} alert-dismissible fade show`;
                    
                    if (type === 'success') {
                        // Hide success message after 3 seconds
                        setTimeout(() => {
                            statusElement.classList.add('d-none');
                        }, 3000);
                    } else {
                        statusElement.classList.remove('d-none');
                    }
                }
            }
            
            function updateTaskList(tasks) {
                // Update the task list with new data
                const taskListElement = document.getElementById('upcoming-tasks');
                // Implementation would update the DOM with new task data
            }
            
            function updateProjectProgress() {
                // Calculate and update overall project progress
                if (currentProjectId) {
                    fetch(`/api/projects/${currentProjectId}/progress`)
                        .then(response => response.json())
                        .then(data => {
                            const progressBar = document.getElementById('project-progress-bar');
                            if (progressBar) {
                                progressBar.style.width = data.progress + '%';
                                progressBar.textContent = data.progress + '%';
                                progressBar.setAttribute('aria-valuenow', data.progress);
                            }
                        })
                        .catch(error => console.error('Failed to update project progress:', error));
                }
            }
            
            function updateLastUpdated() {
                const lastUpdatedElement = document.getElementById('last-updated');
                if (lastUpdatedElement) {
                    const now = new Date();
                    lastUpdatedElement.textContent = `Last updated: ${now.toLocaleTimeString()}`;
                }
            }
            
            function showNotification(notification) {
                // Show Bootstrap toast notification
                const toastContainer = document.getElementById('toast-container') || createToastContainer();
                
                const toast = document.createElement('div');
                toast.className = 'toast';
                toast.setAttribute('role', 'alert');
                toast.innerHTML = `
                    <div class="toast-header">
                        <strong class="me-auto">${notification.title}</strong>
                        <small>${new Date(notification.timestamp).toLocaleTimeString()}</small>
                        <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
                    </div>
                    <div class="toast-body">
                        ${notification.message}
                    </div>
                `;
                
                toastContainer.appendChild(toast);
                
                const bsToast = new bootstrap.Toast(toast);
                bsToast.show();
                
                // Remove after hiding
                toast.addEventListener('hidden.bs.toast', () => {
                    toast.remove();
                });
            }
            
            function createToastContainer() {
                const container = document.createElement('div');
                container.id = 'toast-container';
                container.className = 'toast-container position-fixed top-0 end-0 p-3';
                container.style.zIndex = '1100';
                document.body.appendChild(container);
                return container;
            }
            
            // Initialize when page loads
            document.addEventListener('DOMContentLoaded', function() {
                updateLastUpdated();
                
                if (currentProjectId) {
                    connectWebSocket();
                    
                    // Cache initial data for offline use
                    if (window.offlineManager) {
                        offlineManager.cacheTaskData(currentProjectId);
                    }
                }
            });
            
            // Cleanup on page unload
            window.addEventListener('beforeunload', function() {
                if (stompClient && isConnected) {
                    stompClient.deactivate();
                }
            });
        </script>
    </div>
</body>
</html>
```

---

## Step 9: Application Configuration Updates

### **Updated Application Properties for Phase 2**

**File**: `src/main/resources/application.yml` (Enhanced)
```yaml
spring:
  application:
    name: frc-project-management
  
  profiles:
    active: development
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
    defer-datasource-initialization: true
    
  sql:
    init:
      mode: embedded
      
  h2:
    console:
      enabled: true
      path: /h2-console
      
  thymeleaf:
    cache: false
    mode: HTML
    encoding: UTF-8
    prefix: classpath:/templates/
    suffix: .html
    
  web:
    resources:
      static-locations: classpath:/static/
      cache:
        period: 86400
        
  security:
    user:
      name: admin
      password: admin
      roles: ADMIN
      
  # WebSocket Configuration
  websocket:
    allowed-origins: "*"
    
  # Session Management
  session:
    store-type: jdbc
    timeout: 1800 # 30 minutes default
    
  # Email Configuration (for COPPA notifications)
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME:}
    password: ${EMAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# Application-specific configuration
app:
  name: FRC Project Management System
  version: 2.0.0
  description: Project management system for FIRST Robotics Competition teams
  
  # COPPA Compliance
  coppa:
    enabled: true
    consent-email-template: classpath:templates/email/parental-consent.html
    consent-url-base: ${BASE_URL:http://localhost:8080}/coppa/consent
    consent-expiry-days: 30
    
  # Security Settings
  security:
    mfa:
      issuer: "FRC Project Management"
      token-length: 6
      window-size: 3
      secret-length: 20
    
    session:
      student-timeout: 900    # 15 minutes for students
      mentor-timeout: 1800    # 30 minutes for mentors
      remember-me-validity: 86400  # 24 hours
    
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-numbers: true
      require-special-chars: false
      
  # PWA Configuration
  pwa:
    enabled: true
    cache-version: "v2.0.0"
    offline-pages:
      - "/dashboard"
      - "/tasks"
      - "/offline.html"
    max-offline-days: 7
    
  # WebSocket Configuration
  websocket:
    heartbeat-interval: 25000
    disconnect-delay: 5000
    max-text-message-size: 8192
    max-binary-message-size: 8192

logging:
  level:
    org.frcpm: DEBUG
    org.springframework.security: DEBUG
    org.springframework.messaging: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/frc-project-management.log

server:
  port: 8080
  servlet:
    context-path: /
  error:
    include-message: always
    include-binding-errors: always
    
  # Session configuration
  servlet:
    session:
      cookie:
        secure: false  # Set to true in production with HTTPS
        http-only: true
        same-site: strict

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,sessions
  endpoint:
    health:
      show-details: when-authorized
    sessions:
      enabled: true

# Actuator info
info:
  app:
    name: ${app.name}
    version: ${app.version}
    description: ${app.description}
  java:
    version: ${java.version}
  spring:
    version: ${spring-boot.version}
```

### **Email Templates for COPPA Compliance**

**File**: `src/main/resources/templates/email/parental-consent.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Parental Consent Required</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #0066cc; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; background: #f9f9f9; }
        .button { display: inline-block; padding: 12px 24px; background: #0066cc; color: white; text-decoration: none; border-radius: 4px; margin: 10px 5px; }
        .footer { font-size: 12px; color: #666; padding: 20px; text-align: center; }
        .important { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 4px; margin: 15px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Parental Consent Required</h1>
            <p>FRC Project Management System</p>
        </div>
        
        <div class="content">
            <p>Dear Parent/Guardian,</p>
            
            <p>Your child, <strong th:text="${user.fullName}">Student Name</strong>, has requested access to our FRC Project Management System. Because your child is under 13 years old, federal law (COPPA - Children's Online Privacy Protection Act) requires us to obtain your consent before we can collect, use, or disclose their personal information.</p>
            
            <div class="important">
                <h3>What information we collect:</h3>
                <ul>
                    <li>Name and username</li>
                    <li>Team assignment and role</li>
                    <li>Task assignments and progress</li>
                    <li>Meeting attendance records</li>
                    <li>Basic contact information</li>
                </ul>
                
                <h3>How we use this information:</h3>
                <ul>
                    <li>Coordinate robotics team activities</li>
                    <li>Track project progress and assignments</li>
                    <li>Facilitate team communication</li>
                    <li>Ensure student safety and supervision</li>
                </ul>
            </div>
            
            <p>We take your child's privacy seriously and implement appropriate safeguards to protect their information. We do not share personal information with third parties except as required by law or with your explicit consent.</p>
            
            <h3>Your Options:</h3>
            <p>Please click one of the buttons below to indicate your decision:</p>
            
            <div style="text-align: center; margin: 30px 0;">
                <a th:href="${consentUrl + '?token=' + consentToken + '&consent=true'}" 
                   class="button" style="background: #28a745;">
                    ✓ I Give Consent
                </a>
                
                <a th:href="${consentUrl + '?token=' + consentToken + '&consent=false'}" 
                   class="button" style="background: #dc3545;">
                    ✗ I Do Not Give Consent
                </a>
            </div>
            
            <div class="important">
                <h3>Important Notes:</h3>
                <ul>
                    <li>This consent is required only once</li>
                    <li>You may revoke consent at any time by contacting us</li>
                    <li>If consent is not provided, your child cannot access the system</li>
                    <li>This link expires in 30 days</li>
                </ul>
            </div>
            
            <p>If you have any questions about this request or our privacy practices, please contact:</p>
            <ul>
                <li>Email: <a th:href="'mailto:' + ${teamEmail}">${teamEmail}</a></li>
                <li>Phone: <span th:text="${teamPhone}">Team Phone</span></li>
            </ul>
            
            <p>Thank you for supporting your child's participation in FIRST Robotics Competition.</p>
            
            <p>Sincerely,<br>
            <span th:text="${teamName}">Team Name</span><br>
            FRC Team <span th:text="${teamNumber}">Team Number</span></p>
        </div>
        
        <div class="footer">
            <p>This email was sent because your child requested access to our FRC Project Management System. If you believe this was sent in error, please contact us immediately.</p>
            <p><small>Token: <span th:text="${consentToken}">Token</span></small></p>
        </div>
    </div>
</body>
</html>
```

---

## Step 10: Testing Infrastructure for Phase 2

### **Security Integration Tests**

**File**: `src/test/java/org/frcpm/security/SecurityIntegrationTest.java`
```java
package org.frcpm.security;

import org.frcpm.FrcProjectManagementApplication;
import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.frcpm.services.impl.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FrcProjectManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
    
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void testStudentCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));
    }
    
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void testStudentCannotAccessAdminArea() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "mentor", roles = "MENTOR")
    void testMentorCanAccessMentorArea() throws Exception {
        mockMvc.perform(get("/mentor/reports"))
                .andExpected(status().isOk());
    }
    
    @Test
    void testCOPPAComplianceForUnder13User() {
        // Create user under 13
        User user = userService.createUser("young_student", "password123", 
                                         "young@example.com", "Young", "Student", 
                                         UserRole.STUDENT, 12);
        
        // Verify COPPA compliance requirements
        assertTrue(user.requiresCOPPACompliance());
        assertTrue(user.isRequiresParentalConsent());
    }
}
```

### **WebSocket Integration Tests**

**File**: `src/test/java/org/frcpm/websocket/WebSocketIntegrationTest.java`
```java
package org.frcpm.websocket;

import org.frcpm.FrcProjectManagementApplication;
import org.frcpm.web.dto.TaskUpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FrcProjectManagementApplication.class, 
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    private StompSession stompSession;
    
    @BeforeEach
    void setUp() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        
        String url = "ws://localhost:" + port + "/ws";
        stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testTaskUpdateBroadcast() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TaskUpdateMessage> receivedMessage = new AtomicReference<>();
        
        // Subscribe to project updates
        stompSession.subscribe("/topic/project/1", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaskUpdateMessage.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessage.set((TaskUpdateMessage) payload);
                latch.countDown();
            }
        });
        
        // Send a task update
        TaskUpdateMessage message = new TaskUpdateMessage(1L, 1L, "Test Task", 50, "IN_PROGRESS", "Test User", "UPDATED");
        stompSession.send("/app/task/update", message);
        
        // Wait for message to be received
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Verify received message
        TaskUpdateMessage received = receivedMessage.get();
        assertNotNull(received);
        assertEquals("Test Task", received.getTaskTitle());
        assertEquals(50, received.getProgress());
    }
}
```

---

## Step 11: Deployment Configuration

### **Docker Configuration for Development**

**File**: `Dockerfile.dev`
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src/ ./src/

# Build application
RUN ./mvnw clean package -DskipTests

# Run application
CMD ["java", "-jar", "target/frc-project-management-2.0.0.jar", "--spring.profiles.active=development"]

EXPOSE 8080
```

**File**: `docker-compose.yml`
```yaml
version: '3.8'

services:
  frc-app:
    build:
      context: .
      dockerfile: Dockerfile.dev
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - DATABASE_URL=jdbc:h2:./db/frc-project-dev
    volumes:
      - ./db:/app/db
      - ./logs:/app/logs
    networks:
      - frc-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  frc-network:
    driver: bridge
```

### **Environment-specific Scripts**

**File**: `scripts/run-dev.sh`
```bash
#!/bin/bash

echo "Starting FRC Project Management System - Phase 2 Development Mode..."

# Set development profile
export SPRING_PROFILES_ACTIVE=development

# Create necessary directories
mkdir -p db
mkdir -p logs

# Set up environment variables for COPPA testing
export EMAIL_USERNAME=""
export EMAIL_PASSWORD=""
export BASE_URL="http://localhost:8080"

# Run the application with Phase 2 features
mvn spring-boot:run -Dspring-boot.run.profiles=development

echo "Application started at http://localhost:8080"
echo "H2 Console available at http://localhost:8080/h2-console"
echo "WebSocket endpoint at ws://localhost:8080/ws"
```

**File**: `scripts/test-phase2.sh`
```bash
#!/bin/bash

echo "Running Phase 2 Tests..."

# Run security tests
mvn test -Dtest="*Security*Test" -Dspring.profiles.active=test

# Run WebSocket tests
mvn test -Dtest="*WebSocket*Test" -Dspring.profiles.active=test

# Run COPPA compliance tests
mvn test -Dtest="*COPPA*Test" -Dspring.profiles.active=test

# Run integration tests
mvn test -Dtest="*Integration*Test" -Dspring.profiles.active=test

echo "Phase 2 tests completed"
```

---

## Expected Outcomes

Upon successful completion of Phase 2, you will have:

### **✅ Enhanced Security Framework**
- **User Management**: Complete user registration, authentication, and role-based access control
- **COPPA Compliance**: Automated parental consent process for users under 13
- **MFA Implementation**: TOTP-based two-factor authentication for mentors and administrators
- **Audit Logging**: Comprehensive audit trails for all user actions and data access

### **✅ Real-time Collaboration**
- **WebSocket Integration**: Live updates for task progress, project changes, and team activities
- **Activity Feeds**: Real-time activity streams showing team member actions
- **Instant Notifications**: Toast notifications for important updates and alerts
- **Multi-user Support**: Concurrent user sessions with real-time synchronization

### **✅ Progressive Web App Capabilities**
- **Offline Functionality**: 7-day offline access with local data caching
- **Service Worker**: Intelligent caching and background synchronization
- **Mobile Optimization**: Responsive design optimized for tablets and smartphones
- **App-like Experience**: Installable PWA with native app behavior

### **✅ Enhanced User Experience**
- **Modern Authentication**: Secure login with age verification and consent workflows
- **Mobile-First Design**: Touch-friendly interface optimized for workshop environments
- **Real-time Updates**: Live data synchronization across all connected devices
- **Offline Resilience**: Seamless offline/online transitions with conflict resolution

### **⚠️ Ready for Phase 3**
- **FRC Integration Points**: Architecture prepared for competition API integration
- **Advanced Features Foundation**: Real-time framework ready for build season workflows
- **Mobile Workshop Features**: Offline capabilities ready for QR code scanning and voice input
- **Compliance Framework**: COPPA implementation ready for team-specific customization

This phase establishes enterprise-grade security, modern web capabilities, and real-time collaboration features while maintaining compliance with student data protection regulations. The foundation is now ready for FRC-specific optimizations and advanced workshop features.