package pt.axxiv.mariataskswebapp.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.data.User;

public final class AuthUtil {
	public static final String SESSION_TOKEN_KEY = "AUTH_TOKEN";

	private AuthUtil() {
	}

	public static String getToken() {
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		Cookie[] cookies = request.getCookies();

			if (cookies != null) {
			    for (Cookie c : cookies) {
			        if ("rememberMe".equals(c.getName())) {
			            String token = c.getValue();

			            User user = new UserDAO().findByRememberToken(token);
			            if (user != null) {
			    			Sessions.getCurrent().setAttribute(AuthUtil.SESSION_TOKEN_KEY, token);
			    			Sessions.getCurrent().setAttribute("currentUserId", user.getId());
			    			Sessions.getCurrent().setAttribute("currentTitle", user.getTitle());
			    			Sessions.getCurrent().setMaxInactiveInterval(60 * 60 * 24 * 30);
			            }
			        }
			    }
			}
		
		Object t = Sessions.getCurrent().getAttribute(SESSION_TOKEN_KEY);
		return (t != null) ? t.toString() : null;
	}

	public static boolean isLoggedIn() {
		return getToken() != null;
	}

	public static void logout() {
		
		Cookie cookie = new Cookie("rememberMe", "");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		HttpServletResponse response = (HttpServletResponse) Executions.getCurrent().getNativeResponse();
		response.addCookie(cookie);
		
		Sessions.getCurrent().removeAttribute(SESSION_TOKEN_KEY);
		Sessions.getCurrent().removeAttribute("currentUserId");
		Sessions.getCurrent().removeAttribute("currentTitle");
		Sessions.getCurrent().invalidate();
	}
}