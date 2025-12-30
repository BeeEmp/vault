package com.vaultv1.controller;

import com.vaultv1.model.Snippet;
import com.vaultv1.service.SnippetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/snippets")
/**
 * REST Controller for managing code snippets.
 * Provides endpoints for creating, retrieving, and deleting snippets.
 */
public class SnippetController {

    private static final Logger logger = LoggerFactory.getLogger(SnippetController.class);

    @Autowired
    private SnippetService snippetService;

    /**
     * Create a new snippet.
     *
     * @param payload   Map containing "content", "language", "title", "expiryTime"
     * @param principal Authenticated user (optional)
     * @return ResponseEntity containing the created snippet or error message
     */
    @PostMapping
    public ResponseEntity<?> createSnippet(@RequestBody Map<String, Object> payload,
            java.security.Principal principal) {
        String content = (String) payload.get("content");
        String language = (String) payload.get("language");
        String title = (String) payload.get("title");
        Integer expiryMinutes = (Integer) payload.get("expiryTime");

        // Soft Error Handling: Validation
        if (content == null || content.isEmpty()) {
            logger.warn("Creation failed: Content is required");
            throw new RuntimeException("Content is required"); // Handled by GlobalExceptionHandler
        }

        if (expiryMinutes == null) {
            expiryMinutes = 360; // Default 6h
        }

        String username = (principal != null) ? principal.getName() : null;
        logger.info("Creating snippet for user: {}", (username != null ? username : "Anonymous"));

        Snippet snippet = snippetService.createSnippet(content, language, expiryMinutes, title, username);
        return ResponseEntity.ok(snippet);
    }

    /**
     * Retrieve a public snippet by ID.
     *
     * @param id The snippet ID
     * @return The snippet if found and valid, otherwise 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSnippet(@PathVariable String id) {
        Optional<Snippet> snippet = snippetService.getSnippet(id);
        if (snippet.isPresent()) {
            return ResponseEntity.ok(snippet.get());
        } else {
            logger.info("Snippet lookup failed for ID: {} (Not found or expired)", id);
            return ResponseEntity.status(404).body("Snippet not found or expired");
        }
    }

    /**
     * Get snippet history for the logged-in user.
     *
     * @param principal The authenticated user
     * @return List of user's snippets
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(snippetService.getSnippetsByUser(principal.getName()));
    }

    /**
     * Delete a snippet.
     * Only the creator can delete their snippet.
     *
     * @param id        The snippet ID
     * @param principal The authenticated user
     * @return 200 OK if deleted, 403 if forbidden/not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSnippet(@PathVariable String id, java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        logger.info("Request to delete snippet ID: {} by user: {}", id, principal.getName());

        boolean deleted = snippetService.deleteSnippet(id, principal.getName());

        if (deleted) {
            logger.info("Snippet deleted successfully: {}", id);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Delete failed for ID: {} - Forbidden or Not Found", id);
            return ResponseEntity.status(403).body("Forbidden or Not Found");
        }
    }
}
