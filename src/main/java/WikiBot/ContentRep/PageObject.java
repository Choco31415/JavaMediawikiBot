package WikiBot.ContentRep;

/**
 * This class is used to represent various features of a page.
 * For example, templates, image, categories, and more.
 */
public abstract class PageObject {
	//Name variables will be created in their respective classes.
	protected int openPos;
	protected int closePos;
	protected String myObjectType;
	
	protected String textOpening;
	protected String textClosing;

	public PageObject(String myObjectType_, int openPos_, int closePos_, String textOpening_, String textClosing_) {
		myObjectType = myObjectType_;
		openPos = openPos_;
		closePos = closePos_;
		textOpening = textOpening_;
		textClosing = textClosing_;
	}
	
	//Set information
	public void setOpeningPosition(int index) {
		openPos = index;
	}
	
	//Get information.
	public int getOpeningPosition() {
		return openPos;
	}
	
	public int getClosingPosition() {
		return closePos;
	}
	
	public String getObjectType() {
		return myObjectType;
	}
	
	public String getOpeningString() {
		return textOpening;
	}
	
	public String getClosingString() {
		return textClosing;
	}

	/*
	 * A simple to string method that should stay within 1 line.
	 */
	public abstract String simpleToString();
	
	public abstract String toString();
	
	//TODO: Add equals method.
}
