package WikiBot.ContentRep;

import WikiBot.Core.GenericBot;


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
		lan = GenericBot.getInstance().getHomeWikiLanguage();
	}
	
	/**
	 * The language is assumed to be the home language of the bot.
	 * @param pt The Page Title object describing the title of the page.
	 */
	public PageLocation(PageTitle pt) {
		super(pt);
		lan = GenericBot.getInstance().getHomeWikiLanguage();
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
			return pg.getTitle().equalsIgnoreCase(titleObject.getTitle()) && lan.equals(pg.getLanguage());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "PageLocation: " + lan + ": " + titleObject.getTitle();
	}
}
