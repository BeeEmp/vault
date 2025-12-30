package com.vaultv1.service;

import com.vaultv1.model.Snippet;
import com.vaultv1.repository.SnippetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SnippetService {

    // Dependency Injection: Spring automatically injects the Repository and
    // EncryptionService
    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private EncryptionService encryptionService;

    // Logic: Create a new snippet with encryption and expiry
    public Snippet createSnippet(String content, String language, int expiryMinutes, String title,
            String creatorUsername) {
        Snippet snippet = new Snippet();
        snippet.setId(UUID.randomUUID().toString()); // Generate unique ID

        // Security: Encrypt content before saving to DB
        snippet.setEncryptedContent(encryptionService.encrypt(content));

        snippet.setLanguage(language);
        snippet.setTitle(title);
        snippet.setCreatorUsername(creatorUsername);
        snippet.setCreationDate(LocalDateTime.now());

        // Cap at 6 hours (360 minutes) to prevent abuse
        if (expiryMinutes > 360)
            expiryMinutes = 360;
        if (expiryMinutes < 1)
            expiryMinutes = 60; // Default fallback if weird value

        // Calculate expiry date based on current time
        snippet.setExpiryDate(LocalDateTime.now().plusMinutes(expiryMinutes));

        return snippetRepository.save(snippet); // Save to database
    }

    // Logic: Retrieve snippet only if it exists and hasn't expired
    public Optional<Snippet> getSnippet(String id) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(id);
        if (snippetOpt.isPresent()) {
            Snippet snippet = snippetOpt.get();

            // Check if expired
            if (LocalDateTime.now().isAfter(snippet.getExpiryDate())) {
                return Optional.empty(); // Treat as missing if expired
            }
            // Decrypt content for the view so user can read it
            snippet.setEncryptedContent(encryptionService.decrypt(snippet.getEncryptedContent()));
            return Optional.of(snippet);
        }
        return Optional.empty();
    }

    // List usage: Return history for specific user
    public List<Snippet> getSnippetsByUser(String username) {
        return snippetRepository.findByCreatorUsernameOrderByCreationDateDesc(username);
    }

    // Validation: Ensure only the creator can delete their snippet
    public boolean deleteSnippet(String id, String username) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(id);
        if (snippetOpt.isPresent()) {
            Snippet snippet = snippetOpt.get();
            // Ownership check
            if (username != null && username.equals(snippet.getCreatorUsername())) {
                snippetRepository.delete(snippet);
                return true;
            }
        }
        return false;
    }
}
