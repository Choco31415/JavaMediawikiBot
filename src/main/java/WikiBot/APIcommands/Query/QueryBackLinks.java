package WikiBot.APIcommands.Query;

import WikiBot.ContentRep.PageLocation;

public class QueryBackLinks extends QueryList {

	/**
	 * Automatically fetches up to 40 backlinks.
	 */
	public QueryBackLinks(PageLocation page) {
		super("Query back links", page.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(page.getTitle());
		keys.add("bllimit");
		values.add("" + 40);
		unescapeText = true;
	}
	
	/*
	 * Use this for continuing large queries.
	 */
	public QueryBackLinks(PageLocation page, int depth) {
		super("Query back links", page.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(page.getTitle());
		keys.add("bllimit");
		values.add("" + depth);
		unescapeText = true;
	}
	
	public QueryBackLinks(PageLocation page, int depth, String blcontinue) {
		super("Query back links", page.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(page.getTitle());
		keys.add("bllimit");
		values.add("" + depth);
		keys.add("blcontinue");
		values.add(blcontinue);
		unescapeText = true;
		unescapeHTML = false;
	}
}
