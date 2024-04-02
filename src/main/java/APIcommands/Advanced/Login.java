package APIcommands.Advanced;

import APIcommands.APIcommand;
import Content.User;

/**
 * @Description
 * This command logins to a wiki.
 * 
 * Recommended not used.
 * 
 * @MediawikiSupport
 * all
 */
public class Login extends APIcommand {
	public Login(User user, String password) {
		super("login", user.getLanguage(), true, "login", "login");
		
		keys.add("action");
		values.add("login");
		keys.add("format");
		values.add("json");
		keys.add("lgname");
		values.add(user.getUserName());
		keys.add("lgpassword");
		values.add(password);
	}
}
