package WikiBot.APIcommands.Query;

import WikiBot.MediawikiData.VersionNumber;

/**
 * This requires MW version 1.23 and above.
 */
public class QueryPrefix extends QueryList {
	public QueryPrefix(String language, String prefix) {
		super("Query prefix", language, "prefixsearch");
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("pssearch");
		values.add(prefix);
		unescapeText = true;
		unescapeHTML = false;
		
		enforceMWVersion(new VersionNumber("1.22"));
	}
	
	public QueryPrefix(String language, String prefix, int psoffset) {
		super("Query prefix", language, "prefixsearch");
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("pssearch");
		values.add(prefix);
		keys.add("psoffset");
		values.add("" + psoffset);//Get psoffset items and up.
		unescapeText = true;
		unescapeHTML = false;
		
		enforceMWVersion(new VersionNumber("1.22"));
	}
}
