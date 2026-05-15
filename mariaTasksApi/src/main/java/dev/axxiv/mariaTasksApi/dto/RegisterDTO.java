package dev.axxiv.mariaTasksApi.dto;

public class RegisterDTO {
    private String title;
    private String username;
    private String password;
    private String confirmPassword;
    
    // Constructors
    public RegisterDTO() {}
    
    public RegisterDTO(String title, String username, String password, String confirmPassword) {
        this.title = title;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
    
    // Getters and Setters
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}