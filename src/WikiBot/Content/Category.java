package WikiBot.Content;

public class Category extends PageObject {

	public String rawCategoryText;
	public String categoryName;
	public String alternateCategoryListing;
	
	public Category(String rawCategoryText_, int openPos_, int closePos_) {
		super("Category", openPos_, closePos_, "[[", "]]");
		rawCategoryText = rawCategoryText_;
		alternateCategoryListing = null;
		
		int index = rawCategoryText.indexOf("|");
		if (index != -1) {
			int index2 = rawCategoryText.indexOf("|", index+1);
			if (index2 != -1) {
				alternateCategoryListing = rawCategoryText.substring(index+1, index2);
			} else {
				alternateCategoryListing = rawCategoryText.substring(index+1);
			}
			categoryName = rawCategoryText.substring(0, index);
		} else {
			categoryName = rawCategoryText_;
		}
	}
	
	/*
	 * Includes Category:
	 */
	public String getRawCategoryText() {
		return rawCategoryText;
	}
	
	/*
	 * Does not include Category:
	 */
	public String getCategoryName() {
		return categoryName;
	}
	
	public String getCategoryNameWithoutNameSpace() {
		return categoryName.substring(9);
	}
	
	public String getAlternateCategoryListing() { return alternateCategoryListing; }
	
	@Override
	public String simpleToString() {
		return toString();
	}
	
	@Override
	public String toString() {

		if (alternateCategoryListing != null) {
			return categoryName + " (listed as: " + alternateCategoryListing + ") (at: " + openPos + ")";
		} else {
			return categoryName + " (at: " + openPos + ")";
		}
	}
}
