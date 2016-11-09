package WikiBot.APIcommands.Query;

/**
 * @Description
 * This command gets pages with a certain prefix.
 * 
 * Recommended not used raw.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
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
		
		enforceMWVersion("1.23");
	}
	
	public QueryPrefix(String language, String prefix, int psoffset, int psnamespace) {
		super("Query prefix", language, "prefixsearch");
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("pssearch");
		values.add(prefix);
		keys.add("psnamespace");
		values.add("" + psnamespace);//Namespace
		keys.add("psoffset");
		values.add("" + psoffset);//Get psoffset items and up.
		
		enforceMWVersion("1.23");
	}
}
