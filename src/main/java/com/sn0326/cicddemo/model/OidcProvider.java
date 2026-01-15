package com.sn0326.cicddemo.model;

/**
 * サポートされているOIDCプロバイダーの列挙型
 */
public enum OidcProvider {
    GOOGLE("google"),
    // 将来的に他のプロバイダーを追加可能
    // GITHUB("github"),
    // AZURE("azure"),
    ;

    private final String value;

    OidcProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OidcProvider fromValue(String value) {
        for (OidcProvider provider : values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown OIDC provider: " + value);
    }
}
