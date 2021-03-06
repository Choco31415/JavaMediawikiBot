package WikiBot.ContentRep;


public abstract class PageTitleContainer {

	protected PageTitle titleObject;
	
	public PageTitleContainer(String title_) {
		titleObject = new PageTitle(title_);
	}
	
	public PageTitleContainer(PageTitle titleObject_) {
		titleObject = titleObject_;
	}
	
	//Title methods
	public String getTitle() {
		return titleObject.getTitle();
	}
	
	public PageTitle getTitleObject() {
		return titleObject;
	}
	
	public String getNameSpace() {
		return titleObject.getNameSpace();
	}
	
	public String getTitleWithoutNameSpace() {
		return titleObject.getTitleWithoutNameSpace();
	}
}
