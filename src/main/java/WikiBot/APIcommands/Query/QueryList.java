package WikiBot.APIcommands.Query;

import WikiBot.APIcommands.APIcommand;

/**
 * This class is used to format other APIcommands.
 * Unless you plan on making an APIcommand,
 * you can ignore this class.
 */
public class QueryList extends APIcommand {
	public QueryList(String shortSummary_, String language, String list) {
		super(shortSummary_, language);
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("list");
		values.add(list);
		unescapeText = true;
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
		unescapeText = true;
		unescapeHTML = true;
	}
}
