package WikiBot.APIcommands.Query;

/**
 * This command gets the members of a category.
 * 
 * Rights required:
 * none
 * 
 * MW version required:
 * 1.11+
 */
public class QueryCategoryMembers extends QueryList {
	public QueryCategoryMembers(String language, String categoryName, int depth) {
		super("Query category", language, "categorymembers");
		keys.add("cmtitle");
		values.add(categoryName);
		keys.add("cmlimit");
		values.add("" + depth);
		unescapeText = true;
		unescapeHTML = false;
		
		enforceMWVersion("1.11");
	}
	
	/*
	 * Use this for continuing large queries.
	 */
	public QueryCategoryMembers(String language, String categoryName, int depth, String cmcontinue) {
		super("Query category", language, "categorymembers");
		keys.add("cmtitle");
		values.add(categoryName);
		keys.add("cmlimit");
		values.add("" + depth);
		keys.add("cmcontinue");
		values.add(cmcontinue);
		unescapeText = true;
		unescapeHTML = false;
		
		enforceMWVersion("1.11");
	}
	
	/**
	 * This method only fetches at maximum 40 category members.
	 */
	public QueryCategoryMembers(String language, String categoryName) {
		super("Query category", language, "categorymembers");
		keys.add("cmtitle");
		values.add(categoryName);
		keys.add("cmlimit");
		values.add("" + 40);
		unescapeText = true;
		unescapeHTML = false;
		
		enforceMWVersion("1.11");
	}
}
