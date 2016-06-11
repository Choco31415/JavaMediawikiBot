package WikiBot.ContentRep;


public abstract class PageLocationContainer {

	protected PageLocation pl;
	
	public PageLocationContainer(String title_, String lan_) {
		pl = new PageLocation(title_, lan_);
	}
	
	public PageLocationContainer(PageLocation pl_) {
		pl = pl_;
	}
	
	//Language methods
	public String getLanguage() {
		return pl.getLanguage();
	}
	
	//Title methods
	public String getTitle() {
		return pl.getTitleObject().getTitle();
	}
	
	public PageTitle getTitleObject() {
		return pl.getTitleObject();
	}
	
	public String getNameSpace() {
		return pl.getTitleObject().getNameSpace();
	}
	
	public String getTitleWithoutNameSpace() {
		return pl.getTitleObject().getTitleWithoutNameSpace();
	}
	
	public PageLocation getPageLocation() {
		return pl;
	}
}
