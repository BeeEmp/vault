package com.vaultv1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
public class Snippet {

    @Id
    private String id; // UUID

    @Column(columnDefinition = "TEXT")
    private String encryptedContent;
    private String language;
    private LocalDateTime creationDate;

    private LocalDateTime expiryDate;

    private String title;

    private String creatorUsername; // For history tracking

    public Snippet() {
    }

    public Snippet(String id, String title, String encryptedContent, String language, LocalDateTime creationDate,
            LocalDateTime expiryDate, String creatorUsername) {
        this.id = id;
        this.title = title;
        this.encryptedContent = encryptedContent;
        this.language = language;
        this.creationDate = creationDate;
        this.expiryDate = expiryDate;
        this.creatorUsername = creatorUsername;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }
}
