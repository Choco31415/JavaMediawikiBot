package WikiBot.APIcommands.Query;

import java.util.ArrayList;

/**
 * This command queries a wiki for user information.
 * 
 * All query-able properties can be found here:
 * https://www.mediawiki.org/wiki/API:Users
 * 
 * Rights required:
 * none
 * 
 * MW version required:
 * 1.12+
 */
public class QueryUserInfo extends QueryList {

	public QueryUserInfo(String language, ArrayList<String> userNames, ArrayList<String> propertiesToGet) {
		super("Query user info", language, "users", "json");
		keys.add("ususers");
		values.add(compactArray(userNames, "|"));
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
