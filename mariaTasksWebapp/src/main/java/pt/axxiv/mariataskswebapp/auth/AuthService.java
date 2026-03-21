package pt.axxiv.mariataskswebapp.auth;

import java.util.UUID;

import pt.axxiv.mariatasks.crypt.CryptUtil;
import pt.axxiv.mariatasks.data.User;

public class AuthService {
	
	public String authenticate(User userBd, String password) {
		if (userBd != null && CryptUtil.checkPassword(password, userBd.getPassword())) {
			return generateTokenForUser(userBd.getUsername());
		}
		return null;
	}

	private String generateTokenForUser(String username) {
		return UUID.randomUUID().toString();
	}
}