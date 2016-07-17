package WikiBot.APIcommands.Query;

import WikiBot.ContentRep.PageLocation;

/**
 * @Description
 * This command gets all the pages that link to this page.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * 1.9+
 */
public class QueryBackLinks extends QueryList {

	/**
	 * Automatically fetches up to 40 backlinks.
	 */
	public QueryBackLinks(PageLocation loc) {
		super("Query back links", loc.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(loc.getTitle());
		keys.add("bllimit");
		values.add("" + 40);
		unescapeText = true;
		
		enforceMWVersion("1.9");
	}
	
	public QueryBackLinks(PageLocation loc, int depth) {
		super("Query back links", loc.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(loc.getTitle());
		keys.add("bllimit");
		values.add("" + depth);
		unescapeText = true;
		
		enforceMWVersion("1.9");
	}
	
	/*
	 * Use this for continuing large queries.
	 */
	public QueryBackLinks(PageLocation loc, int depth, String blcontinue) {
		super("Query back links", loc.getLanguage(), "backlinks");
		keys.add("bltitle");
		values.add(loc.getTitle());
		keys.add("bllimit");
		values.add("" + depth);
		keys.add("blcontinue");
		values.add(blcontinue);
		unescapeText = true;
		unescapeHTML = false;
		
		enforceMWVersion("1.9");
	}
}
