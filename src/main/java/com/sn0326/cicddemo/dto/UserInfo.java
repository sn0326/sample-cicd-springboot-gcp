package com.sn0326.cicddemo.dto;

import java.util.List;

public class UserInfo {
    private String username;
    private boolean enabled;
    private List<String> authorities;

    public UserInfo() {
    }

    public UserInfo(String username, boolean enabled, List<String> authorities) {
        this.username = username;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
}
