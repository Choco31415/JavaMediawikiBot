package WikiBot.ContentRep;

import WikiBot.Core.GenericBot;

public class User {

	private String language;
	private String username;
	
	/**
	 * 
	 * @param username_ The user's name.
	 * @param language_ The user's wiki.
	 */
	public User(String username_, String language_) {
		language = language_;
		username = username_;
	}
	
	/**
	 * The language is assumed to be the home language of the bot.
	 * @param username_ The user's name.
	 */
	public User(String username_) {
		username = username_;
		language = GenericBot.getInstance().getHomeWikiLanguage();
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
