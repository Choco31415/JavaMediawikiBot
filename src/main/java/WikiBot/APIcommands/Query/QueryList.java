package WikiBot.APIcommands.Query;

import WikiBot.APIcommands.APIcommand;

public class QueryList extends APIcommand {
	public QueryList(String editSummary_, String language, String list) {
		super(editSummary_, language);
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("list");
		values.add(list);
		unescapeText = true;
		unescapeHTML = false;
	}
}
