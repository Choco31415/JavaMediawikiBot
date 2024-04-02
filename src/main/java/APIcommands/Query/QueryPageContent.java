package APIcommands.Query;

import java.util.ArrayList;

import APIcommands.APIcommand;
import Content.PageLocation;

/**
 * @Description
 * This command gets the contents of a page.
 * 
 * Recommended not used.
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
	}
}
