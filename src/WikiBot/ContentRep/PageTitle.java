package WikiBot.ContentRep;

/**
 * This class takes in an article title and parses it into: namespace, Page Name (without namespace)
 */
public class PageTitle {

	private String title;
	private String nameSpace;
	private String titleWithoutNameSpace;
	
	/**
	 * @param title_ The title of the page.
	 */
	public PageTitle(String title_) {
		title = title_;
		
		if (title.contains(":") && (title.indexOf(":") < title.indexOf("/") || !title.contains("/"))) {
			nameSpace = title.substring(0, title.indexOf(":"));
			titleWithoutNameSpace = title.substring(title.indexOf(":") + 1);
		} else {
			nameSpace = "main";
			titleWithoutNameSpace = title_;
		}
	}
	
	public static String getNameSpace(String pageName) {
		if (pageName.contains(":") && (pageName.indexOf(":") < pageName.indexOf("/") || !pageName.contains("/"))) {
			return pageName.substring(0, pageName.indexOf(":"));
		} else {
			return "main";
		}
	}
	
	public String getTitle() { return title; }
	public String getNameSpace() { return nameSpace; }
	public String getTitleWithoutNameSpace() { return titleWithoutNameSpace; }
	
	@Override
	public String toString() {
		return "\nPageTitle: " + title;
	}
}
