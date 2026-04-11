package dev.axxiv.mariaTasksApi;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.axxiv.mariaTasksApi.dto.LoginDTO;
import dev.axxiv.mariaTasksApi.response.LoginResponse;
import pt.axxiv.mariatasks.connection.dao.TaskDAO;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.crypt.CryptUtil;
import pt.axxiv.mariatasks.data.Task;
import pt.axxiv.mariatasks.data.User;

@RestController
@RequestMapping("/users")
public class UserController {
	
	private UserDAO userDao;
	
	public UserController() {
        this.userDao = new UserDAO();
    }
	
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest) {
        try {
            // Find user by username
            User user = userDao.findByUsername(loginRequest.getUsername());
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
            
            // Verify password
            if (!CryptUtil.checkPassword(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
            
            // Generate remember token (for "remember me" functionality)
            String bearerToken = UUID.randomUUID().toString();
            Date expiryDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
            user.setBearerToken(bearerToken);
            user.setBearerTokenExpiry(expiryDate);
            userDao.updateBearerToken(user.getId(), bearerToken, expiryDate);
            
            // Create response
            LoginResponse response = new LoginResponse();
            response.setId(user.getId().toString());
            response.setTitle(user.getTitle());
            response.setUsername(user.getUsername());
            response.setBearerToken(user.getBearerToken());
            response.setTokenExpiry(user.getBearerTokenExpiry());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during login: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // Check if token was provided
            if (token == null || token.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Authorization token is required");
            }
            
            // Remove "Bearer " prefix if present
            String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            if (bearerToken.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid token format");
            }
            
            // Find user by remember token
            User user = userDao.findByBearerToken(bearerToken);
            
            if (user == null) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. User not found or already logged out.");
            }
            
            // Clear the remember token
            userDao.clearBearerToken(user.getId());
            
            return ResponseEntity.ok("Logged out successfully");
            
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error during logout: " + e.getMessage());
        }
    }

}
