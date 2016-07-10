package WikiBot.ContentRep;

import java.util.ArrayList;

/**
 * This class stores user information, like it's direct url, ect...
 */
public class UserInfo extends InfoContainer {
	
	protected User user;
	
	public UserInfo(User user_, ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		super(propertyNames_, propertyValues_);
		user = user_;
	}
	
	public UserInfo(User user_) {
		super();
		user = user_;
	}
	
	public User getUser() {
		return user;
	}
	
	@Override
	public String toString() {
		String output = "";
		
		output += "User info for: " + user;
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ": " + propertyValues.get(i);
		}
		
		return output;
	}
}
