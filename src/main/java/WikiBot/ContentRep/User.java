package WikiBot.ContentRep;

public class User {

	private String language;
	private String username;
	
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
	
	@Override
	public String toString() {
		return "User: " + username + " (wiki: " + language + ")";
	}
}
