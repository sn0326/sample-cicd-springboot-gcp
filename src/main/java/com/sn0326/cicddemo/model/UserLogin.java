package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ユーザーログインを保持するエンティティ
 */
@Entity
@Table(name = "user_logins")
@Getter
@Setter
@NoArgsConstructor
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

    public UserLogin(String username, String loginMethod, String oidcProvider, String ipAddress, String userAgent) {
        this.username = username;
        this.loginMethod = loginMethod;
        this.oidcProvider = oidcProvider;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = true;
    }
}
