package pt.axxiv.mariataskswebapp.auth;

import java.util.UUID;

import pt.axxiv.mariatasks.crypt.CryptUtil;
import pt.axxiv.mariatasks.data.User;

public class AuthService {
	/**
	 * Authenticate username/password. Returns session token (JWT or opaque) when
	 * valid, or null.
	 */
	public String authenticate(User userBd, String password) {
// VERY simple check: accept user 'admin' with password 'secret'
		
		
		if (userBd != null && CryptUtil.checkPassword(password, userBd.getPassword())) {
			return generateTokenForUser(userBd.getUsername());
		}
		return null;
	}

	private String generateTokenForUser(String username) {
// Opaque token example using UUID. Replace with JWT or proper token store.
		return UUID.randomUUID().toString();
	}
}