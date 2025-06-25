// src/main/java/org/frcpm/security/TOTPService.java

package org.frcpm.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Time-based One-Time Password (TOTP) service for multi-factor authentication.
 * 
 * Implements RFC 6238 TOTP algorithm for generating and validating
 * time-based authentication codes. Required for mentor and admin users
 * to ensure security of student data access.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Service
public class TOTPService {
    
    private static final int SECRET_LENGTH = 20; // 160 bits
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int WINDOW_SIZE = 1; // Allow 1 time step tolerance
    private static final int CODE_DIGITS = 6; // 6-digit codes
    private static final String CRYPTO_ALGORITHM = "HmacSHA1";
    
    /**
     * Generates a new random secret for TOTP authentication.
     * 
     * @return Base32-encoded secret string
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[SECRET_LENGTH];
        random.nextBytes(secret);
        return encodeBase32(secret);
    }
    
    /**
     * Generates a QR code URL for setting up TOTP in authenticator apps.
     * 
     * @param secret the user's TOTP secret
     * @param username the username
     * @param issuer the application name
     * @return QR code URL for authenticator app setup
     */
    public String generateQRCodeUrl(String secret, String username, String issuer) {
        try {
            String encodedSecret = secret.replace("=", ""); // Remove padding
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
            
            return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                encodedIssuer, encodedUsername, encodedSecret, encodedIssuer, CODE_DIGITS, TIME_STEP
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code URL", e);
        }
    }
    
    /**
     * Validates a TOTP code against the provided secret.
     * 
     * @param secret the user's TOTP secret
     * @param token the 6-digit code to validate
     * @return true if the token is valid
     */
    public boolean validateToken(String secret, String token) {
        if (secret == null || token == null) {
            return false;
        }
        
        try {
            long timeWindow = getCurrentTimeWindow();
            
            // Check current time window and adjacent windows for clock skew tolerance
            for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; i++) {
                String expectedToken = generateToken(secret, timeWindow + i);
                if (token.equals(expectedToken)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return false; // Validation failed
        }
    }
    
    /**
     * Generates a TOTP token for the current time.
     * Primarily used for testing purposes.
     * 
     * @param secret the user's TOTP secret
     * @return 6-digit TOTP code
     */
    public String generateCurrentToken(String secret) {
        try {
            return generateToken(secret, getCurrentTimeWindow());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP token", e);
        }
    }
    
    /**
     * Gets the remaining seconds until the current TOTP token expires.
     * Useful for UI display to show users when to refresh.
     * 
     * @return seconds remaining until token expiration
     */
    public int getSecondsUntilExpiration() {
        long currentTime = System.currentTimeMillis() / 1000;
        long timeInWindow = currentTime % TIME_STEP;
        return (int) (TIME_STEP - timeInWindow);
    }
    
    /**
     * Validates the format of a TOTP secret.
     * 
     * @param secret the secret to validate
     * @return true if the secret is properly formatted
     */
    public boolean isValidSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Try to decode the secret
            decodeBase32(secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validates the format of a TOTP token.
     * 
     * @param token the token to validate
     * @return true if the token format is valid
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null) {
            return false;
        }
        
        // Remove any whitespace
        token = token.replaceAll("\\s", "");
        
        // Must be exactly 6 digits
        return token.matches("^\\d{6}$");
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Generates a TOTP token for a specific time window.
     */
    private String generateToken(String secret, long timeWindow) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        byte[] secretBytes = decodeBase32(secret);
        byte[] timeBytes = longToBytes(timeWindow);
        
        Mac mac = Mac.getInstance(CRYPTO_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, CRYPTO_ALGORITHM);
        mac.init(keySpec);
        
        byte[] hash = mac.doFinal(timeBytes);
        
        // Dynamic truncation as per RFC 4226
        int offset = hash[hash.length - 1] & 0x0f;
        
        int code = ((hash[offset] & 0x7f) << 24) |
                   ((hash[offset + 1] & 0xff) << 16) |
                   ((hash[offset + 2] & 0xff) << 8) |
                   (hash[offset + 3] & 0xff);
        
        code = code % (int) Math.pow(10, CODE_DIGITS);
        
        return String.format("%0" + CODE_DIGITS + "d", code);
    }
    
    /**
     * Gets the current time window for TOTP calculation.
     */
    private long getCurrentTimeWindow() {
        return System.currentTimeMillis() / 1000 / TIME_STEP;
    }
    
    /**
     * Converts a long value to 8-byte array (big-endian).
     */
    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return result;
    }
    
    /**
     * Encodes byte array to Base32 string.
     * Simplified implementation for TOTP secrets.
     */
    private String encodeBase32(byte[] data) {
        String base32Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder result = new StringBuilder();
        
        int buffer = 0;
        int bufferSize = 0;
        
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bufferSize += 8;
            
            while (bufferSize >= 5) {
                int index = (buffer >> (bufferSize - 5)) & 0x1F;
                result.append(base32Alphabet.charAt(index));
                bufferSize -= 5;
            }
        }
        
        if (bufferSize > 0) {
            int index = (buffer << (5 - bufferSize)) & 0x1F;
            result.append(base32Alphabet.charAt(index));
        }
        
        // Add padding
        while (result.length() % 8 != 0) {
            result.append('=');
        }
        
        return result.toString();
    }
    
    /**
     * Decodes Base32 string to byte array.
     * Simplified implementation for TOTP secrets.
     */
    private byte[] decodeBase32(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("Invalid Base32 string");
        }
        
        encoded = encoded.toUpperCase().replaceAll("=", "");
        String base32Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        
        int[] values = new int[encoded.length()];
        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            int index = base32Alphabet.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }
            values[i] = index;
        }
        
        int outputLength = encoded.length() * 5 / 8;
        byte[] result = new byte[outputLength];
        
        int buffer = 0;
        int bufferSize = 0;
        int resultIndex = 0;
        
        for (int value : values) {
            buffer = (buffer << 5) | value;
            bufferSize += 5;
            
            if (bufferSize >= 8) {
                result[resultIndex++] = (byte) ((buffer >> (bufferSize - 8)) & 0xFF);
                bufferSize -= 8;
            }
        }
        
        return result;
    }
}