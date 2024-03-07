package APIcommands.Query;

import APIcommands.APIcommand;

/**
 * @Description
 * This class is used to format other APIcommands.
 * Unless you plan on making an APIcommand,
 * you can ignore this class.
 * 
 * Recommended not used.
 */
public class QueryList extends APIcommand {
	public QueryList(String shortSummary_, String language, String list) {
		super(shortSummary_, language);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("list");
		values.add(list);
		unescapeHTML = true;
	}
	
	public QueryList(String shortSummary_, String language, String list, String format) {
		super(shortSummary_, language);
		keys.add("format");
		values.add(format);
		keys.add("action");
		values.add("query");
		keys.add("list");
		values.add(list);
		unescapeHTML = true;
	}
}
