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
	
	public String getTitle() { return title; }
	public String getNameSpace() { return nameSpace; }
	public String getTitleWithoutNameSpace() { return titleWithoutNameSpace; }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass().equals(PageLocation.class)) {
			PageTitle pt = (PageTitle)obj;
			return getTitle().substring(0).equalsIgnoreCase(pt.getTitle().substring(0));
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "\nPageTitle: " + title;
	}
}
