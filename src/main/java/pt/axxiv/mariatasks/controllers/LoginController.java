package pt.axxiv.mariatasks.controllers;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import pt.axxiv.mariatasks.auth.AuthService;
import pt.axxiv.mariatasks.auth.AuthUtil;
import pt.axxiv.mariatasks.connection.dao.UserDAO;
import pt.axxiv.mariatasks.data.User;

public class LoginController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;
	private final AuthService authService = new AuthService();

	@Wire
	private Textbox tbUsername;
	@Wire
	private Textbox tbPassword;
	@Wire
	private Textbox tbTitle;
	@Wire
	private Textbox tbNewUsername;
	@Wire
	private Textbox tbNewPassword;
	@Wire
	private Textbox tbConfirmPassword;
	@Wire
	private Div registerWindow;

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);

	}

	@Listen("onOK = #tbUsername")
	public void onEnterInUsername(Event event) {
		onCliclbtLogin(null);
	}

	@Listen("onOK = #tbPassword")
	public void onEnterPress(Event event) {
		onCliclbtLogin(null);
	}
	
	@Listen("onClick = #btLogin")
	public void onCliclbtLogin(Event event) {
		String user = tbUsername.getValue();
		String pass = tbPassword.getValue();
		
		User userBd = new UserDAO().findByUsername(user);

		generateToken(userBd, pass);

	}

	@Listen("onClick = #btRegister")
	public void onCliclbtRegister(Event event) {
		String sclass = registerWindow.getSclass();
	    if (sclass.contains("collapsed")) {
	    	openRegisterWindow();
	    } else {
	    	closeRegisterWindow();
	    }
	}
	
	private void openRegisterWindow() {
		registerWindow.setSclass("login-container");
	}
	
	private void closeRegisterWindow() {
		registerWindow.setSclass("login-container collapsed");
	}

	@Listen("onOK = #tbConfirmPassword")
	public void onEnterPresstbConfirmPassword(Event event) {
		onCliclbtCreateNewUser(null);
	}
	
	@Listen("onClick = #btCreateNewUser")
	public void onCliclbtCreateNewUser(Event event) {
		
		String title = tbTitle.getValue();
		String username = tbNewUsername.getValue();
		String pass = tbNewPassword.getValue();
		String confirmPass = tbConfirmPassword.getValue();

		if(title == null || title.isEmpty() || title.isBlank()) {
			Clients.showNotification("Can't be empty.", tbTitle);
			return;
		}

		if(username == null || username.isEmpty() || username.isBlank()) {
			Clients.showNotification("Can't be empty.", tbNewUsername);
			return;
		}

		if(pass == null || pass.isEmpty() || pass.isBlank()) {
			Clients.showNotification("Can't be empty.", tbNewPassword);
			return;
		}

		if(confirmPass == null || confirmPass.isEmpty() || confirmPass.isBlank()) {
			Clients.showNotification("Can't be empty.", tbConfirmPassword);
			return;
		}
		
		if(username.length()<=3) {
			Clients.showNotification("Username is to small.", tbUsername);
			return;
		}
		
		if(pass.length()<8) {
			Clients.showNotification("Password is to small.", tbNewPassword);
			return;
		}
		
		if(!pass.equals(confirmPass)) {
			Clients.showNotification("Passwords don't match.", tbConfirmPassword);
			return;
			
		}
		
		User checkUsername = new UserDAO().findByUsername(username);
		if(checkUsername != null) {
			Clients.showNotification("Username already taken.", tbNewUsername);
			return;
		}
		
		User user = new User(title, username, pass);
		user = new UserDAO().insert(user);
		
		generateToken(user, pass);
		
	}
	
	private void generateToken(User userBd, String pass) {
		String token = authService.authenticate(userBd, pass);
		if (token != null) {
			Sessions.getCurrent().setAttribute(AuthUtil.SESSION_TOKEN_KEY, token);
			Sessions.getCurrent().setAttribute("currentUserId", userBd.getId());
			Sessions.getCurrent().setAttribute("currentTitle", userBd.getTitle());
			Sessions.getCurrent().setMaxInactiveInterval(30 * 60);
			Executions.sendRedirect("/");
		} else {
			Clients.showNotification("Invalid username or password");
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
