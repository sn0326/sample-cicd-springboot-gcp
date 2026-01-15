package com.sn0326.cicddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OIDC連携情報のレスポンスDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OidcConnectionInfo {
    private Long id;
    private String provider;
    private String email;
    private boolean enabled;
    private LocalDateTime createdAt;
}
