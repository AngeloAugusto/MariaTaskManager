package pt.axxiv.mariatasks.crypt;

import org.mindrot.jbcrypt.BCrypt;

public final class CryptUtil{

	public CryptUtil() {
		
	}
	
	public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}