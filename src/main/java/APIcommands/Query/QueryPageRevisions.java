package APIcommands.Query;

import APIcommands.APIcommand;
import ContentRep.PageLocation;

/**
 * @Description
 * This command gets the revisions for a page.
 * 
 * See this page for revision API info:
 * https://www.mediawiki.org/wiki/API:Revisions
 * 
 * Recommended not used.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * all
 */
public class QueryPageRevisions extends APIcommand {
	public QueryPageRevisions(PageLocation loc, int revisionLimit, boolean getContent) {
		super("Query revisions", loc);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("prop");
		values.add("revisions");
		keys.add("titles");
		values.add(loc.getTitle());
		keys.add("rvprop");
		if (getContent) {
			values.add("user|comment|timestamp|content|flags");
		} else {
			values.add("user|comment|timestamp|flags");	
		}
		keys.add("rvlimit");
		values.add("" + revisionLimit);
		unescapeHTML = true;
	}
}
