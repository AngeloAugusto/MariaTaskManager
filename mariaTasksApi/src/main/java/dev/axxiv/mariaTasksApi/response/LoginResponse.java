package dev.axxiv.mariaTasksApi.response;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LoginResponse {
    private String id;
    private String title;
    private String username;
    private String bearerToken;
    private Date tokenExpiry;  // Add this
    private long expiresInDays;  // Optional: add days until expiration
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getBearerToken() {
        return bearerToken;
    }
    
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
    
    public Date getTokenExpiry() {
        return tokenExpiry;
    }
    
    public void setTokenExpiry(Date tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
        // Calculate days remaining
        this.expiresInDays = TimeUnit.MILLISECONDS.toDays(
            tokenExpiry.getTime() - System.currentTimeMillis()
        );
    }
    
    public long getExpiresInDays() {
        return expiresInDays;
    }
}