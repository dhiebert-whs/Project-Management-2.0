package org.frcpm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QRCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    
    // In-memory cache for QR codes and tokens - in production, use Redis or database
    private final ConcurrentHashMap<String, QRCodeToken> tokenCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, QRCodeData> qrCodeCache = new ConcurrentHashMap<>();
    
    public static class QRCodeToken {
        private final String token;
        private final LocalDateTime expiryTime;
        private final String sessionId;
        private final String action; // "CHECK_IN", "CHECK_OUT", "TOOL_CHECKOUT", etc.
        
        public QRCodeToken(String token, LocalDateTime expiryTime, String sessionId, String action) {
            this.token = token;
            this.expiryTime = expiryTime;
            this.sessionId = sessionId;
            this.action = action;
        }
        
        public boolean isValid() {
            return LocalDateTime.now().isBefore(expiryTime);
        }
        
        // Getters
        public String getToken() { return token; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
        public String getSessionId() { return sessionId; }
        public String getAction() { return action; }
    }
    
    public static class QRCodeData {
        private final String data;
        private final String format; // "BASE64", "SVG", "PNG"
        private final int size;
        private final LocalDateTime createdAt;
        
        public QRCodeData(String data, String format, int size) {
            this.data = data;
            this.format = format;
            this.size = size;
            this.createdAt = LocalDateTime.now();
        }
        
        // Getters
        public String getData() { return data; }
        public String getFormat() { return format; }
        public int getSize() { return size; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
    
    /**
     * Generate a secure token for QR code operations
     */
    public String generateToken(String sessionId, String action, int validityMinutes) {
        // Generate a secure random token
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Create expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(validityMinutes);
        
        // Store token in cache
        QRCodeToken qrToken = new QRCodeToken(token, expiryTime, sessionId, action);
        tokenCache.put(token, qrToken);
        
        logger.info("Generated QR token for session {} with action {} (expires: {})", 
                   sessionId, action, expiryTime);
        
        return token;
    }
    
    /**
     * Validate a QR code token
     */
    public boolean validateToken(String token) {
        QRCodeToken qrToken = tokenCache.get(token);
        if (qrToken == null) {
            logger.warn("Token not found: {}", token);
            return false;
        }
        
        if (!qrToken.isValid()) {
            logger.warn("Token expired: {} (expired: {})", token, qrToken.getExpiryTime());
            tokenCache.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get token information
     */
    public QRCodeToken getTokenInfo(String token) {
        QRCodeToken qrToken = tokenCache.get(token);
        if (qrToken != null && qrToken.isValid()) {
            return qrToken;
        }
        return null;
    }
    
    /**
     * Generate QR code for session check-in
     */
    public String generateSessionCheckInQR(String sessionId, int validityMinutes) {
        String token = generateToken(sessionId, "CHECK_IN", validityMinutes);
        
        // Create QR code data structure
        String qrData = String.format("{\"action\":\"CHECK_IN\",\"session\":\"%s\",\"token\":\"%s\",\"timestamp\":\"%s\"}", 
                                     sessionId, token, LocalDateTime.now().format(formatter));
        
        return generateQRCode(qrData, 200);
    }
    
    /**
     * Generate QR code for session check-out
     */
    public String generateSessionCheckOutQR(String sessionId, int validityMinutes) {
        String token = generateToken(sessionId, "CHECK_OUT", validityMinutes);
        
        String qrData = String.format("{\"action\":\"CHECK_OUT\",\"session\":\"%s\",\"token\":\"%s\",\"timestamp\":\"%s\"}", 
                                     sessionId, token, LocalDateTime.now().format(formatter));
        
        return generateQRCode(qrData, 200);
    }
    
    /**
     * Generate QR code for tool checkout
     */
    public String generateToolCheckoutQR(String sessionId, String toolName, int validityMinutes) {
        String token = generateToken(sessionId, "TOOL_CHECKOUT", validityMinutes);
        
        String qrData = String.format("{\"action\":\"TOOL_CHECKOUT\",\"session\":\"%s\",\"tool\":\"%s\",\"token\":\"%s\",\"timestamp\":\"%s\"}", 
                                     sessionId, toolName, token, LocalDateTime.now().format(formatter));
        
        return generateQRCode(qrData, 200);
    }
    
    /**
     * Generate QR code for quick task update
     */
    public String generateTaskUpdateQR(String taskId, String action, int validityMinutes) {
        String token = generateToken(taskId, "TASK_UPDATE", validityMinutes);
        
        String qrData = String.format("{\"action\":\"TASK_UPDATE\",\"task\":\"%s\",\"update\":\"%s\",\"token\":\"%s\",\"timestamp\":\"%s\"}", 
                                     taskId, action, token, LocalDateTime.now().format(formatter));
        
        return generateQRCode(qrData, 200);
    }
    
    /**
     * Generate QR code with specified data and size
     */
    private String generateQRCode(String data, int size) {
        try {
            // For now, return a placeholder base64 QR code
            // In production, this would use a QR code library like ZXing
            String qrCodeKey = generateQRCodeKey(data, size);
            
            // Check cache first
            QRCodeData cached = qrCodeCache.get(qrCodeKey);
            if (cached != null) {
                return cached.getData();
            }
            
            // Generate QR code (placeholder implementation)
            String base64QRCode = generatePlaceholderQRCode(data, size);
            
            // Cache the result
            QRCodeData qrData = new QRCodeData(base64QRCode, "BASE64", size);
            qrCodeCache.put(qrCodeKey, qrData);
            
            logger.debug("Generated QR code for data length: {}, size: {}", data.length(), size);
            
            return base64QRCode;
            
        } catch (Exception e) {
            logger.error("Error generating QR code: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate SVG QR code
     */
    public String generateSVGQRCode(String data, int size) {
        // Placeholder for SVG QR code generation
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + size + "\" height=\"" + size + "\">" +
               "<rect width=\"100%\" height=\"100%\" fill=\"white\"/>" +
               "<text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dy=\"0.3em\">QR Code</text>" +
               "</svg>";
    }
    
    /**
     * Clean up expired tokens
     */
    public void cleanupExpiredTokens() {
        int removed = 0;
        for (String token : tokenCache.keySet()) {
            QRCodeToken qrToken = tokenCache.get(token);
            if (qrToken != null && !qrToken.isValid()) {
                tokenCache.remove(token);
                removed++;
            }
        }
        
        if (removed > 0) {
            logger.info("Cleaned up {} expired QR tokens", removed);
        }
    }
    
    /**
     * Get active token count
     */
    public int getActiveTokenCount() {
        return tokenCache.size();
    }
    
    /**
     * Get QR code cache size
     */
    public int getQRCodeCacheSize() {
        return qrCodeCache.size();
    }
    
    /**
     * Invalidate a specific token
     */
    public void invalidateToken(String token) {
        tokenCache.remove(token);
        logger.info("Invalidated token: {}", token);
    }
    
    /**
     * Invalidate all tokens for a session
     */
    public void invalidateSessionTokens(String sessionId) {
        int removed = 0;
        for (String token : tokenCache.keySet()) {
            QRCodeToken qrToken = tokenCache.get(token);
            if (qrToken != null && sessionId.equals(qrToken.getSessionId())) {
                tokenCache.remove(token);
                removed++;
            }
        }
        
        if (removed > 0) {
            logger.info("Invalidated {} tokens for session {}", removed, sessionId);
        }
    }
    
    private String generateQRCodeKey(String data, int size) {
        return String.format("%s_%d_%d", 
                           Integer.toHexString(data.hashCode()), 
                           size, 
                           System.currentTimeMillis() / 300000); // 5-minute buckets
    }
    
    private String generatePlaceholderQRCode(String data, int size) {
        // This is a placeholder - in production, use ZXing or similar library
        // For now, return a simple base64 encoded placeholder
        String placeholder = "data:image/svg+xml;base64," + 
                           Base64.getEncoder().encodeToString(
                               String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">" +
                                           "<rect width=\"100%%\" height=\"100%%\" fill=\"white\" stroke=\"black\"/>" +
                                           "<text x=\"50%%\" y=\"50%%\" text-anchor=\"middle\" dy=\"0.3em\" font-size=\"12\">QR Code</text>" +
                                           "</svg>", size, size).getBytes()
                           );
        
        return placeholder;
    }
}

// Note: To implement actual QR code generation, you would need to add dependencies like:
// <dependency>
//     <groupId>com.google.zxing</groupId>
//     <artifactId>core</artifactId>
//     <version>3.5.1</version>
// </dependency>
// <dependency>
//     <groupId>com.google.zxing</groupId>
//     <artifactId>javase</artifactId>
//     <version>3.5.1</version>
// </dependency>