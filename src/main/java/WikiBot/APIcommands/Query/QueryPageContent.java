package WikiBot.APIcommands.Query;

import java.util.ArrayList;

import WikiBot.APIcommands.APIcommand;
import WikiBot.ContentRep.PageLocation;

/**
 * @Description
 * This command gets the contents of a page.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * all
 */
public class QueryPageContent extends APIcommand {	
	public QueryPageContent(PageLocation loc) {
		super("Query page", loc);
		addParameter("format", "json");
		addParameter("action", "query");
		addParameter("titles", loc.getTitle());
		addParameter("prop", "revisions");//Gets current page revision.
		addParameter("rvprop", "content");//Gets current page revision content.
		unescapeText = true;
	}
	
	/**
	 * Warning: All pages must be from the same wiki!
	 */
	public QueryPageContent(ArrayList<PageLocation> locs) {
		super("Query pages", locs.get(0).getLanguage());
		addParameter("format", "json");
		addParameter("action", "query");
		addParameter("titles", compactPLArray(locs, "|"));
		addParameter("prop", "revisions");
		addParameter("rvprop", "content");
		unescapeText = true;
	}
}
