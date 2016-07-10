package WikiBot.APIcommands.Query;

/**
 * This command gets pages with a certain prefix.
 * 
 * Rights required:
 * none
 * 
 * MW version required:
 * 1.23+
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
		
		enforceMWVersion("1.23");
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
		
		enforceMWVersion("1.23");
	}
}
