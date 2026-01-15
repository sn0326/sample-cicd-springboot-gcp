package com.sn0326.cicddemo.dto;

import java.time.LocalDateTime;

/**
 * OIDC連携情報のレスポンスDTO
 */
public class OidcConnectionInfo {
    private Long id;
    private String provider;
    private String email;
    private boolean enabled;
    private LocalDateTime createdAt;

    public OidcConnectionInfo() {
    }

    public OidcConnectionInfo(Long id, String provider, String email, boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.provider = provider;
        this.email = email;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
