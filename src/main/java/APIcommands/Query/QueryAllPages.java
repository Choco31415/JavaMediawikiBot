package WikiBot.APIcommands.Query;

/**
 * @Description
 * This command gets a list of all pages on the wiki.
 * 
 * Recommended not used raw.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * all
 */
public class QueryAllPages extends QueryList {
	/**
	 * This constructor assumes the main namespace.
	 * @param language The wiki language.
	 * @param depth How many pages to get.
	 */
	public QueryAllPages(String language, int depth) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
	}
	
	/*
	 * Use for continuing large queries.
	 */
	public QueryAllPages(String language, int depth, String apcontinue) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apcontinue");
		values.add(apcontinue);
	}

	/**
	 * 
	 * @param language The wiki language.
	 * @param depth How many pages to get.
	 * @param apcontinue The namespace id to query.
	 */
	public QueryAllPages(String language, int depth, int apnamespace) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apnamespace");
		values.add("" + apnamespace);
	}
	
	/*
	 * Use for continuing large queries.
	 */
	public QueryAllPages(String language, int depth, String apcontinue, int apnamespace) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apcontinue");
		values.add(apcontinue);
		keys.add("apnamespace");
		values.add("" + apnamespace);
	}
}
