package WikiBot.APIcommands.Query;

/**
 * This requires MW version 1.23 and above.
 */
public class QueryPrefix extends QueryList {
	public QueryPrefix(String language, String prefix) {
		super(language, "prefixsearch");
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("pssearch");
		values.add(prefix);
		unescapeText = true;
		unescapeHTML = false;
	}
	
	public QueryPrefix(String language, String prefix, int psoffset) {
		super(language, "prefixsearch");
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
	}
}
