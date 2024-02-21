package WikiBot.ContentRep.SiteInfo;

import java.util.ArrayList;

import WikiBot.ContentRep.InfoContainer;

public class SiteStatistics extends InfoContainer {
	
	String language;
	
	public SiteStatistics(String language_, ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		super(propertyNames_, propertyValues_);
		language = language_;
	}
	
	public SiteStatistics(String language_) {
		super();
		language = language_;
	}
	
	public String getLanguage() {
		return language;
	}
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of pages.
	 * @return
	 */
	public boolean hasNumPages() { return hasProperty("pages"); }
	public int getNumPages() { return Integer.parseInt(getValue("pages")); }
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of articles,
	 * aka the number of content pages.
	 * @return
	 */
	public boolean hasNumArticle() { return hasProperty("articles"); }
	public int getNumArticles() { return Integer.parseInt(getValue("articles")); }
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of edits.
	 * @return
	 */
	public boolean hasNumEdits() { return hasProperty("edits"); }
	public int getNumEdits() { return Integer.parseInt(getValue("edits")); }
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of images.
	 * @return
	 */
	public boolean hasNumImages() { return hasProperty("images"); }
	public int getNumImages() { return Integer.parseInt(getValue("images")); }
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of users.
	 * @return
	 */
	public boolean hasNumUsers() { return hasProperty("users"); }
	public int getNumUsers() { return Integer.parseInt(getValue("users")); }
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of active users.
	 * @return
	 */
	public boolean hasNumActiveUsers() { return hasProperty("activeusers"); }
	public int getNumActiveUsers() { return Integer.parseInt(getValue("activeusers")); }
	
	/**
	 * Use this method to see if this SiteStatistics contains the number of active users.
	 * @return
	 */
	public boolean hasNumAdmins() { return hasProperty("admins"); }
	public int getNumAdmins() { return Integer.parseInt(getValue("admins")); }
	
	@Override
	public String toString() {
		String output = "";
		
		output += "Site statistics for: " + language;
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ": " + propertyValues.get(i);
		}
		
		return output;
	}
}
