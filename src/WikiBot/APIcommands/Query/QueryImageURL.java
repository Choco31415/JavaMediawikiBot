package WikiBot.APIcommands.Query;

import WikiBot.APIcommands.APIcommand;
import WikiBot.PageRep.PageLocation;

public class QueryImageURL extends APIcommand {
	public QueryImageURL(PageLocation loc) {
		//			input = getURL(baseURL + "/api.php?format=json&action=query&titles=" +  + "&prop=imageinfo&iiprop=url"
		super(loc);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("titles");
		values.add(loc.getTitle());
		keys.add("prop");
		values.add("imageinfo");
		keys.add("iiprop");
		values.add("url");
		unescapeText = true;
		unescapeHTML = false;
	}
}
