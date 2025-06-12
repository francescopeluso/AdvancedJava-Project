package wordageddon.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for secure password management.
 * Provides methods for password hashing and verification using SHA-256 with salt.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class PasswordUtils {
    
    // Salt length in bytes
    private static final int SALT_LENGTH = 16;
    // Hashing algorithm used
    private static final String HASH_ALGORITHM = "SHA-256";
    // Delimiter between salt and hashed password
    private static final String DELIMITER = ":";
    
    /**
     * Generates a secure hash for a password using SHA-256 with a random salt.
     * 
     * @param password The password to hash
     * @return String containing the salt and hash separated by a delimiter
     * @throws RuntimeException if the hashing algorithm is not available
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Generate the hash of the password with salt
            byte[] hashedPassword = hashWithSalt(password.toCharArray(), salt);
            
            // Convert salt and hash to Base64 strings
            String saltString = Base64.getEncoder().encodeToString(salt);
            String hashString = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Return salt + delimiter + hash
            return saltString + DELIMITER + hashString;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while hashing the password", e);
        }
    }
    
    /**
     * Verifies if a password matches a stored hash.
     * 
     * @param password The password to verify
     * @param storedHash The stored hash to compare the password against
     * @return true if the password matches the hash, false otherwise
     * @throws RuntimeException if the hashing algorithm is not available or if the hash format is invalid
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Extract salt and hash from the stored string
            String[] parts = storedHash.split(DELIMITER);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid hash format");
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Generate the hash of the provided password with the same salt
            byte[] testHash = hashWithSalt(password.toCharArray(), salt);
            
            // Compare hashes with constant time to prevent timing attacks
            return MessageDigest.isEqual(hash, testHash);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            throw new RuntimeException("Error during password verification", e);
        }
    }
    
    /**
     * Generates a hash for a password with a specific salt.
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The byte array containing the hash
     * @throws NoSuchAlgorithmException if the hashing algorithm is not available
     */
    private static byte[] hashWithSalt(char[] password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.reset();
        md.update(salt);
        
        // Convert the password from char[] to byte[]
        byte[] passwordBytes = new byte[password.length];
        for (int i = 0; i < password.length; i++) {
            passwordBytes[i] = (byte) password[i];
        }
        
        md.update(passwordBytes);
        return md.digest();
    }
}
