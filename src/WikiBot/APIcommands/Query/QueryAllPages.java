package WikiBot.APIcommands.Query;

public class QueryAllPages extends QueryList {
	public QueryAllPages(String language, int depth) {
		super(language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
	}
	
	public QueryAllPages(String language, int depth, String apcontinue) {
		super(language, "allpages");
		keys.add("aplimit");
		values.add("" + depth);
		keys.add("apcontinue");
		values.add(apcontinue);
	}
}
