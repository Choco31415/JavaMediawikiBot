package WikiBot.APIcommands.Query;

import WikiBot.Content.PageLocation;

public class QueryBackLinks extends QueryList {

	/**
	 * Automatically fetches up to 40 backlinks.
	 */
	public QueryBackLinks(PageLocation page) {
		super(page.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(page.getTitle());
		keys.add("bllimit");
		values.add("" + 40);
	}
	
	/*
	 * Use this for continuing large queries.
	 */
	public QueryBackLinks(PageLocation page, int depth) {
		super(page.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(page.getTitle());
		keys.add("bllimit");
		values.add("" + depth);
	}
	
	public QueryBackLinks(PageLocation page, int depth, String blcontinue) {
		super(page.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(page.getTitle());
		keys.add("bllimit");
		values.add("" + depth);
		keys.add("blcontinue");
		values.add(blcontinue);
	}
}
