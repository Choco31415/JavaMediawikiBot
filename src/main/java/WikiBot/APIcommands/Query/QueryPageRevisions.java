package WikiBot.APIcommands.Query;

import WikiBot.APIcommands.APIcommand;
import WikiBot.ContentRep.PageLocation;

public class QueryPageRevisions extends APIcommand {
	/*
	 * See this page for revision API info: https://www.mediawiki.org/wiki/API:Revisions
	 */
	public QueryPageRevisions(PageLocation loc, int pageID, int revisionLimit, boolean getContent) {
		super("Query revisions", loc);
		keys.add("format");
		values.add("xml");
		keys.add("action");
		values.add("query");
		keys.add("prop");
		values.add("revisions");
		keys.add("pageids");
		values.add("" + pageID);
		keys.add("rvprop");
		if (getContent) {
			values.add("user|comment|timestamp|content|flags");
		} else {
			values.add("user|comment|timestamp|flags");	
		}
		keys.add("rvlimit");
		values.add("" + revisionLimit);
		unescapeText = true;
		unescapeHTML = true;
	}
	
	public QueryPageRevisions(PageLocation loc, int revisionLimit, boolean getContent) {
		super("Query revisions", loc);
		keys.add("format");
		values.add("xml");
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
		unescapeText = true;
		unescapeHTML = true;
	}
}
