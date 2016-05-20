package WikiBot.APIcommands.Query;

public class QueryRecentChanges extends QueryList {
	public QueryRecentChanges(String language, int revisionLimit) {
		super(language, "recentchanges");
		keys.add("rcprop");
		values.add("timestamp|title|user|comment");
		keys.add("rclimit");
		values.add("" + revisionLimit);
		unescapeText = true;
		unescapeHTML = true;
	}
	
	/*
	 * Use this for continuing large queries.
	 */
	public QueryRecentChanges(String language, int revisionLimit, String rccontinue) {
		super(language, "recentchanges");
		keys.add("rcprop");
		values.add("timestamp|title|user|comment");
		keys.add("rclimit");
		values.add("" + revisionLimit);
		keys.add("rccontinue");
		values.add(rccontinue);
		unescapeText = true;
		unescapeHTML = true;
	}
}
