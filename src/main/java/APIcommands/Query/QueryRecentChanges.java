package APIcommands.Query;

/**
 * @Description
 * This command gets a list of recent changes.
 * 
 * Recommended not used.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * 1.9+
 */
public class QueryRecentChanges extends QueryList {
	public QueryRecentChanges(String language, int revisionLimit) {
		super("Query rc", language, "recentchanges");
		keys.add("rcprop");
		values.add("timestamp|title|user|comment|flags");
		keys.add("rclimit");
		values.add("" + revisionLimit);
		unescapeHTML = true;
		
		enforceMWVersion("1.9");
	}
	
	/*
	 * Use this for continuing large queries.
	 */
	public QueryRecentChanges(String language, int revisionLimit, String rccontinue) {
		super("Query rc", language, "recentchanges");
		keys.add("rcprop");
		values.add("timestamp|title|user|comment|flags");
		keys.add("rclimit");
		values.add("" + revisionLimit);
		keys.add("rccontinue");
		values.add(rccontinue);
		unescapeHTML = true;
		
		enforceMWVersion("1.9");
	}
}
