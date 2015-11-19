package WikiBot.APIcommands.Query;

import WikiBot.APIcommands.APIcommand;

public class QueryList extends APIcommand {
	public QueryList(String language, String list) {
		super(language);
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
