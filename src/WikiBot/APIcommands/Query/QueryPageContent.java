package WikiBot.APIcommands.Query;

import java.util.ArrayList;

import WikiBot.APIcommands.APIcommand;
import WikiBot.Content.PageLocation;

public class QueryPageContent extends APIcommand {	
	public QueryPageContent(PageLocation loc) {
		super(loc);
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
		super(locs.get(0).getLanguage());
		addParameter("format", "json");
		addParameter("action", "query");
		addParameter("titles", compactArray(locs, "|"));
		addParameter("prop", "revisions");
		addParameter("rvprop", "content");
	}
	
	static public String compactArray(ArrayList<PageLocation> array, String delimitor) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (int i = 0; i < array.size(); i++) {
			output+= array.get(i).getTitle();
			if (i != array.size()-1) {
				output += delimitor;
			}
		}
		
		return output;
	}
}
