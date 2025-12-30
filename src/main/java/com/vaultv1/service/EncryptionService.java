package com.vaultv1.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
/**
 * Service for Encrypting and Decrypting snippet content.
 * Uses AES encryption.
 * <p>
 * WARNING: Uses a hardcoded key for demonstration/academic purposes.
 * In production, use a Key Management System (KMS) or Environment Variables.
 */
public class EncryptionService {

    // Hardcoded key for demo purposes (Should be in env vars in production)
    private static final String SECRET_KEY = "ThisIsASecretKeyThisIsASecretKey"; // 32 chars for AES-256

    /**
     * Encrypts the raw data string using AES.
     *
     * @param data The plaintext data
     * @return Base64 encoded encrypted string
     */
    public String encrypt(String data) {
        try {
            // Prepare Key
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            // Initialize Cipher in Encrypt Mode
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // Perform Encryption and Encode to Base64 for safe storage
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.toString());
        }
    }

    /**
     * Decrypts the encrypted string.
     *
     * @param encryptedData Base64 encoded encrypted string
     * @return The original plaintext string
     */
    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            // Initialize Cipher in Decrypt Mode
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // Decode Base64 and Decrypt
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.toString());
        }
    }
}
