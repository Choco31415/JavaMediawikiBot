package WikiBot.APIcommands.Query;

import java.util.ArrayList;

import WikiBot.APIcommands.APIcommand;
import WikiBot.ContentRep.PageLocation;

public class QueryImageInfo extends APIcommand {
	public QueryImageInfo(PageLocation loc, ArrayList<String> infoTypes) {
		super("Query image info", loc);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("titles");
		values.add(loc.getTitle());
		keys.add("prop");
		values.add("imageinfo");
		keys.add("iiprop");
		String infoWanted = "";
		for (int i = 0; i < infoTypes.size(); i++) {
			if (i != 0) {
				infoWanted += "|";
			}
			infoWanted += infoTypes.get(i);
		}
		values.add(infoWanted);
		unescapeText = true;
		unescapeHTML = true;
	}
	
	public QueryImageInfo(PageLocation loc) {
		super("Query image info", loc);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("titles");
		values.add(loc.getTitle());
		keys.add("prop");
		values.add("imageinfo");
		keys.add("iiprop");
		values.add("url|size|dimensions");//default
		unescapeText = true;
		unescapeHTML = true;
	}
}
