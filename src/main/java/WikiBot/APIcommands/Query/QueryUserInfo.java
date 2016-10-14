package WikiBot.APIcommands.Query;

import java.util.ArrayList;

import WikiBot.ContentRep.User;

/**
 * @Description
 * This command queries a wiki for user information.
 * 
 * All query-able properties can be found here:
 * https://www.mediawiki.org/wiki/API:Users
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * Minimum:
 * 1.12+
 * 
 * Specifics:
 * Support for certain properties was only introduced in later versions of mediawiki.
 * The support table is:
 * implicitgroups: 1.18+
 * rights: 1.17+
 * registration: 1.13+
 * emailable: 1.14+
 * gender: 1.16+
 */
public class QueryUserInfo extends QueryList {

	public QueryUserInfo(ArrayList<User> users, ArrayList<String> propertiesToGet) {
		super("Query user info", users.get(0).getLanguage(), "users", "json");
		
		//Parse usernames into a MW friendly format.
		String parsedUserNames = "";
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			
			if (i != 0){
				parsedUserNames += "|";
			}
			
			parsedUserNames += user.getUserName();
		}
		
		//Finish initializing the API command
		keys.add("ususers");
		values.add(parsedUserNames);
		keys.add("usprop");
		values.add(compactArray(propertiesToGet, "|"));
		
		enforceMWVersion("1.12");
		
		localEnforce(propertiesToGet);
	}
	
	private void localEnforce(ArrayList<String> propertiesToGet) {
		if (propertiesToGet.contains("implicitgroups")) {
			enforceMWVersion("1.18");
		}
		if (propertiesToGet.contains("rights")) {
			enforceMWVersion("1.17");
		}
		if (propertiesToGet.contains("registration")) {
			enforceMWVersion("1.13");
		}
		if (propertiesToGet.contains("emailable")) {
			enforceMWVersion("1.14");
		}
		if (propertiesToGet.contains("gender")) {
			enforceMWVersion("1.16");
		}
	}
}
