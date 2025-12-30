package com.vaultv1.controller;

import com.vaultv1.model.Snippet;
import com.vaultv1.service.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {

    @Autowired
    private SnippetService snippetService;

    // Endpoint: Create new snippet (POST)
    // Input: JSON payload with content, language, title, expiry
    @PostMapping
    public ResponseEntity<?> createSnippet(@RequestBody Map<String, Object> payload,
            java.security.Principal principal) {
        String content = (String) payload.get("content");
        String language = (String) payload.get("language");
        String title = (String) payload.get("title");
        Integer expiryMinutes = (Integer) payload.get("expiryTime");

        // Error Handling: Basic Validation
        if (content == null || content.isEmpty()) {
            return ResponseEntity.badRequest().body("Content is required");
        }

        if (expiryMinutes == null) {
            expiryMinutes = 360; // Default 6h
        }

        String username = (principal != null) ? principal.getName() : null;

        Snippet snippet = snippetService.createSnippet(content, language, expiryMinutes, title, username);
        return ResponseEntity.ok(snippet);
    }

    // Endpoint: Get Public Snippet (GET)
    // Access: Public (Unauthenticated allowed)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSnippet(@PathVariable String id) {
        Optional<Snippet> snippet = snippetService.getSnippet(id);
        if (snippet.isPresent()) {
            return ResponseEntity.ok(snippet.get());
        } else {
            return ResponseEntity.status(404).body("Snippet not found or expired");
        }
    }

    // Endpoint: User History (GET)
    // Access: Secured (Requires Login)
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(snippetService.getSnippetsByUser(principal.getName()));
    }

    // Endpoint: Delete Snippet (DELETE)
    // Logic: Checks ownership before deleting
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSnippet(@PathVariable String id, java.security.Principal principal) {
        System.out.println("Received DELETE request for ID: " + id + " from user: "
                + (principal != null ? principal.getName() : "null"));

        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        boolean deleted = snippetService.deleteSnippet(id, principal.getName());
        System.out.println("Delete result for ID " + id + ": " + deleted);

        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body("Forbidden or Not Found");
        }
    }
}
