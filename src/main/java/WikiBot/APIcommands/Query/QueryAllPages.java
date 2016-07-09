package WikiBot.APIcommands.Query;

/*
 * APnamespace - The id associated with a namespace in a wiki.
 */

public class QueryAllPages extends QueryList {
	public QueryAllPages(String language, int depth) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		unescapeText = true;
	}
	
	public QueryAllPages(String language, int depth, String apcontinue) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apcontinue");
		values.add(apcontinue);
		unescapeText = true;
		unescapeHTML = false;
	}

	public QueryAllPages(String language, int depth, int apnamespace) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apnamespace");
		values.add("" + apnamespace);
		unescapeText = true;
		unescapeHTML = false;
	}
	
	public QueryAllPages(String language, int depth, String apcontinue, int apnamespace) {
		super("Query all pages", language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apcontinue");
		values.add(apcontinue);
		keys.add("apnamespace");
		values.add("" + apnamespace);
		unescapeText = true;
		unescapeHTML = false;
	}
}
