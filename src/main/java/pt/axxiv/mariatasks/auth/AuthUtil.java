package pt.axxiv.mariatasks.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.zkoss.zk.ui.Sessions;

public final class AuthUtil {
	public static final String SESSION_TOKEN_KEY = "AUTH_TOKEN";

	private AuthUtil() {
	}

	public static String getToken() {
		Object t = Sessions.getCurrent().getAttribute(SESSION_TOKEN_KEY);
		return (t != null) ? t.toString() : null;
	}

	public static boolean isLoggedIn() {
		return getToken() != null;
	}

	public static void logout() {
		Sessions.getCurrent().removeAttribute(SESSION_TOKEN_KEY);
		Sessions.getCurrent().removeAttribute("currentUserId");
		Sessions.getCurrent().removeAttribute("currentTitle");
		Sessions.getCurrent().invalidate();
	}
	
	public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}