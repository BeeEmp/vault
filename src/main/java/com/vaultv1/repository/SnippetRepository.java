package com.vaultv1.repository;

import com.vaultv1.model.Snippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.time.LocalDateTime;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, String> {
    List<Snippet> findByCreatorUsernameOrderByCreationDateDesc(String creatorUsername);

    List<Snippet> findByExpiryDateBefore(LocalDateTime now);
}
