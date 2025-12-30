package com.vaultv1.service;

import com.vaultv1.model.Snippet;
import com.vaultv1.repository.SnippetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    @Autowired
    private SnippetRepository snippetRepository;

    // Run every hour (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredSnippets() {
        LocalDateTime now = LocalDateTime.now();
        List<Snippet> expiredSnippets = snippetRepository.findByExpiryDateBefore(now);
        if (!expiredSnippets.isEmpty()) {
            snippetRepository.deleteAll(expiredSnippets);
            System.out.println("Cleaned up " + expiredSnippets.size() + " expired snippets.");
        }
    }
}
