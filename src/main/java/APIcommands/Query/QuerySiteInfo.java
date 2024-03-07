package APIcommands.Query;

import java.util.ArrayList;

import APIcommands.APIcommand;

/**
 * @Description
 * This command gets site information.
 * 
 * Recommended not used.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * all
 * 
 * Specifics:
 * Support for certain properties was only introduced in later versions of mediawiki.
 * The support table is:
 * statistics: 1.11+
 * ect...
 * 
 * More details at: https://www.mediawiki.org/wiki/API:Siteinfo
 */
public class QuerySiteInfo extends APIcommand {
	
	public QuerySiteInfo(String language, ArrayList<String> propertiesToGet) {
		super("Query site info", language);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("meta");
		values.add("siteinfo");
		keys.add("siprop");
		values.add(compactArray(propertiesToGet, "|"));
		
		localEnforce(propertiesToGet);
	}
	
	private void localEnforce(ArrayList<String> propertiesToGet) {
		//MW requirements and such
		if (propertiesToGet.contains("statistics")) {
			enforceMWVersion("1.11");
		}
		//TODO: Add support for other properties as methods for them are built.
	}
}
