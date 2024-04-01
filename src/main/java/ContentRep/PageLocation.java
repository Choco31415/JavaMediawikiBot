package ContentRep;

import WikiBot.MediawikiBot;

public class PageLocation extends PageTitleContainer {

	private String lan;
	
	/**
	 * @param lan_ The language abbreviation for the wiki.
	 * @param title_ The title of the page.
	 */
	public PageLocation(String lan_, String title_) {
		super(title_);
		lan = lan_;
	}
	
	/**
	 * @param lan_ The language abbreviation for the wiki.
	 * @param pt The Page Title object describing the title of the page.
	 */
	public PageLocation(String lan_, PageTitle pt) {
		super(pt);
		lan = lan_;
	}
	
	/**
	 * The language is assumed to be the home language of the bot.
	 * @param title_ The title of the page.
	 */
	public PageLocation(String title_) {
		super(title_);
		lan = MediawikiBot.getInstance().getHomeWikiLanguage();
	}
	
	/**
	 * The language is assumed to be the home language of the bot.
	 * @param pt The Page Title object describing the title of the page.
	 */
	public PageLocation(PageTitle pt) {
		super(pt);
		lan = MediawikiBot.getInstance().getHomeWikiLanguage();
	}
	
	public String getLanguage() {
		return lan;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass().equals(PageLocation.class)) {
			PageLocation pg = (PageLocation)obj;
			boolean equal = true;
			equal = equal & lan.equals(pg.getLanguage());
			equal = equal & pg.getTitleObject().equals(titleObject);
			return equal;
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		return lan.hashCode() + getTitle().hashCode();
	}
	
	@Override
	public String toString() {
		return "PageLocation: " + lan + ": " + titleObject.getTitle();
	}
}
