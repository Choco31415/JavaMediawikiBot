package WikiBot.APIcommands.Query;

/**
 * @Description
 * This command gets the members of a category.
 * 
 * Recommended not used raw.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * 1.11+
 */
public class QueryCategoryMembers extends QueryList {
	public QueryCategoryMembers(String language, String categoryName, int depth) {
		super("Query category", language, "categorymembers");
		keys.add("cmtitle");
		values.add(categoryName);
		keys.add("cmlimit");
		values.add("" + depth);
		
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
		
		enforceMWVersion("1.11");
	}
}
