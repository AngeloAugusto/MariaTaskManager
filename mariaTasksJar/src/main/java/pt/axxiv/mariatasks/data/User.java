package pt.axxiv.mariatasks.data;

import java.util.Date;

import org.bson.types.ObjectId;

import pt.axxiv.mariatasks.crypt.CryptUtil;

public class User implements Comparable<User> {

	private ObjectId id;
	private String title;
	private String username;
	private String password;
	private String rememberToken;
	private String bearerToken;
	private Date bearerTokenExpiry;
	
	public User(){}

	public User(String title, String username, String password) {
		super();
		this.title = title;
		this.username = username;
		this.password = CryptUtil.hashPassword(password);
		this.bearerToken=null;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRememberToken() {
		return rememberToken;
	}

	public void setRememberToken(String rememberToken) {
		this.rememberToken = rememberToken;
	}

	public String getBearerToken() {
		return bearerToken;
	}

	public void setBearerToken(String bearerToken) {
		this.bearerToken = bearerToken;
	}
	
    public Date getBearerTokenExpiry() {
        return bearerTokenExpiry;
    }

    public void setBearerTokenExpiry(Date bearerTokenExpiry) {
        this.bearerTokenExpiry = bearerTokenExpiry;
    }
    
    // Helper method to check if token is expired
    public boolean isBearerTokenExpired() {
        if (bearerTokenExpiry == null) {
            return true;
        }
        return new Date().after(bearerTokenExpiry);
    }

	@Override
	public int compareTo(User arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
