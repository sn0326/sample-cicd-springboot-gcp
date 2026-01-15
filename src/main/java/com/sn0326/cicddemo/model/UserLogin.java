package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ユーザーログインを保持するエンティティ
 */
@Entity
@Table(name = "user_logins")
public class UserLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "logged_in_at", nullable = false)
    private LocalDateTime loggedInAt;

    @Column(name = "login_method", nullable = false, length = 20)
    private String loginMethod;

    @Column(name = "oidc_provider", length = 20)
    private String oidcProvider;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private boolean success = true;

    @PrePersist
    protected void onCreate() {
        if (loggedInAt == null) {
            loggedInAt = LocalDateTime.now();
        }
    }

    // Constructors
    public UserLogin() {
    }

    public UserLogin(String username, String loginMethod, String oidcProvider, String ipAddress, String userAgent) {
        this.username = username;
        this.loginMethod = loginMethod;
        this.oidcProvider = oidcProvider;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getLoggedInAt() {
        return loggedInAt;
    }

    public void setLoggedInAt(LocalDateTime loggedInAt) {
        this.loggedInAt = loggedInAt;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getOidcProvider() {
        return oidcProvider;
    }

    public void setOidcProvider(String oidcProvider) {
        this.oidcProvider = oidcProvider;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
