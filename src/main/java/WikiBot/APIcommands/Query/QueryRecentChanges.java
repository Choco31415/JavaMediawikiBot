package WikiBot.APIcommands.Query;

public class QueryRecentChanges extends QueryList {
	public QueryRecentChanges(String language, int revisionLimit) {
		super("Query rc", language, "recentchanges");
		keys.add("rcprop");
		values.add("timestamp|title|user|comment|flags");
		keys.add("rclimit");
		values.add("" + revisionLimit);
		unescapeText = true;
		unescapeHTML = true;
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
		unescapeText = true;
		unescapeHTML = true;
	}
}
