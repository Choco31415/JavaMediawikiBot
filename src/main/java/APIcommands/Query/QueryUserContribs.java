package APIcommands.Query;

import java.util.ArrayList;

import ContentRep.User;

/**
 * @Description
 * This command gets a list of a user's contributions.
 * 
 * Recommended not used.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * Minimum:
 * 1.9+
 * 
 * Specifics:
 * Support for certain properties was only introduced in later versions of mediawiki.
 * The support table is:
 * parsedcomment: 1.16+
 * size: 1.16+
 * sizediff: 1.20+
 * tags: 1.16+
 */
public class QueryUserContribs extends QueryList {
	public QueryUserContribs(User user, ArrayList<String> propertiesToGet, int uclimit) {
		super("Query user contribs", user.getLanguage(), "usercontribs", "json");
		
		keys.add("ucuser");
		values.add(user.getUserName());
		keys.add("ucprop");
		values.add(compactArray(propertiesToGet, "|"));
		keys.add("uclimit");
		values.add("" + uclimit);
		unescapeHTML = true;
		
		enforceMWVersion("1.9");
		
		localEnforce(propertiesToGet);
	}
	
	public void localEnforce(ArrayList<String> propertiesToGet) {
		if (propertiesToGet.contains("parsedcomment")) {
			enforceMWVersion("1.16");
		}
		if (propertiesToGet.contains("size")) {
			enforceMWVersion("1.16");
		}
		if (propertiesToGet.contains("sizediff")) {
			enforceMWVersion("1.20");
		}
		if (propertiesToGet.contains("tags")) {
			enforceMWVersion("1.16");
		}
	}
}
