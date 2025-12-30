package com.vaultv1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
/**
 * Service for Encrypting and Decrypting snippet content.
 * Uses AES encryption.
 */
public class EncryptionService {

    @Value("${app.encryption.key}")
    private String secretKey;

    /**
     * Encrypts the raw data string using AES.
     *
     * @param data The plaintext data
     * @return Base64 encoded encrypted string
     */
    public String encrypt(String data) {
        try {
            // Prepare Key
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            // Initialize Cipher in Encrypt Mode
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
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
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            // Initialize Cipher in Decrypt Mode
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            // Decode Base64 and Decrypt
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.toString());
        }
    }
}
