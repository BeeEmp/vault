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
/**
 * Service class for managing Snippets.
 * Handles business logic for creation, retrieval, deletion, and expiry of
 * snippets.
 */
public class SnippetService {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private EncryptionService encryptionService;

    /**
     * Create a new snippet with encrypted content.
     *
     * @param content         The raw content to share
     * @param language
     * @param expiryMinutes
     * @param title
     * @param creatorUsername
     * @return The saved snippet entity
     */
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

        // Cap at 6 hours
        if (expiryMinutes > 360)
            expiryMinutes = 360;
        if (expiryMinutes < 1)
            expiryMinutes = 60;

        // Calculate expiry date based on current time
        snippet.setExpiryDate(LocalDateTime.now().plusMinutes(expiryMinutes));

        return snippetRepository.save(snippet);
    }

    /**
     * Retrieve a snippet by ID.
     * Decrypts content if found and not expired.
     * Returns Empty if expired.
     *
     * @param id
     * @return Optional containing the snippet if valid
     */
    public Optional<Snippet> getSnippet(String id) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(id);
        if (snippetOpt.isPresent()) {
            Snippet snippet = snippetOpt.get();

            // Check if expired
            if (LocalDateTime.now().isAfter(snippet.getExpiryDate())) {
                // Ideally, trigger cleanup here or via a scheduled task
                return Optional.empty(); // Treat as missing if expired
            }
            // Decrypt content for the view so user can read it
            snippet.setEncryptedContent(encryptionService.decrypt(snippet.getEncryptedContent()));
            return Optional.of(snippet);
        }
        return Optional.empty();
    }

    /**
     * Get all snippets created by a specific user.
     *
     * @param username The username
     * @return List of snippets
     */
    public List<Snippet> getSnippetsByUser(String username) {
        return snippetRepository.findByCreatorUsernameOrderByCreationDateDesc(username);
    }

    /**
     * Delete a snippet if the user owns it.
     *
     * @param id
     * @param username
     * @return true if deleted, false if not found or unauthorized
     */
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
