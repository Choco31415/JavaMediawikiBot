package WikiBot.Content;

import java.util.ArrayList;

public abstract class PageObjectAdvanced extends PageObject {
	//Name variables will be created in their respective classes.
	protected String header;
	protected ArrayList<PageObject> pageObjects = new ArrayList<PageObject>();
	protected ArrayList<String> parameters = new ArrayList<String>();

	public PageObjectAdvanced(String myObjectType_, String header_, int openPos_, int closePos_, ArrayList<String> parameters_, ArrayList<PageObject> pageObjects_, String textOpening_, String textClosing_) {
		super(myObjectType_, openPos_, closePos_, textOpening_, textClosing_);
		header = header_;
		pageObjects = pageObjects_;
		parameters = parameters_;
	}
	
	public PageObjectAdvanced(String myObjectType_, String header_, int openPos_, int closePos_, String textOpening_, String textClosing_) {
		super(myObjectType_, openPos_, closePos_, textOpening_, textClosing_);
		header = header_;
	}
	
	//Set information.
	public void addPageObjects(ArrayList<PageObject> po) {
		pageObjects.addAll(po);
	}
	
	public void addParameter(String param) {
		parameters.add(param);
	}
	
	//Get information.
	
	public String getHeader() {
		return header;
	}
	
	public ArrayList<PageObject> getPageObjects() {
		return pageObjects;
	}
	
	public PageObject getPageObject(int index) {
		return pageObjects.get(index);
	}
	
	public String getParameter(int index) {
		return parameters.get(index);
	}
}
