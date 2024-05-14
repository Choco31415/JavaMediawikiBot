package Content;

public class User {

	private String language;
	private String username;
	
	/**
	 * 
	 * @param language_ The user's wiki.
	 * @param username_ The user's name.
	 */
	public User(String language_, String username_) {
		language = language_;
		username = username_;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getUserName() {
		return username;
	}
	
	public String simpleToString() {
		return username + " (wiki: " + language + ")";
	}
	
	@Override
	public String toString() {
		return "User: " + username + " (wiki: " + language + ")";
	}
}
