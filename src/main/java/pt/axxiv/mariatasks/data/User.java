package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

public class User implements Comparable<User> {

	private ObjectId id;
	private String title;
	private String username;
	private String password;
	
	public User(){}

	public User(String title, String username, String password) {
		super();
		this.title = title;
		this.username = username;
		this.password = password;
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

	@Override
	public int compareTo(User arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
