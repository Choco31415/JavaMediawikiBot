package WikiBot.APIcommands.Query;

import WikiBot.ContentRep.PageLocation;

public class QueryImageURL extends QueryImageInfo {
	public QueryImageURL(PageLocation loc) {
		super(loc, "url");
	}
}
